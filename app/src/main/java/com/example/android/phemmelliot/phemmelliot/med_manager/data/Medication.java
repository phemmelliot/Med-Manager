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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.UUID;

/**
 * Immutable model class for a Medication.
 */
@Entity(tableName = "medications")
public final class Medication {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "entryid")
    private final String mId;

    @Nullable
    @ColumnInfo(name = "title")
    private final String mTitle;

    @Nullable
    @ColumnInfo(name = "description")
    private final String mDescription;

    @Nullable
    @ColumnInfo(name = "frequency")
    private final String mFrequency;

    @Nullable
    @ColumnInfo(name = "start")
    private final String mStart;

    @Nullable
    @ColumnInfo(name = "end")
    private final String mEnd;

    @ColumnInfo(name = "completed")
    private final boolean mCompleted;

    /**
     * Use this constructor to create a new active Medication.
     *
     * @param title       title of the medication
     * @param description description of the medication
     */
    @Ignore
    public Medication(@Nullable String title, @Nullable String description, @Nullable String frequency,
                      @Nullable String start, @Nullable String end) {
        this(title, description, frequency, start, end, UUID.randomUUID().toString(), false);
    }

    /**
     * Use this constructor to create an active Medication if the Medication already has an id (copy of another
     * Medication).
     *
     * @param title       title of the task
     * @param description description of the task
     * @param id          id of the task
     */
    @Ignore
    public Medication(@Nullable String title, @Nullable String description, @Nullable String frequency,
                      @Nullable String start, @Nullable String end, @NonNull String id) {
        this(title, description, frequency, start, end, id, false);
    }

    /**
     * Use this constructor to create a new completed Medication.
     *
     * @param title       title of the task
     * @param description description of the task
     * @param completed   true if the task is completed, false if it's active
     */
    @Ignore
    public Medication(@Nullable String title, @Nullable String description, @Nullable String frequency,
                      @Nullable String start, @Nullable String end, boolean completed) {
        this(title, description, frequency, start, end, UUID.randomUUID().toString(), completed);
    }

    /**
     * Use this constructor to specify a completed Medication if the Medication already has an id (copy of
     * another Medication).
     *
     * @param title       title of the medication
     * @param description description of the medication
     * @param id          id of the medication
     * @param completed   true if the medication is completed, false if it's active
     */
    public Medication(@Nullable String title, @Nullable String description, @Nullable String frequency,
                      @Nullable String start, @Nullable String end, @NonNull String id, boolean completed) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mFrequency = frequency;
        mStart = start;
        mEnd = end;
        mCompleted = completed;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getTitleForList() {
        if (!Strings.isNullOrEmpty(mTitle)) {
            return mTitle;
        } else {
            return mDescription;
        }
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    @Nullable
    public String getFrequency() {
        return mFrequency;
    }

    @Nullable
    public String getStart() {
        return mStart;
    }

    @Nullable
    public String getEnd() {
        return mEnd;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public boolean isActive() {
        return !mCompleted;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mTitle) &&
               Strings.isNullOrEmpty(mDescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medication medication = (Medication) o;
        return Objects.equal(mId, medication.mId) &&
               Objects.equal(mTitle, medication.mTitle) &&
               Objects.equal(mDescription, medication.mDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTitle, mDescription);
    }

    @Override
    public String toString() {
        return "Medication with title " + mTitle;
    }
}
