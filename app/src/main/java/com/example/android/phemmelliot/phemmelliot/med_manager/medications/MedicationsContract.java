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

import android.support.annotation.NonNull;

import com.example.android.phemmelliot.phemmelliot.med_manager.BaseView;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.BasePresenter;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface MedicationsContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showMedications(List<Medication> medications);

        void showAddMedication();

        void showMedicationDetailsUi(String taskId);

        void showMedicationMarkedComplete();

        void showMedicationMarkedActive();

        void showCompletedMedicationsCleared();

        void showLoadingMedicationsError();

        void showNoMedications();

        void showActiveFilterLabel();

        void showCompletedFilterLabel();

        void showAllFilterLabel();

        void showNoActiveMedications();

        void showNoCompletedMedications();

        void showSuccessfullySavedMessage();

        boolean isActive();

        void showFilteringPopUpMenu();
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadMedications(boolean forceUpdate);

        void addNewMedication();

        void openMedicationDetails(@NonNull Medication requestedMedication);

        void completeMedication(@NonNull Medication completedMedication);

        void activateMedication(@NonNull Medication activeMedication);

        void clearCompletedMedications();

        void setFiltering(MedicationsFilterType requestType);

        MedicationsFilterType getFiltering();
    }
}
