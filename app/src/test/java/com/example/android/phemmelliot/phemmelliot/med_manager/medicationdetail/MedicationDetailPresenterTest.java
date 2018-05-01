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

package com.example.android.phemmelliot.phemmelliot.med_manager.medicationdetail;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link MedicationDetailPresenter}
 */
public class MedicationDetailPresenterTest {

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

    public static final String INVALID_TASK_ID = "";

    public static final Medication ACTIVE_MEDICATION = new Medication(TITLE_TEST, DESCRIPTION_TEST);

    public static final Medication COMPLETED_MEDICATION = new Medication(TITLE_TEST, DESCRIPTION_TEST, true);

    @Mock
    private MedicationsRepository mTasksRepository;

    @Mock
    private MedicationDetailContract.View mTaskDetailView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<MedicationsDataSource.GetMedicationsCallback> mGetTaskCallbackCaptor;

    private MedicationDetailPresenter mMedicationDetailPresenter;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // The presenter won't update the view unless it's active.
        when(mTaskDetailView.isActive()).thenReturn(true);
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                ACTIVE_MEDICATION.getId(), mTasksRepository, mTaskDetailView);

        // Then the presenter is set to the view
        verify(mTaskDetailView).setPresenter(mMedicationDetailPresenter);
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        // When medications presenter is asked to open a task
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                ACTIVE_MEDICATION.getId(), mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getMessage(eq(ACTIVE_MEDICATION.getId()), mGetTaskCallbackCaptor.capture());
        InOrder inOrder = inOrder(mTaskDetailView);
        inOrder.verify(mTaskDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onMessagesLoaded(ACTIVE_MEDICATION); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(mTaskDetailView).setLoadingIndicator(false);
        verify(mTaskDetailView).showTitle(TITLE_TEST);
        verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTaskDetailView).showCompletionStatus(false);
    }

    @Test
    public void getCompletedTaskFromRepositoryAndLoadIntoView() {
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                COMPLETED_MEDICATION.getId(), mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getMessage(
                eq(COMPLETED_MEDICATION.getId()), mGetTaskCallbackCaptor.capture());
        InOrder inOrder = inOrder(mTaskDetailView);
        inOrder.verify(mTaskDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onMessagesLoaded(COMPLETED_MEDICATION); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(mTaskDetailView).setLoadingIndicator(false);
        verify(mTaskDetailView).showTitle(TITLE_TEST);
        verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTaskDetailView).showCompletionStatus(true);
    }

    @Test
    public void getUnknownTaskFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid task ID.
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                INVALID_TASK_ID, mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.start();
        verify(mTaskDetailView).showMissingTask();
    }

    @Test
    public void deleteTask() {
        // Given an initialized MedicationDetailPresenter with stubbed medication
        Medication medication = new Medication(TITLE_TEST, DESCRIPTION_TEST);

        // When the deletion of a medication is requested
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                medication.getId(), mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.deleteTask();

        // Then the repository and the view are notified
        verify(mTasksRepository).deleteMessage(medication.getId());
        verify(mTaskDetailView).showTaskDeleted();
    }

    @Test
    public void completeTask() {
        // Given an initialized presenter with an active medication
        Medication medication = new Medication(TITLE_TEST, DESCRIPTION_TEST);
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                medication.getId(), mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.start();

        // When the presenter is asked to complete the medication
        mMedicationDetailPresenter.completeTask();

        // Then a request is sent to the medication repository and the UI is updated
        verify(mTasksRepository).completeMessage(medication.getId());
        verify(mTaskDetailView).showTaskMarkedComplete();
    }

    @Test
    public void activateTask() {
        // Given an initialized presenter with a completed medication
        Medication medication = new Medication(TITLE_TEST, DESCRIPTION_TEST, true);
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                medication.getId(), mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.start();

        // When the presenter is asked to activate the medication
        mMedicationDetailPresenter.activateTask();

        // Then a request is sent to the medication repository and the UI is updated
        verify(mTasksRepository).activateMessage(medication.getId());
        verify(mTaskDetailView).showTaskMarkedActive();
    }

    @Test
    public void activeTaskIsShownWhenEditing() {
        // When the edit of an ACTIVE_MEDICATION is requested
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                ACTIVE_MEDICATION.getId(), mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.editTask();

        // Then the view is notified
        verify(mTaskDetailView).showEditTask(ACTIVE_MEDICATION.getId());
    }

    @Test
    public void invalidTaskIsNotShownWhenEditing() {
        // When the edit of an invalid task id is requested
        mMedicationDetailPresenter = new MedicationDetailPresenter(
                INVALID_TASK_ID, mTasksRepository, mTaskDetailView);
        mMedicationDetailPresenter.editTask();

        // Then the edit mode is never started
        verify(mTaskDetailView, never()).showEditTask(INVALID_TASK_ID);
        // instead, the error is shown.
        verify(mTaskDetailView).showMissingTask();
    }

}
