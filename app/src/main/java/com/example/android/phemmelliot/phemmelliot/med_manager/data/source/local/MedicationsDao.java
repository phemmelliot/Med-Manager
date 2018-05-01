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

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;

import java.util.List;

/**
 * Data Access Object for the medications table.
 */
@Dao
public interface MedicationsDao {

    /**
     * Select all medications from the medications table.
     *
     * @return all medications.
     */
    @Query("SELECT * FROM Medications")
    List<Medication> getMedications();

    /**
     * Select a medication by id.
     *
     * @param medicationId the medication id.
     * @return the medication with medicationId.
     */
    @Query("SELECT * FROM Medications WHERE entryid = :medicationId")
    Medication getMedicationById(String medicationId);

    /**
     * Insert a medication in the database. If the medication already exists, replace it.
     *
     * @param medication the medication to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMedication(Medication medication);

    /**
     * Update a medication.
     *
     * @param medication medication to be updated
     * @return the number of medications updated. This should always be 1.
     */
    @Update
    int updateMedication(Medication medication);

    /**
     * Update the complete status of a medication
     *
     * @param medicationId    id of the medication
     * @param completed status to be updated
     */
    @Query("UPDATE Medications SET completed = :completed WHERE entryid = :medicationId")
    void updateCompleted(String medicationId, boolean completed);

    /**
     * Delete a medication by id.
     *
     * @return the number of medications deleted. This should always be 1.
     */
    @Query("DELETE FROM Medications WHERE entryid = :medicationId")
    int deleteMedicationById(String medicationId);

    /**
     * Delete all medications.
     */
    @Query("DELETE FROM Medications")
    void deleteMedications();

    /**
     * Delete all completed medications from the table.
     *
     * @return the number of medications deleted.
     */
    @Query("DELETE FROM Medications WHERE completed = 1")
    int deleteCompletedMedications();
}
