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

package com.example.android.phemmelliot.phemmelliot.med_manager.addmedication;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link AddMedicationPresenter}.
 */
public class AddMedicationPresenterTest {

    @Mock
    private MedicationsRepository mTasksRepository;

    @Mock
    private AddMedicationContract.View mAddEditTaskView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<MedicationsDataSource.GetMedicationsCallback> mGetTaskCallbackCaptor;

    private AddMedicationPresenter mAddMedicationPresenter;

    @Before
    public void setupMocksAndView() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // The presenter wont't update the view unless it's active.
        when(mAddEditTaskView.isActive()).thenReturn(true);
    }

    @Test
    public void createPresenter_setsThePresenterToView(){
        // Get a reference to the class under test
        mAddMedicationPresenter = new AddMedicationPresenter(
                null, mTasksRepository, mAddEditTaskView, true);

        // Then the presenter is set to the view
        verify(mAddEditTaskView).setPresenter(mAddMedicationPresenter);
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddMedicationPresenter = new AddMedicationPresenter(
                null, mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to save a task
        mAddMedicationPresenter.saveTask("New Medication Title", "Some Medication Description", "3", "22/22/2016",
                "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0);

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveMessage(any(Medication.class)); // saved to the model
        verify(mAddEditTaskView).showMedicationsList(); // shown in the UI
    }

    @Test
    public void saveTask_emptyTaskShowsErrorUi() {
        // Get a reference to the class under test
        mAddMedicationPresenter = new AddMedicationPresenter(
                null, mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to save an empty task
        mAddMedicationPresenter.saveTask("", "", "3", "22/22/2016",
                "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0);

        // Then an empty not error is shown in the UI
        verify(mAddEditTaskView).showEmptyMedicationError();
    }

    @Test
    public void saveExistingTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddMedicationPresenter = new AddMedicationPresenter(
                "1", mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to save an existing task
        mAddMedicationPresenter.saveTask("Existing Medication Title", "Some Medication Description", "3", "22/22/2016",
                "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0);

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveMessage(any(Medication.class)); // saved to the model
        verify(mAddEditTaskView).showMedicationsList(); // shown in the UI
    }

    @Test
    public void populateTask_callsRepoAndUpdatesView() {
        Medication testMedication = new Medication("TITLE", "DESCRIPTION", "3", "22/22/2016",
                "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0);
        // Get a reference to the class under test
        mAddMedicationPresenter = new AddMedicationPresenter(testMedication.getId(),
                mTasksRepository, mAddEditTaskView, true);

        // When the presenter is asked to populate an existing task
        mAddMedicationPresenter.populateMedication();

        // Then the task repository is queried and the view updated
        verify(mTasksRepository).getMessage(eq(testMedication.getId()), mGetTaskCallbackCaptor.capture());
        assertThat(mAddMedicationPresenter.isDataMissing(), is(true));

        // Simulate callback
        mGetTaskCallbackCaptor.getValue().onMessagesLoaded(testMedication);

        verify(mAddEditTaskView).setTitle(testMedication.getTitle());
        verify(mAddEditTaskView).setDescription(testMedication.getDescription());
        assertThat(mAddMedicationPresenter.isDataMissing(), is(false));
    }
}
