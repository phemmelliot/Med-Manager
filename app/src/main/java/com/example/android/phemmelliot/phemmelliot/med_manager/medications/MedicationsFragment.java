/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.phemmelliot.phemmelliot.med_manager.medications;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.phemmelliot.phemmelliot.med_manager.R;
import com.example.android.phemmelliot.phemmelliot.med_manager.addmedication.AddMedicationActivity;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.login.LoginActivity;
import com.example.android.phemmelliot.phemmelliot.med_manager.medicationdetail.MedicationDetailActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Medication}s. User can choose to view all, active or completed medications.
 */
public class MedicationsFragment extends Fragment implements MedicationsContract.View {

    private MedicationsContract.Presenter mPresenter;

    private MedicationsAdapter mListAdapter;

    private View mNoMedicationsView;

    private ImageView mNoMedicationIcon;

    private TextView mNoMedicationMainView;

    private TextView mNoMedicationAddView;

    private LinearLayout mMedicationsView;

    private TextView mFilteringLabelView;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    public GoogleApiClient mGoogleApiClient;

    public MedicationsFragment() {
        // Requires empty public constructor
    }

    public static MedicationsFragment newInstance() {
        return new MedicationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new MedicationsAdapter(new ArrayList<Medication>(0), mItemListener);
    }

    @Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull MedicationsContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.medications_frag, container, false);

        // Set up medications view
        ListView listView =  root.findViewById(R.id.medication_recycler);
        listView.setAdapter(mListAdapter);
        mFilteringLabelView =  root.findViewById(R.id.filteringLabel);
        mMedicationsView = root.findViewById(R.id.medicationLayout);
        mAuth = FirebaseAuth.getInstance();

        // Set up  no medications view
        mNoMedicationsView = root.findViewById(R.id.noMedications);
        mNoMedicationIcon =  root.findViewById(R.id.noMedicationsIcon);
        mNoMedicationMainView = root.findViewById(R.id.noMedicationsMain);
        mNoMedicationAddView =  root.findViewById(R.id.noMedicationsAdd);
        mNoMedicationAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMedication();
            }
        });

        progressDialog = new ProgressDialog(getContext());

        // Set up floating action button
        FloatingActionButton fab =
                 getActivity().findViewById(R.id.fab_add_medication);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.addNewMedication();
            }
        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                 root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadMedications(false);
            }
        });


        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mPresenter.clearCompletedMedications();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mPresenter.loadMedications(true);
                break;
            case R.id.menu_log_out:
                logOutFromGoogleAndFirebase();
                break;
        }
        return true;
    }

    private void logOutFromGoogleAndFirebase() {
            //showProgressDialog;
        if(isNetworkAvailable()) {
            progressDialog.setMessage("Signing out....");
            progressDialog.show();
            if (Auth.GoogleSignInApi != null) {
                //log out of firebase
                mAuth.signOut();
                //log out of google
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        });
            }
        }else {
            progressDialog.hide();
            Snackbar.make(getView(), "Log Out Failed, No Internet", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logOutFromGoogleAndFirebase();
                        }
                    }).show();
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        return activeInfo != null;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.medications_fragment_menu, menu);
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_medications, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mPresenter.setFiltering(MedicationsFilterType.ACTIVE_MEDICATIONS);
                        break;
                    case R.id.completed:
                        mPresenter.setFiltering(MedicationsFilterType.COMPLETED_MEDICATIONS);
                        break;
                    default:
                        mPresenter.setFiltering(MedicationsFilterType.ALL_MEDICATIONS);
                        break;
                }
                mPresenter.loadMedications(false);
                return true;
            }
        });

        popup.show();
    }

    /**
     * Listener for clicks on medications in the ListView.
     */
    TaskItemListener mItemListener = new TaskItemListener() {
        @Override
        public void onMedicationClick(Medication clickedMedication) {
            mPresenter.openMedicationDetails(clickedMedication);
        }

        @Override
        public void onCompleteMedicationClick(Medication completedMedication) {
            mPresenter.completeMedication(completedMedication);
        }

        @Override
        public void onActivateMedicationClick(Medication activatedMedication) {
            mPresenter.activateMedication(activatedMedication);
        }
    };

    @Override
    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showMedications(List<Medication> medications) {
        mListAdapter.replaceData(medications);

        mMedicationsView.setVisibility(View.VISIBLE);
        mNoMedicationsView.setVisibility(View.GONE);
    }

    @Override
    public void showNoActiveMedications() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_active),
                R.drawable.ic_check_circle_24dp,
                false
        );
    }

    @Override
    public void showNoMedications() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_all),
                R.drawable.ic_assignment_turned_in_24dp,
                false
        );
    }

    @Override
    public void showNoCompletedMedications() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_completed),
                R.drawable.ic_verified_user_24dp,
                false
        );
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message));
    }

    private void showNoTasksViews(String mainText, int iconRes, boolean showAddView) {
        mMedicationsView.setVisibility(View.GONE);
        mNoMedicationsView.setVisibility(View.VISIBLE);

        mNoMedicationMainView.setText(mainText);
        mNoMedicationIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoMedicationAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showActiveFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    @Override
    public void showCompletedFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_completed));
    }

    @Override
    public void showAllFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_all));
    }

    @Override
    public void showAddMedication() {
        Intent intent = new Intent(getContext(), AddMedicationActivity.class);
        startActivityForResult(intent, AddMedicationActivity.REQUEST_ADD_TASK);
    }

    @Override
    public void showMedicationDetailsUi(String taskId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        Intent intent = new Intent(getContext(), MedicationDetailActivity.class);
        intent.putExtra(MedicationDetailActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    @Override
    public void showMedicationMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete));
    }

    @Override
    public void showMedicationMarkedActive() {
        showMessage(getString(R.string.task_marked_active));
    }

    @Override
    public void showCompletedMedicationsCleared() {
        showMessage(getString(R.string.completed_tasks_cleared));
    }

    @Override
    public void showLoadingMedicationsError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private static class MedicationsAdapter extends BaseAdapter {

        private List<Medication> mMedications;
        private TaskItemListener mItemListener;

        public MedicationsAdapter(List<Medication> medications, TaskItemListener itemListener) {
            setList(medications);
            mItemListener = itemListener;
        }

        public void replaceData(List<Medication> medications) {
            setList(medications);
            notifyDataSetChanged();
        }

        private void setList(List<Medication> medications) {
            mMedications = checkNotNull(medications);
        }

        @Override
        public int getCount() {
            return mMedications.size();
        }

        @Override
        public Medication getItem(int i) {
            return mMedications.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.medication_item, viewGroup, false);
            }

            final Medication medication = getItem(i);

            TextView titleTV = rowView.findViewById(R.id.title);
            TextView nextIntakeTV = rowView.findViewById(R.id.next_intake);
            TextView frequencyTV = rowView.findViewById(R.id.frequency_tv);
            titleTV.setText(medication.getTitleForList());

            CheckBox completeCB = rowView.findViewById(R.id.complete);

            // Active/completed medication UI
            completeCB.setChecked(medication.isCompleted());
            if (medication.isCompleted()) {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.list_completed_touch_feedback));
            } else {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.touch_feedback));
            }

            completeCB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!medication.isCompleted()) {
                        mItemListener.onCompleteMedicationClick(medication);
                    } else {
                        mItemListener.onActivateMedicationClick(medication);
                    }
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemListener.onMedicationClick(medication);
                }
            });

            return rowView;
        }
    }

    public interface TaskItemListener {

        void onMedicationClick(Medication clickedMedication);

        void onCompleteMedicationClick(Medication completedMedication);

        void onActivateMedicationClick(Medication activatedMedication);
    }

}
