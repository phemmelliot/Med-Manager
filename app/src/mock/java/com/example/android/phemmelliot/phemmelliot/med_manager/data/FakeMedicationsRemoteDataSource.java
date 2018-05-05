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

package com.example.android.phemmelliot.phemmelliot.med_manager.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeMedicationsRemoteDataSource implements MedicationsDataSource {

    private static FakeMedicationsRemoteDataSource INSTANCE;

    private static final Map<String, Medication> TASKS_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeMedicationsRemoteDataSource() {}

    public static FakeMedicationsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeMedicationsRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void getMessages(@NonNull LoadMessagesCallback callback) {
        callback.onMessagesLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));
    }

    @Override
    public void getMessage(@NonNull String messageId, @NonNull GetMedicationsCallback callback) {
        Medication medication = TASKS_SERVICE_DATA.get(messageId);
        callback.onMessagesLoaded(medication);
    }

    @Override
    public void saveMessage(@NonNull Medication medication) {
        TASKS_SERVICE_DATA.put(medication.getId(), medication);
    }

    @Override
    public void completeMessage(@NonNull Medication medication) {
        Medication completedMedication = new Medication(medication.getTitle(), medication.getDescription(),medication.getFrequency(), medication.getStart(),
                medication.getEnd(), startDay, startMonth, startYear, endDay, endMonth, endYear, startHour, startMinute, midHour, midMinute, endHour, endMinute, medication.getId(), true);
        TASKS_SERVICE_DATA.put(medication.getId(), completedMedication);
    }

    @Override
    public void completeMessage(@NonNull String messageId) {
        // Not required for the remote data source.
    }

    @Override
    public void activateMessage(@NonNull Medication medication) {
        Medication activeMedication = new Medication(medication.getTitle(), medication.getDescription(), medication.getFrequency(),
                medication.getStart(), medication.getEnd(), startDay, startMonth, startYear, endDay, endMonth, endYear, startHour, startMinute, midhour, midMinute, endHour, endMinute, medication.getId());
        TASKS_SERVICE_DATA.put(medication.getId(), activeMedication);
    }

    @Override
    public void activateMessage(@NonNull String messageId) {
        // Not required for the remote data source.
    }

    @Override
    public void clearCompletedMessages() {
        Iterator<Map.Entry<String, Medication>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Medication> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    public void refreshMessages() {
        // Not required because the {@link MedicationsRepository} handles the logic of refreshing the
        // medications from all the available data sources.
    }

    @Override
    public void deleteMessage(@NonNull String messageId) {
        TASKS_SERVICE_DATA.remove(messageId);
    }

    @Override
    public void deleteAllMessages() {
        TASKS_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addTasks(Medication... medications) {
        for (Medication medication : medications) {
            TASKS_SERVICE_DATA.put(medication.getId(), medication);
        }
    }
}
