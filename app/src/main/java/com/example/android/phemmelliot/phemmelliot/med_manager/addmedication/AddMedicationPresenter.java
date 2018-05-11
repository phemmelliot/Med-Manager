package com.example.android.phemmelliot.phemmelliot.med_manager.addmedication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;
import com.example.android.phemmelliot.phemmelliot.med_manager.data.source.MedicationsDataSource;

import java.util.Calendar;

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

    private int todaysDay, todaysMonth, todaysYear;


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
    public void saveTask(String title, String description, String frequency, String start, String end, int startDay,
                         int startMonth, int startYear, int endDay, int endMonth, int endYear, int startHour, int startMinute,
                         int midHour, int midMinute, int endHour, int endMinute) {
        if (isNewMedication()) {
            createMedication(title, description, frequency, start, end, startDay, startMonth, startYear, endDay, endMonth, endYear, startHour, startMinute, midHour, midMinute, endHour, endMinute);
        } else {
            updateMedication(title, description, frequency, start, end, startDay, startMonth, startYear, endDay, endMonth, endYear, startHour, startMinute, midHour, midMinute, endHour, endMinute);
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
          todaysDay = myCalendar.get(Calendar.DAY_OF_MONTH);
          todaysMonth = myCalendar.get(Calendar.MONTH);
          todaysYear = myCalendar.get(Calendar.YEAR);

    }

    @Override
    public void onClickDate(final Context context, final String whichDate) {
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(year < todaysYear){
                    mAddMedicationView.showToast("Selected Year");
                }else if(month < todaysMonth){
                    mAddMedicationView.showToast("Selected Month");
                }else if(dayOfMonth < todaysDay){
                    mAddMedicationView.showToast("Selected Day");
                }else if(whichDate.equals("start")){
                    mAddMedicationView.setDate(whichDate, dayOfMonth, month, year);
                }else if(whichDate.equals("end")){
                    int[] date = mAddMedicationView.getStartDate();
                    int minDay = date[0];
                    int minMonth = date[1];
                    int minYear = date[2];
                    if(minDay != 0 && minMonth != 0 && minYear != 0){
                        if(year < minYear){
                            mAddMedicationView.showImpossibleDateToast("Selected Year");
                        }else if(month < minMonth){
                            mAddMedicationView.showImpossibleDateToast("Selected Month");
                        }else if(dayOfMonth < minDay) {
                            mAddMedicationView.showImpossibleDateToast("Selected Day");
                        }else
                            mAddMedicationView.setDate(whichDate, dayOfMonth, month, year);
                    }else
                        Toast.makeText(context, "Pick a starting date first", Toast.LENGTH_LONG).show();
                }
            }
        }, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onClickTime(final Context context, final String whichTime, final int position) {
        final int leastTimeDiff = 8 * 60;
        if (whichTime.equals("startTime")) {
            new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mAddMedicationView.setTime(hourOfDay, minute, whichTime);

                }
            }, myCalendar
                    .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false)
                    .show();


        } else if (whichTime.equals("midTime")) {
            final int startTime = mAddMedicationView.getStartTime();
            if (startTime == 2000) {
                Toast.makeText(context, "Pick time for morning intake first", Toast.LENGTH_LONG).show();
            } else {
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int totalSelectedTime = (hourOfDay * 60) + minute;
                        if (startTime + leastTimeDiff > totalSelectedTime) {
                            mAddMedicationView.showTimeToast();
                        } else if (startTime + leastTimeDiff <= totalSelectedTime) {
                            mAddMedicationView.setTime(hourOfDay, minute, whichTime);
                        }
                    }
                }, myCalendar
                        .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false)
                        .show();
            }


        } else if (whichTime.equals("endTime")) {
            final int midTime = mAddMedicationView.getMidTime();
            if (midTime == 2000 && position == 2) {
                Toast.makeText(context, "Pick time for afternoon intake first", Toast.LENGTH_LONG).show();
            } else if (midTime == 2000 && position == 1) {
                final int startTime = mAddMedicationView.getStartTime();
                if(startTime == 2000)
                    Toast.makeText(context, "Pick time for morning intake first", Toast.LENGTH_LONG).show();
                else{
                    new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            int totalSelectedTime = (hourOfDay * 60) + minute;
                            if (startTime + leastTimeDiff > totalSelectedTime) {
                                mAddMedicationView.showTimeToast();
                            } else if (startTime + leastTimeDiff < totalSelectedTime) {
                                mAddMedicationView.setTime(hourOfDay, minute, whichTime);

                            }
                        }
                    }, myCalendar
                            .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false)
                            .show();
                }

            } else {
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int totalSelectedTime = (hourOfDay * 60) + minute;
                        if (midTime + leastTimeDiff > totalSelectedTime) {
                            mAddMedicationView.showTimeToast();
                        } else if (midTime + leastTimeDiff < totalSelectedTime) {
                            mAddMedicationView.setTime(hourOfDay, minute, whichTime);

                        }
                    }
                }, myCalendar
                        .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false)
                        .show();
            }


        }
    }

    @Override
    public void onMessagesLoaded(Medication medication) {
        // The view may not be able to handle UI updates anymore
        if (mAddMedicationView.isActive()) {
            mAddMedicationView.setTitle(medication.getTitle());
            mAddMedicationView.setDescription(medication.getDescription());
            mAddMedicationView.setFrequency(medication.getFrequency());
            mAddMedicationView.setStartDate(medication.getStart(), medication.getStartDay(), medication.getStartMonth(), medication.getStartYear());
            mAddMedicationView.setEndDate(medication.getEnd(), medication.getEndDay(), medication.getEndMonth(), medication.getEndYear());
            mAddMedicationView.setStartTime(medication.getStartHour(), medication.getStartMinute());
            mAddMedicationView.setMidTime(medication.getMidHour(), medication.getMidMinute());
            mAddMedicationView.setEndTime(medication.getEndHour(), medication.getMidMinute());
            Log.d("This Activity", ""+medication.getStartHour() + ", " + medication.getEndHour() + ", " + medication.getMidHour());
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

    private void createMedication(String title, String description, String frequency, String start, String end, int startDay, int startMonth, int startYear, int endDay, int endMonth, int endYear, int startHour, int startMinute, int midHour, int midMinute, int endHour, int endMinute) {
        Medication newMedication = new Medication(title, description, frequency, start, end, startDay, startMonth, startYear, endDay, endMonth, endYear, startHour, startMinute, midHour, midMinute, endHour, endMinute);
        if (newMedication.isEmpty()) {
            mAddMedicationView.showEmptyMedicationError();
        } else {
            mMedicationsRepository.saveMessage(newMedication);
            mAddMedicationView.showMedicationsList();
        }
    }

    private void updateMedication(String title, String description, String frequency, String start, String end, int startDay, int startMonth, int startYear, int endDay, int endMonth, int endYear, int startHour, int startMinute, int midHour, int midMinute, int endHour, int endMinute) {
        if (isNewMedication()) {
            throw new RuntimeException("updateMedication() was called but medication is new.");
        }
        mMedicationsRepository.saveMessage(new Medication(title, description, frequency, start, end, startDay, startMonth, startYear, endDay, endMonth, endYear, startHour, startMinute, midHour, midMinute, endHour, endMinute, mMedicationId));
        mAddMedicationView.showMedicationsList(); // After an edit, go back to the list.
    }

}



