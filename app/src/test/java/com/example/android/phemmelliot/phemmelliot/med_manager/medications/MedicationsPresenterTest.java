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

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource.LoadMessagesCallback;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link MedicationsPresenter}
 */
public class MedicationsPresenterTest {

    private static List<Medication> Medications;

    @Mock
    private MedicationsRepository mTasksRepository;

    @Mock
    private MedicationsContract.View mTasksView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LoadMessagesCallback> mLoadTasksCallbackCaptor;

    private MedicationsPresenter mMedicationsPresenter;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mMedicationsPresenter = new MedicationsPresenter(mTasksRepository, mTasksView);

        // The presenter won't update the view unless it's active.
        when(mTasksView.isActive()).thenReturn(true);

        // We start the medications to 3, with one active and two completed
        Medications = Lists.newArrayList(new Medication("Title1", "Description1"),
                new Medication("Title2", "Description2", true), new Medication("Title3", "Description3", true));
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mMedicationsPresenter = new MedicationsPresenter(mTasksRepository, mTasksView);

        // Then the presenter is set to the view
        verify(mTasksView).setPresenter(mMedicationsPresenter);
    }

    @Test
    public void loadAllTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized MedicationsPresenter with initialized medications
        // When loading of Tasks is requested
        mMedicationsPresenter.setFiltering(MedicationsFilterType.ALL_MEDICATIONS);
        mMedicationsPresenter.loadMedications(true);

        // Callback is captured and invoked with stubbed medications
        verify(mTasksRepository).getMessages(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onMessagesLoaded(Medications);

        // Then progress indicator is shown
        InOrder inOrder = inOrder(mTasksView);
        inOrder.verify(mTasksView).setLoadingIndicator(true);
        // Then progress indicator is hidden and all medications are shown in UI
        inOrder.verify(mTasksView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showMedications(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 3);
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized MedicationsPresenter with initialized medications
        // When loading of Tasks is requested
        mMedicationsPresenter.setFiltering(MedicationsFilterType.ACTIVE_MEDICATIONS);
        mMedicationsPresenter.loadMedications(true);

        // Callback is captured and invoked with stubbed medications
        verify(mTasksRepository).getMessages(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onMessagesLoaded(Medications);

        // Then progress indicator is hidden and active medications are shown in UI
        verify(mTasksView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showMedications(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 1);
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized MedicationsPresenter with initialized medications
        // When loading of Tasks is requested
        mMedicationsPresenter.setFiltering(MedicationsFilterType.COMPLETED_MEDICATIONS);
        mMedicationsPresenter.loadMedications(true);

        // Callback is captured and invoked with stubbed medications
        verify(mTasksRepository).getMessages(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onMessagesLoaded(Medications);

        // Then progress indicator is hidden and completed medications are shown in UI
        verify(mTasksView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showMedications(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {
        // When adding a new task
        mMedicationsPresenter.addNewTask();

        // Then add task UI is shown
        verify(mTasksView).showAddMedication();
    }

    @Test
    public void clickOnTask_ShowsDetailUi() {
        // Given a stubbed active task
        Medication requestedMedication = new Medication("Details Requested", "For this task");

        // When open task details is requested
        mMedicationsPresenter.openTaskDetails(requestedMedication);

        // Then task detail UI is shown
        verify(mTasksView).showMedicationDetailsUi(any(String.class));
    }

    @Test
    public void completeTask_ShowsTaskMarkedComplete() {
        // Given a stubbed medication
        Medication medication = new Medication("Details Requested", "For this medication");

        // When medication is marked as complete
        mMedicationsPresenter.completeTask(medication);

        // Then repository is called and medication marked complete UI is shown
        verify(mTasksRepository).completeMessage(medication);
        verify(mTasksView).showMedicationMarkedComplete();
    }

    @Test
    public void activateTask_ShowsTaskMarkedActive() {
        // Given a stubbed completed medication
        Medication medication = new Medication("Details Requested", "For this medication", true);
        mMedicationsPresenter.loadMedications(true);

        // When medication is marked as activated
        mMedicationsPresenter.activateTask(medication);

        // Then repository is called and medication marked active UI is shown
        verify(mTasksRepository).activateMessage(medication);
        verify(mTasksView).showMedicationMarkedActive();
    }

    @Test
    public void unavailableTasks_ShowsError() {
        // When medications are loaded
        mMedicationsPresenter.setFiltering(MedicationsFilterType.ALL_MEDICATIONS);
        mMedicationsPresenter.loadMedications(true);

        // And the medications aren't available in the repository
        verify(mTasksRepository).getMessages(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        verify(mTasksView).showLoadingMedicationsError();
    }
}
