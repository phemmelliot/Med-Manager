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

package com.example.android.phemmelliot.phemmelliot.med_manager.data.source;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class MedicationsRepositoryTest {

    private final static String TASK_TITLE = "title";

    private final static String TASK_TITLE2 = "title2";

    private final static String TASK_TITLE3 = "title3";

    private static List<Medication> Medications = Lists.newArrayList(new Medication("Title1", "Description1"),
            new Medication("Title2", "Description2"));

    private MedicationsRepository mTasksRepository;

    @Mock
    private MedicationsDataSource mTasksRemoteDataSource;

    @Mock
    private MedicationsDataSource mTasksLocalDataSource;

    @Mock
    private MedicationsDataSource.GetMedicationsCallback mGetMedicationsCallback;

    @Mock
    private MedicationsDataSource.LoadMessagesCallback mLoadMessagesCallback;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<MedicationsDataSource.LoadMessagesCallback> mTasksCallbackCaptor;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<MedicationsDataSource.GetMedicationsCallback> mTaskCallbackCaptor;

    @Before
    public void setupTasksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksRepository = MedicationsRepository.getInstance(
                mTasksRemoteDataSource, mTasksLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        MedicationsRepository.destroyInstance();
    }

    @Test
    public void getTasks_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the medications repository
        twoTasksLoadCallsToRepository(mLoadMessagesCallback);

        // Then medications were only requested once from Service API
        verify(mTasksRemoteDataSource).getMessages(any(MedicationsDataSource.LoadMessagesCallback.class));
    }

    @Test
    public void getTasks_requestsAllTasksFromLocalDataSource() {
        // When medications are requested from the medications repository
        mTasksRepository.getMessages(mLoadMessagesCallback);

        // Then medications are loaded from the local data source
        verify(mTasksLocalDataSource).getMessages(any(MedicationsDataSource.LoadMessagesCallback.class));
    }

    @Test
    public void saveTask_savesTaskToServiceAPI() {
        // Given a stub task with title and description
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description");

        // When a task is saved to the medications repository
        mTasksRepository.saveMessage(newMedication);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).saveMessage(newMedication);
        verify(mTasksLocalDataSource).saveMessage(newMedication);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
    }

    @Test
    public void completeTask_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description");
        mTasksRepository.saveMessage(newMedication);

        // When a task is completed to the medications repository
        mTasksRepository.completeMessage(newMedication);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeMessage(newMedication);
        verify(mTasksLocalDataSource).completeMessage(newMedication);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newMedication.getId()).isActive(), is(false));
    }

    @Test
    public void completeTaskId_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description");
        mTasksRepository.saveMessage(newMedication);

        // When a task is completed using its id to the medications repository
        mTasksRepository.completeMessage(newMedication.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeMessage(newMedication);
        verify(mTasksLocalDataSource).completeMessage(newMedication);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newMedication.getId()).isActive(), is(false));
    }

    @Test
    public void activateTask_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description", true);
        mTasksRepository.saveMessage(newMedication);

        // When a completed task is activated to the medications repository
        mTasksRepository.activateMessage(newMedication);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateMessage(newMedication);
        verify(mTasksLocalDataSource).activateMessage(newMedication);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newMedication.getId()).isActive(), is(true));
    }

    @Test
    public void activateTaskId_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description", true);
        mTasksRepository.saveMessage(newMedication);

        // When a completed task is activated with its id to the medications repository
        mTasksRepository.activateMessage(newMedication.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateMessage(newMedication);
        verify(mTasksLocalDataSource).activateMessage(newMedication);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newMedication.getId()).isActive(), is(true));
    }

    @Test
    public void getTask_requestsSingleTaskFromLocalDataSource() {
        // When a task is requested from the medications repository
        mTasksRepository.getMessage(TASK_TITLE, mGetMedicationsCallback);

        // Then the task is loaded from the database
        verify(mTasksLocalDataSource).getMessage(eq(TASK_TITLE), any(
                MedicationsDataSource.GetMedicationsCallback.class));
    }

    @Test
    public void deleteCompletedTasks_deleteCompletedTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed medications and 1 stub active medications in the repository
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description", true);
        mTasksRepository.saveMessage(newMedication);
        Medication newMedication2 = new Medication(TASK_TITLE2, "Some Medication Description");
        mTasksRepository.saveMessage(newMedication2);
        Medication newMedication3 = new Medication(TASK_TITLE3, "Some Medication Description", true);
        mTasksRepository.saveMessage(newMedication3);

        // When a completed medications are cleared to the medications repository
        mTasksRepository.clearCompletedMessages();


        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).clearCompletedMessages();
        verify(mTasksLocalDataSource).clearCompletedMessages();

        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertTrue(mTasksRepository.mCachedTasks.get(newMedication2.getId()).isActive());
        assertThat(mTasksRepository.mCachedTasks.get(newMedication2.getId()).getTitle(), is(TASK_TITLE2));
    }

    @Test
    public void deleteAllTasks_deleteTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed medications and 1 stub active medications in the repository
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description", true);
        mTasksRepository.saveMessage(newMedication);
        Medication newMedication2 = new Medication(TASK_TITLE2, "Some Medication Description");
        mTasksRepository.saveMessage(newMedication2);
        Medication newMedication3 = new Medication(TASK_TITLE3, "Some Medication Description", true);
        mTasksRepository.saveMessage(newMedication3);

        // When all medications are deleted to the medications repository
        mTasksRepository.deleteAllMessages();

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteAllMessages();
        verify(mTasksLocalDataSource).deleteAllMessages();

        assertThat(mTasksRepository.mCachedTasks.size(), is(0));
    }

    @Test
    public void deleteTask_deleteTaskToServiceAPIRemovedFromCache() {
        // Given a task in the repository
        Medication newMedication = new Medication(TASK_TITLE, "Some Medication Description", true);
        mTasksRepository.saveMessage(newMedication);
        assertThat(mTasksRepository.mCachedTasks.containsKey(newMedication.getId()), is(true));

        // When deleted
        mTasksRepository.deleteMessage(newMedication.getId());

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteMessage(newMedication.getId());
        verify(mTasksLocalDataSource).deleteMessage(newMedication.getId());

        // Verify it's removed from repository
        assertThat(mTasksRepository.mCachedTasks.containsKey(newMedication.getId()), is(false));
    }

    @Test
    public void getTasksWithDirtyCache_tasksAreRetrievedFromRemote() {
        // When calling getMessages in the repository with dirty cache
        mTasksRepository.refreshMessages();
        mTasksRepository.getMessages(mLoadMessagesCallback);

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, Medications);

        // Verify the medications from the remote data source are returned, not the local
        verify(mTasksLocalDataSource, never()).getMessages(mLoadMessagesCallback);
        verify(mLoadMessagesCallback).onMessagesLoaded(Medications);
    }

    @Test
    public void getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When calling getMessages in the repository
        mTasksRepository.getMessages(mLoadMessagesCallback);

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, Medications);

        // Verify the medications from the local data source are returned
        verify(mLoadMessagesCallback).onMessagesLoaded(Medications);
    }

    @Test
    public void getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getMessages in the repository
        mTasksRepository.getMessages(mLoadMessagesCallback);

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);

        // And the remote data source has no data available
        setTasksNotAvailable(mTasksRemoteDataSource);

        // Verify no data is returned
        verify(mLoadMessagesCallback).onDataNotAvailable();
    }

    @Test
    public void getTaskWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a task id
        final String taskId = "123";

        // When calling getMessage in the repository
        mTasksRepository.getMessage(taskId, mGetMedicationsCallback);

        // And the local data source has no data available
        setTaskNotAvailable(mTasksLocalDataSource, taskId);

        // And the remote data source has no data available
        setTaskNotAvailable(mTasksRemoteDataSource, taskId);

        // Verify no data is returned
        verify(mGetMedicationsCallback).onDataNotAvailable();
    }

    @Test
    public void getTasks_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mTasksRepository.refreshMessages();

        // When calling getMessages in the repository
        mTasksRepository.getMessages(mLoadMessagesCallback);

        // Make the remote data source return data
        setTasksAvailable(mTasksRemoteDataSource, Medications);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mTasksLocalDataSource, times(Medications.size())).saveMessage(any(Medication.class));
    }

    /**
     * Convenience method that issues two calls to the medications repository
     */
    private void twoTasksLoadCallsToRepository(MedicationsDataSource.LoadMessagesCallback callback) {
        // When medications are requested from repository
        mTasksRepository.getMessages(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mTasksLocalDataSource).getMessages(mTasksCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mTasksCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mTasksRemoteDataSource).getMessages(mTasksCallbackCaptor.capture());

        // Trigger callback so medications are cached
        mTasksCallbackCaptor.getValue().onMessagesLoaded(Medications);

        mTasksRepository.getMessages(callback); // Second call to API
    }

    private void setTasksNotAvailable(MedicationsDataSource dataSource) {
        verify(dataSource).getMessages(mTasksCallbackCaptor.capture());
        mTasksCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTasksAvailable(MedicationsDataSource dataSource, List<Medication> medications) {
        verify(dataSource).getMessages(mTasksCallbackCaptor.capture());
        mTasksCallbackCaptor.getValue().onMessagesLoaded(medications);
    }

    private void setTaskNotAvailable(MedicationsDataSource dataSource, String taskId) {
        verify(dataSource).getMessage(eq(taskId), mTaskCallbackCaptor.capture());
        mTaskCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTaskAvailable(MedicationsDataSource dataSource, Medication medication) {
        verify(dataSource).getMessage(eq(medication.getId()), mTaskCallbackCaptor.capture());
        mTaskCallbackCaptor.getValue().onMessagesLoaded(medication);
    }
}
