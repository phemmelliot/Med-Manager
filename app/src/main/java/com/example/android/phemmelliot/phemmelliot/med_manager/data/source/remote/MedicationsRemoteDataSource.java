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

package com.example.android.phemmelliot.phemmelliot.med_manager.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class MedicationsRemoteDataSource implements MedicationsDataSource {

    private static MedicationsRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Medication> TASKS_SERVICE_DATA;

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addMedication("Paracetamol", "Take two dosages three times a day", "1 per day","11/04/2018", "12/05/2018",3,4,2018,4,5,2018,5,5,0,0,0,0);
        addMedication("Panadol", "Take 4 pill per dosage 2 times a day", "1 per day","11/05/2018", "12/06/2018",4,5,2019,5,6,2019,6,6,0,0,0,0);
    }

    public static MedicationsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MedicationsRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private MedicationsRemoteDataSource() {}

    private static void addMedication(String title, String description, String frequency,  String start, String end, int startDay,
                                      int startMonth, int startYear, int endDay, int endMonth, int endYear, int startHour, int startMinute,
                                      int midHour, int midMinute, int endHour, int endMinute) {
        Medication newMedication = new Medication(title, description, frequency, start, end, startDay, startMonth, startYear, endDay, endMonth, endYear, startHour, startMinute, midHour, midMinute, endHour, endMinute);
        TASKS_SERVICE_DATA.put(newMedication.getId(), newMedication);
    }

    /**
     * Note: {@link LoadMessagesCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getMessages(final @NonNull LoadMessagesCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onMessagesLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    /**
     * Note: {@link GetMedicationsCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getMessage(@NonNull String messageId, final @NonNull GetMedicationsCallback callback) {
        final Medication medication = TASKS_SERVICE_DATA.get(messageId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onMessagesLoaded(medication);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void saveMessage(@NonNull Medication medication) {
        TASKS_SERVICE_DATA.put(medication.getId(), medication);
    }

    @Override
    public void completeMessage(@NonNull Medication medication) {
        Medication completedMedication = new Medication(medication.getTitle(), medication.getDescription(),
                medication.getFrequency(), medication.getStart(), medication.getEnd(),medication.getStartDay(), medication.getStartMonth(), medication.getStartYear(),
                medication.getEndDay(), medication.getEndMonth(), medication.getEndYear(), medication.getStartHour(),
                medication.getStartMinute(), medication.getMidHour(), medication.getMidMinute(), medication.getEndHour(),
                medication.getEndMinute(), medication.getId(), true);
        TASKS_SERVICE_DATA.put(medication.getId(), completedMedication);
    }

    @Override
    public void completeMessage(@NonNull String messageId) {
        // Not required for the remote data source because the {@link MedicationsRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void activateMessage(@NonNull Medication medication) {
        Medication activeMedication = new Medication(medication.getTitle(), medication.getDescription(),
                medication.getFrequency(), medication.getStart(), medication.getEnd(),medication.getStartDay(), medication.getStartMonth(), medication.getStartYear(),
                medication.getEndDay(), medication.getEndMonth(), medication.getEndYear(), medication.getStartHour(),
                medication.getStartMinute(), medication.getMidHour(), medication.getMidMinute(), medication.getEndHour(),
                medication.getEndMinute(), medication.getId());
        TASKS_SERVICE_DATA.put(medication.getId(), activeMedication);
    }

    @Override
    public void activateMessage(@NonNull String messageId) {
        // Not required for the remote data source because the {@link MedicationsRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
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

    @Override
    public void refreshMessages() {
        // Not required because the {@link MedicationsRepository} handles the logic of refreshing the
        // medications from all the available data sources.
    }

    @Override
    public void deleteAllMessages() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteMessage(@NonNull String messageId) {
        TASKS_SERVICE_DATA.remove(messageId);
    }
}
