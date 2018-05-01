package com.example.android.phemmelliot.phemmelliot.med_manager.addmedication;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.phemmelliot.phemmelliot.med_manager.R;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddMedicationFragment extends Fragment implements AddMedicationContract.View {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";

    private AddMedicationContract.Presenter mPresenter;

    private TextView mTitle;

    private TextView mDescription;

    private Spinner mFrequencySpinner;

    private EditText mStart, mEnd, mStartTime, mEndTime, mMidTime;

    public static AddMedicationFragment newInstance() {
        return new AddMedicationFragment();
    }

    public AddMedicationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull AddMedicationContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab =
                 getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrequencySpinner.getSelectedItemPosition();
                Integer frequencyInt = mFrequencySpinner.getSelectedItemPosition();
                String frequency = frequencyInt.toString();
                String startDate = mStart.getText().toString();
                String endDate = mEnd.getText().toString();
                mPresenter.saveTask(mTitle.getText().toString(), mDescription.getText().toString(), frequency,
                        startDate,endDate);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addmedication_frag, container, false);
        mTitle =  root.findViewById(R.id.add_medication_name);
        mDescription =  root.findViewById(R.id.add_medication_description);
        mFrequencySpinner = root.findViewById(R.id.frequency_spinner);
        mStart = root.findViewById(R.id.start_et);
        mEnd = root.findViewById(R.id.end_et);
        mStartTime = root.findViewById(R.id.start_time_et);
        mEndTime = root.findViewById(R.id.end_time_et);
        mMidTime = root.findViewById(R.id.mid_time_et);

        mPresenter.setMyCalendarAndDate();
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickDate(getContext(), "start");
            }
        });

        mEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickDate(getContext(), "end");
            }
        });


        mFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    mEndTime.setVisibility(View.VISIBLE);
                    clickStartTime();
                    clickEndTime();
                }else if(position == 2){
                    mEndTime.setVisibility(View.VISIBLE);
                    mMidTime.setVisibility(View.VISIBLE);
                    clickEndTime();
                    clickStartTime();
                    clickMidTime();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setHasOptionsMenu(true);
        return root;
    }

    private void clickStartTime(){
        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickTime(getContext(), "startTime");
            }
        });
    }

    private void clickEndTime(){
        mEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickTime(getContext(), "endTime");
            }
        });
    }

    private void clickMidTime(){
        mMidTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickTime(getContext(), "midTime");
            }
        });
    }

    @Override
    public void showEmptyMedicationError() {
        Snackbar.make(mTitle, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMedicationsList() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setDescription(String description) {
        mDescription.setText(description);
    }



    @Override
    public void setFrequency(String frequency) {
        int position = Integer.parseInt(frequency);
        mFrequencySpinner.setSelection(position);
    }

    @Override
    public void setStartDate(String startDate) {
          mStart.setText(startDate);
    }

    @Override
    public void setEndDate(String endDate) {
         mEnd.setText(endDate);
    }

    @Override
    public void setDate(String editText, int dayOfMonth, int month, int year) {
        if(editText.equals("start"))
            mStart.setText(String.format(Locale.US, "%d/%d/%d", dayOfMonth, month, year));
        else if(editText.equals("end"))
            mEnd.setText(String.format(Locale.US, "%d/%d/%d", dayOfMonth, month, year));
    }

    @Override
    public void setTime(int hour, int minute, String editText) {
        switch (editText) {
            case "startTime":
                mStartTime.setText(String.format(Locale.US, "%d : %d", hour, minute));
                break;
            case "endTime":
                mEndTime.setText(String.format(Locale.US, "%d : %d", hour, minute));
                break;
            case "midTime":
                mMidTime.setText(String.format(Locale.US, "%d : %d", hour, minute));
                break;
        }

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


}
