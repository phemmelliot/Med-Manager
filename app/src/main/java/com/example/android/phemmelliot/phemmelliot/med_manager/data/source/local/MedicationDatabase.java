/*
 * Copyright 2017, The Android Open Source Project
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

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.google.firebase.auth.FirebaseAuth;

/**
 * The Room Database that contains the Medication table.
 */
@Database(entities = {Medication.class}, version = 2)
public abstract class MedicationDatabase extends RoomDatabase {

    private static MedicationDatabase INSTANCE;

    public abstract MedicationsDao taskDao();

    private static final Object sLock = new Object();

    public static MedicationDatabase getInstance(Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId;
        if(mAuth.getCurrentUser() != null)
            userId = mAuth.getCurrentUser().getUid();
        else
            userId = "Medications.db";
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        MedicationDatabase.class, userId)
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return INSTANCE;
        }
    }

}
