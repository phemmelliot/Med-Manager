package com.example.android.phemmelliot.phemmelliot.med_manager.addmedication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddMedicationFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddMedicationPresenter implements AddMedicationContract.Presenter,
        MedicationsDataSource.GetMedicationsCallback {

    @NonNull
    private final MedicationsDataSource mMedicationsRepository;

    @NonNull
    private final AddMedicationContract.View mAddMedicationView;

    @Nullable
    private String mMedicationId;

    private boolean mIsDataMissing;


    private final Calendar myCalendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener date;

    private TimePickerDialog.OnTimeSetListener time;

    /**
     * Creates a presenter for the add/edit view.
     *
     * @param medicationId ID of the task to edit or null for a new task
     * @param medicationsRepository a repository of data for medications
     * @param addMedicationView the add/edit view
     * @param shouldLoadDataFromRepo whether data needs to be loaded or not (for config changes)
     */
    public AddMedicationPresenter(@Nullable String medicationId, @NonNull MedicationsDataSource medicationsRepository,
                                  @NonNull AddMedicationContract.View addMedicationView, boolean shouldLoadDataFromRepo) {
        mMedicationId = medicationId;
        mMedicationsRepository = checkNotNull(medicationsRepository);
        mAddMedicationView = checkNotNull(addMedicationView);
        mIsDataMissing = shouldLoadDataFromRepo;

        mAddMedicationView.setPresenter(this);
    }

    @Override
    public void start() {
        if (!isNewMedication() && mIsDataMissing) {
            populateMedication();
        }
    }

    @Override
    public void saveTask(String title, String description, String frequency, String start, String end) {
        if (isNewMedication()) {
            createMedication(title, description, frequency, start, end);
        } else {
            updateMedication(title, description, frequency, start, end);
        }
    }

    @Override
    public void populateMedication() {
        if (isNewMedication()) {
            throw new RuntimeException("populateMedication() was called but medication is new.");
        }
        mMedicationsRepository.getMessage(mMedicationId, this);
    }

    @Override
    public void setMyCalendarAndDate() {
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }

        };

        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);
            }
        };

    }

    @Override
    public void onClickDate(Context context, final String editText) {
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mAddMedicationView.setDate(editText, dayOfMonth, month, year);
            }
        }, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onClickTime(Context context, final String editText) {
        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mAddMedicationView.setTime(hourOfDay,minute,editText);
            }
        }, myCalendar
                .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false)
                .show();
    }

    @Override
    public void onMessagesLoaded(Medication medication) {
        // The view may not be able to handle UI updates anymore
        if (mAddMedicationView.isActive()) {
            mAddMedicationView.setTitle(medication.getTitle());
            mAddMedicationView.setDescription(medication.getDescription());
            mAddMedicationView.setFrequency(medication.getFrequency());
            mAddMedicationView.setStartDate(medication.getStart());
            mAddMedicationView.setEndDate(medication.getEnd());
        }
        mIsDataMissing = false;
    }

    @Override
    public void onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddMedicationView.isActive()) {
            mAddMedicationView.showEmptyMedicationError();
        }
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    private boolean isNewMedication() {
        return mMedicationId == null;
    }

    private void createMedication(String title, String description, String frequency, String start, String end) {
        Medication newMedication = new Medication(title, description, frequency, start, end);
        if (newMedication.isEmpty()) {
            mAddMedicationView.showEmptyMedicationError();
        } else {
            mMedicationsRepository.saveMessage(newMedication);
            mAddMedicationView.showMedicationsList();
        }
    }

    private void updateMedication(String title, String description, String frequency, String start, String end) {
        if (isNewMedication()) {
            throw new RuntimeException("updateMedication() was called but medication is new.");
        }
        mMedicationsRepository.saveMessage(new Medication(title, description, frequency, start, end, mMedicationId));
        mAddMedicationView.showMedicationsList(); // After an edit, go back to the list.
    }

}
