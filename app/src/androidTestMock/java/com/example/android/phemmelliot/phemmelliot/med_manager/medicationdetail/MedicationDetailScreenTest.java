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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.IsNot.not;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.phemmelliot.phemmelliot.med_manager.R;
import com.example.android.phemmelliot.phemmelliot.med_manager.TestUtils;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.FakeMedicationsRemoteDataSource;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsRepository;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the medications screen, the main screen which contains a list of all medications.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MedicationDetailScreenTest {

    private static String TASK_TITLE = "ATSL";

    private static String TASK_DESCRIPTION = "Rocks";

    /**
     * {@link Medication} stub that is added to the fake service API layer.
     */
    private static Medication activeMedication = new Medication(TASK_TITLE, TASK_DESCRIPTION, "3", "22/22/2016",
            "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0, false);

    /**
     * {@link Medication} stub that is added to the fake service API layer.
     */
    private static Medication completedMedication = new Medication(TASK_TITLE, TASK_DESCRIPTION, "3", "22/22/2016",
            "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0, true);

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     *
     * <p>
     * Sometimes an {@link Activity} requires a custom start {@link Intent} to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target Activity.
     */
    @Rule
    public ActivityTestRule<MedicationDetailActivity> mTaskDetailActivityTestRule =
            new ActivityTestRule<>(MedicationDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);


    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    private void loadActiveTask() {
        startActivityWithWithStubbedTask(activeMedication);
    }

    private void loadCompletedTask() {
        startActivityWithWithStubbedTask(completedMedication);
    }

    /**
     * Setup your test fixture with a fake medication id. The {@link MedicationDetailActivity} is started with
     * a particular medication id, which is then loaded from the service API.
     *
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    private void startActivityWithWithStubbedTask(Medication medication) {
        // Add a medication stub to the fake service api layer.
        MedicationsRepository.destroyInstance();
        FakeMedicationsRemoteDataSource.getInstance().addTasks(medication);

        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        Intent startIntent = new Intent();
        startIntent.putExtra(MedicationDetailActivity.EXTRA_TASK_ID, medication.getId());
        mTaskDetailActivityTestRule.launchActivity(startIntent);
    }

    @Test
    public void activeTaskDetails_DisplayedInUi() throws Exception {
        loadActiveTask();

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())));
    }

    @Test
    public void completedTaskDetails_DisplayedInUi() throws Exception {
        loadCompletedTask();

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.task_detail_complete)).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_menuAndTaskPersist() {
        loadActiveTask();

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));

        TestUtils.rotateOrientation(mTaskDetailActivityTestRule.getActivity());

        // Check that the task is shown
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

}
