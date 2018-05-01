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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.AppExecutors;

import java.util.List;


/**
 * Concrete implementation of a data source as a db.
 */
public class MedicationsLocalDataSource implements MedicationsDataSource {

    private static volatile MedicationsLocalDataSource INSTANCE;

    private MedicationsDao mMedicationsDao;

    private AppExecutors mAppExecutors;

    // Prevent direct instantiation.
    private MedicationsLocalDataSource(@NonNull AppExecutors appExecutors,
                                       @NonNull MedicationsDao medicationsDao) {
        mAppExecutors = appExecutors;
        mMedicationsDao = medicationsDao;
    }

    public static MedicationsLocalDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                         @NonNull MedicationsDao medicationsDao) {
        if (INSTANCE == null) {
            synchronized (MedicationsLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MedicationsLocalDataSource(appExecutors, medicationsDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadMessagesCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getMessages(@NonNull final LoadMessagesCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Medication> medications = mMedicationsDao.getMedications();
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (medications.isEmpty()) {
                            // This will be called if the table is new or just empty.
                            callback.onDataNotAvailable();
                        } else {
                            callback.onMessagesLoaded(medications);
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Note: {@link GetMedicationsCallback#onDataNotAvailable()} is fired if the {@link Medication} isn't
     * found.
     */
    @Override
    public void getMessage(@NonNull final String messageId, @NonNull final GetMedicationsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Medication medication = mMedicationsDao.getMedicationById(messageId);

                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (medication != null) {
                            callback.onMessagesLoaded(medication);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveMessage(@NonNull final Medication medication) {
        checkNotNull(medication);
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mMedicationsDao.insertMedication(medication);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void completeMessage(@NonNull final Medication medication) {
        Runnable completeRunnable = new Runnable() {
            @Override
            public void run() {
                mMedicationsDao.updateCompleted(medication.getId(), true);
            }
        };

        mAppExecutors.diskIO().execute(completeRunnable);
    }

    @Override
    public void completeMessage(@NonNull String messageId) {
        // Not required for the local data source because the {@link MedicationsRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void activateMessage(@NonNull final Medication medication) {
        Runnable activateRunnable = new Runnable() {
            @Override
            public void run() {
                mMedicationsDao.updateCompleted(medication.getId(), false);
            }
        };
        mAppExecutors.diskIO().execute(activateRunnable);
    }

    @Override
    public void activateMessage(@NonNull String messageId) {
        // Not required for the local data source because the {@link MedicationsRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void clearCompletedMessages() {
        Runnable clearTasksRunnable = new Runnable() {
            @Override
            public void run() {
                mMedicationsDao.deleteCompletedMedications();

            }
        };

        mAppExecutors.diskIO().execute(clearTasksRunnable);
    }

    @Override
    public void refreshMessages() {
        // Not required because the {@link MedicationsRepository} handles the logic of refreshing the
        // medications from all the available data sources.
    }

    @Override
    public void deleteAllMessages() {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mMedicationsDao.deleteMedications();
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void deleteMessage(@NonNull final String messageId) {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mMedicationsDao.deleteMedicationById(messageId);
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
