

package com.example.android.phemmelliot.phemmelliot.med_manager.data.source.local;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MedicationsDaoTest {

    private static final Medication MEDICATION = new Medication("title", "description", "3", "22/22/2016",
            "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0,"id", true);

    private MedicationDatabase mDatabase;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                MedicationDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertTaskAndGetById() {
        // When inserting a task
        mDatabase.taskDao().insertMedication(MEDICATION);

        // When getting the task by id from the database
        Medication loaded = mDatabase.taskDao().getMedicationById(MEDICATION.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title", "description", true);
    }

    @Test
    public void insertTaskReplacesOnConflict() {
        //Given that a task is inserted
        mDatabase.taskDao().insertMedication(MEDICATION);

        // When a task with the same id is inserted
        Medication newMedication = new Medication("title2", "description2", "3", "22/22/2016",
                "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0,"id",true);
        mDatabase.taskDao().insertMedication(newMedication);
        // When getting the task by id from the database
        Medication loaded = mDatabase.taskDao().getMedicationById(MEDICATION.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void insertTaskAndGetTasks() {
        // When inserting a task
        mDatabase.taskDao().insertMedication(MEDICATION);

        // When getting the medications from the database
        List<Medication> medications = mDatabase.taskDao().getMedications();

        // There is only 1 task in the database
        assertThat(medications.size(), is(1));
        // The loaded data contains the expected values
        assertTask(medications.get(0), "id", "title", "description", true);
    }

    @Test
    public void updateTaskAndGetById() {
        // When inserting a task
        mDatabase.taskDao().insertMedication(MEDICATION);

        // When the task is updated
        Medication updatedMedication = new Medication("title2", "description2", "3", "22/22/2016",
                "34/35/2017",3,4,2018,4,5,2018,5,5,0,0,0,0,"id",true);
        mDatabase.taskDao().updateMedication(updatedMedication);

        // When getting the task by id from the database
        Medication loaded = mDatabase.taskDao().getMedicationById("id");

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void updateCompletedAndGetById() {
        // When inserting a task
        mDatabase.taskDao().insertMedication(MEDICATION);

        // When the task is updated
        mDatabase.taskDao().updateCompleted(MEDICATION.getId(), false);

        // When getting the task by id from the database
        Medication loaded = mDatabase.taskDao().getMedicationById("id");

        // The loaded data contains the expected values
        assertTask(loaded, MEDICATION.getId(), MEDICATION.getTitle(), MEDICATION.getDescription(), false);
    }

    @Test
    public void deleteTaskByIdAndGettingTasks() {
        //Given a task inserted
        mDatabase.taskDao().insertMedication(MEDICATION);

        //When deleting a task by id
        mDatabase.taskDao().deleteMedicationById(MEDICATION.getId());

        //When getting the medications
        List<Medication> medications = mDatabase.taskDao().getMedications();
        // The list is empty
        assertThat(medications.size(), is(0));
    }

    @Test
    public void deleteTasksAndGettingTasks() {
        //Given a task inserted
        mDatabase.taskDao().insertMedication(MEDICATION);

        //When deleting all medications
        mDatabase.taskDao().deleteMedications();

        //When getting the medications
        List<Medication> medications = mDatabase.taskDao().getMedications();
        // The list is empty
        assertThat(medications.size(), is(0));
    }

    @Test
    public void deleteCompletedTasksAndGettingTasks() {
        //Given a completed task inserted
        mDatabase.taskDao().insertMedication(MEDICATION);

        //When deleting completed medications
        mDatabase.taskDao().deleteCompletedMedications();

        //When getting the medications
        List<Medication> medications = mDatabase.taskDao().getMedications();
        // The list is empty
        assertThat(medications.size(), is(0));
    }

    private void assertTask(Medication medication, String id, String title,
                            String description, boolean completed) {
        assertThat(medication, notNullValue());
        assertThat(medication.getId(), is(id));
        assertThat(medication.getTitle(), is(title));
        assertThat(medication.getDescription(), is(description));
        assertThat(medication.isCompleted(), is(completed));
    }
}
