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

package com.example.android.phemmelliot.phemmelliot.med_manager.data.source.local;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Integration test for the {@link MedicationsDataSource}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksLocalDataSourceTest {

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private MedicationsLocalDataSource mLocalDataSource;

    private MedicationDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                MedicationDatabase.class)
                .build();
        MedicationsDao medicationsDao = mDatabase.taskDao();

        // Make sure that we're not keeping a reference to the wrong instance.
        MedicationsLocalDataSource.clearInstance();
        mLocalDataSource = MedicationsLocalDataSource.getInstance(new SingleExecutors(), medicationsDao);
    }

    @After
    public void cleanUp() {
        mDatabase.close();
        MedicationsLocalDataSource.clearInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveTask_retrievesTask() {
        // Given a new task
        final Medication newMedication = new Medication(TITLE, "");

        // When saved into the persistent repository
        mLocalDataSource.saveMessage(newMedication);

        // Then the task can be retrieved from the persistent repository
        mLocalDataSource.getMessage(newMedication.getId(), new MedicationsDataSource.GetMedicationsCallback() {
            @Override
            public void onMessagesLoaded(Medication medication) {
                assertThat(medication, is(newMedication));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Initialize mock for the callback.
        MedicationsDataSource.GetMedicationsCallback callback = mock(MedicationsDataSource.GetMedicationsCallback.class);
        // Given a new task in the persistent repository
        final Medication newMedication = new Medication(TITLE, "");
        mLocalDataSource.saveMessage(newMedication);

        // When completed in the persistent repository
        mLocalDataSource.completeMessage(newMedication);

        // Then the task can be retrieved from the persistent repository and is complete
        mLocalDataSource.getMessage(newMedication.getId(), new MedicationsDataSource.GetMedicationsCallback() {
            @Override
            public void onMessagesLoaded(Medication medication) {
                assertThat(medication, is(newMedication));
                assertThat(medication.isCompleted(), is(true));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void activateTask_retrievedTaskIsActive() {
        // Initialize mock for the callback.
        MedicationsDataSource.GetMedicationsCallback callback = mock(MedicationsDataSource.GetMedicationsCallback.class);

        // Given a new completed task in the persistent repository
        final Medication newMedication = new Medication(TITLE, "");
        mLocalDataSource.saveMessage(newMedication);
        mLocalDataSource.completeMessage(newMedication);

        // When activated in the persistent repository
        mLocalDataSource.activateMessage(newMedication);

        // Then the task can be retrieved from the persistent repository and is active
        mLocalDataSource.getMessage(newMedication.getId(), callback);

        verify(callback, never()).onDataNotAvailable();
        verify(callback).onMessagesLoaded(newMedication);

        assertThat(newMedication.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedTask_taskNotRetrievable() {
        // Initialize mocks for the callbacks.
        MedicationsDataSource.GetMedicationsCallback callback1 = mock(MedicationsDataSource.GetMedicationsCallback.class);
        MedicationsDataSource.GetMedicationsCallback callback2 = mock(MedicationsDataSource.GetMedicationsCallback.class);
        MedicationsDataSource.GetMedicationsCallback callback3 = mock(MedicationsDataSource.GetMedicationsCallback.class);

        // Given 2 new completed medications and 1 active task in the persistent repository
        final Medication newMedication1 = new Medication(TITLE, "");
        mLocalDataSource.saveMessage(newMedication1);
        mLocalDataSource.completeMessage(newMedication1);
        final Medication newMedication2 = new Medication(TITLE2, "");
        mLocalDataSource.saveMessage(newMedication2);
        mLocalDataSource.completeMessage(newMedication2);
        final Medication newMedication3 = new Medication(TITLE3, "");
        mLocalDataSource.saveMessage(newMedication3);

        // When completed medications are cleared in the repository
        mLocalDataSource.clearCompletedMessages();

        // Then the completed medications cannot be retrieved and the active one can
        mLocalDataSource.getMessage(newMedication1.getId(), callback1);

        verify(callback1).onDataNotAvailable();
        verify(callback1, never()).onMessagesLoaded(newMedication1);

        mLocalDataSource.getMessage(newMedication2.getId(), callback2);

        verify(callback2).onDataNotAvailable();
        verify(callback2, never()).onMessagesLoaded(newMedication2);

        mLocalDataSource.getMessage(newMedication3.getId(), callback3);

        verify(callback3, never()).onDataNotAvailable();
        verify(callback3).onMessagesLoaded(newMedication3);
    }

    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new task in the persistent repository and a mocked callback
        Medication newMedication = new Medication(TITLE, "");
        mLocalDataSource.saveMessage(newMedication);
        MedicationsDataSource.LoadMessagesCallback callback = mock(MedicationsDataSource.LoadMessagesCallback.class);

        // When all medications are deleted
        mLocalDataSource.deleteAllMessages();

        // Then the retrieved medications is an empty list
        mLocalDataSource.getMessages(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onMessagesLoaded(anyList());
    }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new medications in the persistent repository
        final Medication newMedication1 = new Medication(TITLE, "");
        mLocalDataSource.saveMessage(newMedication1);
        final Medication newMedication2 = new Medication(TITLE, "");
        mLocalDataSource.saveMessage(newMedication2);

        // Then the medications can be retrieved from the persistent repository
        mLocalDataSource.getMessages(new MedicationsDataSource.LoadMessagesCallback() {
            @Override
            public void onMessagesLoaded(List<Medication> medications) {
                assertNotNull(medications);
                assertTrue(medications.size() >= 2);

                boolean newTask1IdFound = false;
                boolean newTask2IdFound = false;
                for (Medication medication : medications) {
                    if (medication.getId().equals(newMedication1.getId())) {
                        newTask1IdFound = true;
                    }
                    if (medication.getId().equals(newMedication2.getId())) {
                        newTask2IdFound = true;
                    }
                }
                assertTrue(newTask1IdFound);
                assertTrue(newTask2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
