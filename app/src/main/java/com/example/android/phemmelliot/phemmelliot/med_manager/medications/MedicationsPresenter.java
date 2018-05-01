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

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.android.phemmelliot.phemmelliot.med_manager.addmedication.AddMedicationActivity;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsRepository;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link MedicationsFragment}), retrieves the data and updates the
 * UI as required.
 */
public class MedicationsPresenter implements MedicationsContract.Presenter {

    private final MedicationsRepository mMedicationsRepository;

    private final MedicationsContract.View mMedicationsView;

    private MedicationsFilterType mCurrentFiltering = MedicationsFilterType.ALL_MEDICATIONS;

    private boolean mFirstLoad = true;

    public MedicationsPresenter(@NonNull MedicationsRepository medicationsRepository, @NonNull MedicationsContract.View medicationsView) {
        mMedicationsRepository = checkNotNull(medicationsRepository, "medicationsRepository cannot be null");
        mMedicationsView = checkNotNull(medicationsView, "medicationsView cannot be null!");

        mMedicationsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadMedications(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        if (AddMedicationActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mMedicationsView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadMedications(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadMedications(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link MedicationsDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadMedications(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mMedicationsView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mMedicationsRepository.refreshMessages();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mMedicationsRepository.getMessages(new MedicationsDataSource.LoadMessagesCallback() {
            @Override
            public void onMessagesLoaded(List<Medication> medications) {
                List<Medication> tasksToShow = new ArrayList<Medication>();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We filter the medications based on the requestType
                for (Medication medication : medications) {
                    switch (mCurrentFiltering) {
                        case ALL_MEDICATIONS:
                            tasksToShow.add(medication);
                            break;
                        case ACTIVE_MEDICATIONS:
                            if (medication.isActive()) {
                                tasksToShow.add(medication);
                            }
                            break;
                        case COMPLETED_MEDICATIONS:
                            if (medication.isCompleted()) {
                                tasksToShow.add(medication);
                            }
                            break;
                        default:
                            tasksToShow.add(medication);
                            break;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mMedicationsView.isActive()) {
                    return;
                }
                if (showLoadingUI) {
                    mMedicationsView.setLoadingIndicator(false);
                }

                processMedications(tasksToShow);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mMedicationsView.isActive()) {
                    return;
                }
                mMedicationsView.showLoadingMedicationsError();
            }
        });
    }

    private void processMedications(List<Medication> medications) {
        if (medications.isEmpty()) {
            // Show a message indicating there are no medications for that filter type.
            processEmptyTasks();
        } else {
            // Show the list of medications
            mMedicationsView.showMedications(medications);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ACTIVE_MEDICATIONS:
                mMedicationsView.showActiveFilterLabel();
                break;
            case COMPLETED_MEDICATIONS:
                mMedicationsView.showCompletedFilterLabel();
                break;
            default:
                mMedicationsView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTasks() {
        switch (mCurrentFiltering) {
            case ACTIVE_MEDICATIONS:
                mMedicationsView.showNoActiveMedications();
                break;
            case COMPLETED_MEDICATIONS:
                mMedicationsView.showNoCompletedMedications();
                break;
            default:
                mMedicationsView.showNoMedications();
                break;
        }
    }

    @Override
    public void addNewMedication() {
        mMedicationsView.showAddMedication();
    }

    @Override
    public void openMedicationDetails(@NonNull Medication requestedMedication) {
        checkNotNull(requestedMedication, "requestedMedication cannot be null!");
        mMedicationsView.showMedicationDetailsUi(requestedMedication.getId());
    }

    @Override
    public void completeMedication(@NonNull Medication completedMedication) {
        checkNotNull(completedMedication, "completedMedication cannot be null!");
        mMedicationsRepository.completeMessage(completedMedication);
        mMedicationsView.showMedicationMarkedComplete();
        loadMedications(false, false);
    }

    @Override
    public void activateMedication(@NonNull Medication activeMedication) {
        checkNotNull(activeMedication, "activeMedication cannot be null!");
        mMedicationsRepository.activateMessage(activeMedication);
        mMedicationsView.showMedicationMarkedActive();
        loadMedications(false, false);
    }

    @Override
    public void clearCompletedMedications() {
        mMedicationsRepository.clearCompletedMessages();
        mMedicationsView.showCompletedMedicationsCleared();
        loadMedications(false, false);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link MedicationsFilterType#ALL_MEDICATIONS},
     *                    {@link MedicationsFilterType#COMPLETED_MEDICATIONS}, or
     *                    {@link MedicationsFilterType#ACTIVE_MEDICATIONS}
     */
    @Override
    public void setFiltering(MedicationsFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public MedicationsFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
