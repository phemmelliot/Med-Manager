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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation to load medications from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class MedicationsRepository implements MedicationsDataSource {

    private static MedicationsRepository INSTANCE = null;

    private final MedicationsDataSource mMedicationsRemoteDataSource;

    private final MedicationsDataSource mMedicationsLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Medication> mCachedMedications;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private MedicationsRepository(@NonNull MedicationsDataSource tasksRemoteDataSource,
                                  @NonNull MedicationsDataSource tasksLocalDataSource) {
        mMedicationsRemoteDataSource = checkNotNull(tasksRemoteDataSource);
        mMedicationsLocalDataSource = checkNotNull(tasksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link MedicationsRepository} instance
     */
    public static MedicationsRepository getInstance(MedicationsDataSource tasksRemoteDataSource,
                                                    MedicationsDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new MedicationsRepository(tasksRemoteDataSource, tasksLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(MedicationsDataSource, MedicationsDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets medications from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadMessagesCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getMessages(@NonNull final LoadMessagesCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedMedications != null && !mCacheIsDirty) {
            callback.onMessagesLoaded(new ArrayList<>(mCachedMedications.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getMedicationsFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mMedicationsLocalDataSource.getMessages(new LoadMessagesCallback() {
                @Override
                public void onMessagesLoaded(List<Medication> medications) {
                    refreshCache(medications);
                    callback.onMessagesLoaded(new ArrayList<>(mCachedMedications.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getMedicationsFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveMessage(@NonNull Medication medication) {
        checkNotNull(medication);
        mMedicationsRemoteDataSource.saveMessage(medication);
        mMedicationsLocalDataSource.saveMessage(medication);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedMedications == null) {
            mCachedMedications = new LinkedHashMap<>();
        }
        mCachedMedications.put(medication.getId(), medication);
    }

    @Override
    public void completeMessage(@NonNull Medication medication) {
        checkNotNull(medication);
        mMedicationsRemoteDataSource.completeMessage(medication);
        mMedicationsLocalDataSource.completeMessage(medication);

        Medication completedMedication = new Medication(medication.getTitle(), medication.getDescription(),
                medication.getFrequency(), medication.getStart(),
                medication.getEnd(),medication.getmStartDay(), medication.getmStartMonth(), medication.getmStartYear(),
                medication.getmEndDay(), medication.getmEndMonth(), medication.getmEndYear(), medication.getmStartHour(),
                medication.getmStartMinute(), medication.getmMidHour(), medication.getmMidMinute(), medication.getmEndHour(),
                medication.getmEndMinute(), medication.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedMedications == null) {
            mCachedMedications = new LinkedHashMap<>();
        }
        mCachedMedications.put(medication.getId(), completedMedication);
    }

    @Override
    public void completeMessage(@NonNull String messageId) {
        checkNotNull(messageId);
        completeMessage(getMedicationWithId(messageId));
    }

    @Override
    public void activateMessage(@NonNull Medication medication) {
        checkNotNull(medication);
        mMedicationsRemoteDataSource.activateMessage(medication);
        mMedicationsLocalDataSource.activateMessage(medication);

        Medication activeMedication = new Medication(medication.getTitle(), medication.getDescription(),
                medication.getFrequency(), medication.getStart(),
                medication.getEnd(),medication.getmStartDay(), medication.getmStartMonth(), medication.getmStartYear(),
                medication.getmEndDay(), medication.getmEndMonth(), medication.getmEndYear(), medication.getmStartHour(),
                medication.getmStartMinute(), medication.getmMidHour(), medication.getmMidMinute(), medication.getmEndHour(),
                medication.getmEndMinute(), medication.getId());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedMedications == null) {
            mCachedMedications = new LinkedHashMap<>();
        }
        mCachedMedications.put(medication.getId(), activeMedication);
    }

    @Override
    public void activateMessage(@NonNull String messageId) {
        checkNotNull(messageId);
        activateMessage(getMedicationWithId(messageId));
    }

    @Override
    public void clearCompletedMessages() {
        mMedicationsRemoteDataSource.clearCompletedMessages();
        mMedicationsLocalDataSource.clearCompletedMessages();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedMedications == null) {
            mCachedMedications = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Medication>> it = mCachedMedications.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Medication> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    /**
     * Gets medications from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetMedicationsCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getMessage(@NonNull final String messageId, @NonNull final GetMedicationsCallback callback) {
        checkNotNull(messageId);
        checkNotNull(callback);

        Medication cachedMedication = getMedicationWithId(messageId);

        // Respond immediately with cache if available
        if (cachedMedication != null) {
            callback.onMessagesLoaded(cachedMedication);
            return;
        }

        // Load from server/persisted if needed.

        // Is the medication in the local data source? If not, query the network.
        mMedicationsLocalDataSource.getMessage(messageId, new GetMedicationsCallback() {
            @Override
            public void onMessagesLoaded(Medication medication) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedMedications == null) {
                    mCachedMedications = new LinkedHashMap<>();
                }
                mCachedMedications.put(medication.getId(), medication);
                callback.onMessagesLoaded(medication);
            }

            @Override
            public void onDataNotAvailable() {
                mMedicationsRemoteDataSource.getMessage(messageId, new GetMedicationsCallback() {
                    @Override
                    public void onMessagesLoaded(Medication medication) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedMedications == null) {
                            mCachedMedications = new LinkedHashMap<>();
                        }
                        mCachedMedications.put(medication.getId(), medication);
                        callback.onMessagesLoaded(medication);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshMessages() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllMessages() {
        mMedicationsRemoteDataSource.deleteAllMessages();
        mMedicationsLocalDataSource.deleteAllMessages();

        if (mCachedMedications == null) {
            mCachedMedications = new LinkedHashMap<>();
        }
        mCachedMedications.clear();
    }

    @Override
    public void deleteMessage(@NonNull String messageId) {
        mMedicationsRemoteDataSource.deleteMessage(checkNotNull(messageId));
        mMedicationsLocalDataSource.deleteMessage(checkNotNull(messageId));

        mCachedMedications.remove(messageId);
    }

    private void getMedicationsFromRemoteDataSource(@NonNull final LoadMessagesCallback callback) {
        mMedicationsRemoteDataSource.getMessages(new LoadMessagesCallback() {
            @Override
            public void onMessagesLoaded(List<Medication> medications) {
                refreshCache(medications);
                refreshLocalDataSource(medications);
                callback.onMessagesLoaded(new ArrayList<>(mCachedMedications.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Medication> medications) {
        if (mCachedMedications == null) {
            mCachedMedications = new LinkedHashMap<>();
        }
        mCachedMedications.clear();
        for (Medication medication : medications) {
            mCachedMedications.put(medication.getId(), medication);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Medication> medications) {
        mMedicationsLocalDataSource.deleteAllMessages();
        for (Medication medication : medications) {
            mMedicationsLocalDataSource.saveMessage(medication);
        }
    }

    @Nullable
    private Medication getMedicationWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedMedications == null || mCachedMedications.isEmpty()) {
            return null;
        } else {
            return mCachedMedications.get(id);
        }
    }
}
