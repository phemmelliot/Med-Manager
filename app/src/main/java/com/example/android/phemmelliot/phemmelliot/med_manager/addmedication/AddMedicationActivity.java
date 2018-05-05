package com.example.android.phemmelliot.phemmelliot.med_manager.addmedication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.phemmelliot.phemmelliot.med_manager.Injection;
import com.example.android.phemmelliot.phemmelliot.med_manager.R;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.ActivityUtils;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.EspressoIdlingResource;

/**
 * Displays an add or edit task screen.
 */
public class AddMedicationActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_TASK = 1;

    public static final String SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY";

    private AddMedicationPresenter mAddMedicationPresenter;

    private ActionBar mActionBar;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addmedication_act);

        // Set up the toolbar.
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        AddMedicationFragment addMedicationFragment = (AddMedicationFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        String taskId = getIntent().getStringExtra(AddMedicationFragment.ARGUMENT_EDIT_MEDICATION_ID);

        setToolbarTitle(taskId);

        if (addMedicationFragment == null) {
            addMedicationFragment = AddMedicationFragment.newInstance();

            if (getIntent().hasExtra(AddMedicationFragment.ARGUMENT_EDIT_MEDICATION_ID)) {
                Bundle bundle = new Bundle();
                bundle.putString(AddMedicationFragment.ARGUMENT_EDIT_MEDICATION_ID, taskId);
                addMedicationFragment.setArguments(bundle);
            }

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    addMedicationFragment, R.id.contentFrame);
        }

        boolean shouldLoadDataFromRepo = true;

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY);
        }

        // Create the presenter
        mAddMedicationPresenter = new AddMedicationPresenter(
                taskId,
                Injection.provideTasksRepository(getApplicationContext()),
                addMedicationFragment,
                shouldLoadDataFromRepo);

        sharedPreferences = getSharedPreferences("ADD_MED", MODE_PRIVATE);
    }

    private void setToolbarTitle(@Nullable String taskId) {
        if(taskId == null) {
            mActionBar.setTitle(R.string.add_task);
        } else {
            mActionBar.setTitle(R.string.edit_task);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the state so that next time we know if we need to refresh data.
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mAddMedicationPresenter.isDataMissing());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sharedPreferences.edit().clear().apply();
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
