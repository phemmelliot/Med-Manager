package com.example.android.phemmelliot.phemmelliot.med_manager.addmedication;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.phemmelliot.phemmelliot.med_manager.R;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddMedicationFragment extends Fragment implements AddMedicationContract.View {

    public static final String ARGUMENT_EDIT_MEDICATION_ID = "EDIT_TASK_ID";

    private AddMedicationContract.Presenter mPresenter;

    private TextView mTitle;

    private TextView mDescription;

    private Spinner mFrequencySpinner;

    private EditText mStart, mEnd, mStartTime, mEndTime, mMidTime;

    private int startDay = 2000;

    private int startMonth = 2000;

    private int startYear = 2000;

    private int endDay = 2000;

    private int endMonth = 2000;

    private int endYear = 2000;

    private int startHour = 2000;

    private int startMinute = 2000;

    private int midHour = 2000;

    private int midMinute = 2000;

    private int endHour = 2000;

    private int endMinute = 2000;

    public static AddMedicationFragment newInstance() {
        return new AddMedicationFragment();
    }

    public AddMedicationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull AddMedicationContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab =
                 getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrequencySpinner.getSelectedItemPosition();
                Integer frequencyInt = mFrequencySpinner.getSelectedItemPosition();
                String frequency = frequencyInt.toString();
                String startDate = mStart.getText().toString();
                String endDate = mEnd.getText().toString();
                mPresenter.saveTask(mTitle.getText().toString(), mDescription.getText().toString(), frequency,
                        startDate,endDate, startDay, startMonth, startYear, endDay, endMonth, endYear, startHour,
                        startMinute, midHour, midMinute, endHour, endMinute);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addmedication_frag, container, false);
        mTitle =  root.findViewById(R.id.add_medication_name);
        mDescription =  root.findViewById(R.id.add_medication_description);
        mFrequencySpinner = root.findViewById(R.id.frequency_spinner);
        mStart = root.findViewById(R.id.start_et);
        mEnd = root.findViewById(R.id.end_et);
        mStartTime = root.findViewById(R.id.start_time_et);
        mEndTime = root.findViewById(R.id.end_time_et);
        mMidTime = root.findViewById(R.id.mid_time_et);

        mPresenter.setMyCalendarAndDate();
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickDate(getContext(), "start");
            }
        });

        mEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickDate(getContext(), "end");
            }
        });


        mFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    mEndTime.setVisibility(View.VISIBLE);
                    mMidTime.setVisibility(View.INVISIBLE);
                    clickStartTime(position);
                    clickEndTime(position);
                }else if(position == 2){
                    mEndTime.setVisibility(View.VISIBLE);
                    mMidTime.setVisibility(View.VISIBLE);
                    clickEndTime(position);
                    clickStartTime(position);
                    clickMidTime(position);
                }else if(position == 0){
                    mEndTime.setVisibility(View.INVISIBLE);
                    mMidTime.setVisibility(View.INVISIBLE);
                    clickStartTime(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setHasOptionsMenu(true);
        return root;
    }

    private void clickStartTime(final int position){
        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickTime(getContext(), "startTime", position);
            }
        });
    }

    private void clickEndTime(final int position){
                mEndTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPresenter.onClickTime(getContext(), "endTime", position);
                    }
                });
    }

    private void clickMidTime(final int position){
        mMidTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickTime(getContext(), "midTime", position);
            }
        });
    }

    @Override
    public void showEmptyMedicationError() {
        Snackbar.make(mTitle, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMedicationsList() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setDescription(String description) {
        mDescription.setText(description);
    }



    @Override
    public void setFrequency(String frequency) {
        int position = Integer.parseInt(frequency);
        mFrequencySpinner.setSelection(position);
    }

    @Override
    public void setStartTime(int startHour, int startMinute) {
//        Toast.makeText(getContext(), startHour, Toast.LENGTH_LONG).show();
        this.startHour = startHour;
        int hourToShow = hourOfDay(startHour);
        String morningOrEvening = timeOfDay(startHour);
        this.startMinute = startMinute;
        if(startMinute == 0)
            mStartTime.setText(String.format(Locale.US, "%d : %d0" + morningOrEvening, hourToShow, startMinute));
        else
            mStartTime.setText(String.format(Locale.US, "%d : %d" + morningOrEvening, hourToShow, startMinute));
//        if(startHour != 2000) {
//            if (startMinute == 0) {
//                String morningOrEvening = timeOfDay(startHour);
//                this.startHour = startHour;
//                this.startMinute = startMinute;
//                mStartTime.setText(String.format(Locale.US, "%d:%d0%s", startHour, startMinute, morningOrEvening));
//            }
//            else {
//                String morningOrEvening = timeOfDay(startHour);
//                this.startHour = startHour;
//                this.startMinute = startMinute;
//                mStartTime.setText(String.format(Locale.US, "%d:%d%s", startHour, startMinute, morningOrEvening));
//            }
//        }
    }

    @Override
    public void setMidTime(int midHour, int midMinute) {
      //  Toast.makeText(getContext(), midHour, Toast.LENGTH_LONG).show();
        this.midHour =  midHour;
        int hourToShow = hourOfDay(midHour);
        String morningOrEvening = timeOfDay(midHour);
        this.midMinute = midMinute;
        if(midMinute == 0)
            mMidTime.setText(String.format(Locale.US, "%d : %d0" + morningOrEvening, hourToShow, midMinute));
        else
            mMidTime.setText(String.format(Locale.US, "%d : %d" + morningOrEvening, hourToShow, midMinute));
    }

    @Override
    public void setEndTime(int endHour, int endMinute) {
       // Toast.makeText(getContext(), endHour, Toast.LENGTH_LONG).show();
        if(endHour != 2000) {
                this.endHour = endHour;
                int hourToShow = hourOfDay(endHour);
                String morningOrEvening = timeOfDay(endHour);
                this.endMinute = endMinute;
                if(endMinute == 0)
                    mEndTime.setText(String.format(Locale.US, "%d : %d0" + morningOrEvening, hourToShow, endMinute));
                else
                    mEndTime.setText(String.format(Locale.US, "%d : %d" + morningOrEvening, hourToShow, endMinute));
            }
    }

    @Override
    public int[] getStartDate() {
        String startDate = mStart.getText().toString();
        int[] date = {0,0,0};
        if(startDate.contentEquals("")){
            return date;
        }else{
            if(startDate.charAt(1) == '|'){
                date[0] = Integer.parseInt(String.valueOf(startDate.charAt(0)));
                if(startDate.charAt(3) == '|') {
                    date[1] = Integer.parseInt(String.valueOf(startDate.charAt(2)));
                    date[2] = Integer.parseInt(startDate.substring(4));
                }
                else if(startDate.charAt(4) == '|') {
                    date[1] = Integer.parseInt(startDate.substring(2, 4));
                    date[2] = Integer.parseInt(startDate.substring(5));
                }
            }else if(startDate.charAt(2) == '|'){
               date[0] = Integer.parseInt(startDate.substring(0,2));
                if(startDate.charAt(4) == '|') {
                    date[1] = Integer.parseInt(String.valueOf(startDate.charAt(3)));
                    date[2] = Integer.parseInt(startDate.substring(5));
                }
                else if(startDate.charAt(5) == '|') {
                    date[1] = Integer.parseInt(startDate.substring(3, 5));
                    date[2] = Integer.parseInt(startDate.substring(6));
                }
            }

            return date;
        }
    }

    @Override
    public int getStartTime() {
        if(mStartTime.getText().toString().equals("") || mStartTime.getText()==null) {
            return 2000;
        }
        else
            return (startHour * 60) + startMinute;
    }

    @Override
    public int getMidTime() {
        if(mMidTime.getText().toString().equals("") || mMidTime.getText() == null)
            return 2000;
        else
            return (midHour * 60) + midMinute;
    }

    @Override
    public void setStartDate(String startDate, int startDay, int startMonth, int startYear) {
        this.startDay = startDay;
        this.startMonth = startMonth;
        this.startYear = startYear;
        mStart.setText(startDate);
    }

    @Override
    public void setEndDate(String endDate, int endDay, int endMonth, int endYear) {
        this.endDay = endDay;
        this.endMonth = endMonth;
        this.endYear = endYear;
        mEnd.setText(endDate);
    }

    @Override
    public void showToast(String memberOfDate) {
        Toast.makeText(getContext(), memberOfDate + " can't be lower than current date", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showTimeToast() {
        Toast.makeText(getContext(), "Interval between intake should be atleast 8 hours", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showImpossibleDateToast(String s) {
        Toast.makeText(getContext(), s + " can't be lower than the start date", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setDate(String editText, int dayOfMonth, int month, int year) {
        if(editText.equals("start")) {
            startDay = dayOfMonth;
            startMonth = month;
            startYear = year;
            mStart.setText(String.format(Locale.US, "%d|%d|%d", dayOfMonth, month, year));
        }
        else if(editText.equals("end")) {
            endDay = dayOfMonth;
            endMonth = month;
            endYear = year;
            mEnd.setText(String.format(Locale.US, "%d|%d|%d", dayOfMonth, month, year));
        }
    }

    @Override
    public void setTime(int hour, int minute, String editText) {
        switch (editText) {
            case "startTime":
                startHour = hour;
                int hourToShow = hourOfDay(hour);
                String morningOrEvening = timeOfDay(hour);
                startMinute = minute;
                if(minute == 0)
                    mStartTime.setText(String.format(Locale.US, "%d : %d0" + morningOrEvening, hourToShow, minute));
                else
                    mStartTime.setText(String.format(Locale.US, "%d : %d" + morningOrEvening, hourToShow, minute));
                break;
            case "endTime":
                endHour = hour;
                hourToShow = hourOfDay(hour);
                morningOrEvening = timeOfDay(hour);
                endMinute = minute;
                if(minute == 0)
                    mEndTime.setText(String.format(Locale.US, "%d : %d0" + morningOrEvening, hourToShow, minute));
                else
                    mEndTime.setText(String.format(Locale.US, "%d : %d" + morningOrEvening, hourToShow, minute));
                break;
            case "midTime":
                midHour = hour;
                hourToShow = hourOfDay(hour);
                morningOrEvening = timeOfDay(hour);
                midMinute = minute;
                if(minute == 0)
                    mMidTime.setText(String.format(Locale.US, "%d : %d0" + morningOrEvening, hourToShow, minute));
                else
                    mMidTime.setText(String.format(Locale.US, "%d : %d" + morningOrEvening, hourToShow, minute));
                break;
        }

    }

    private String timeOfDay(int startHour) {
        String nightOrDay = "";
        if(startHour>=12 && startHour != 24) {
            nightOrDay = "PM";
        }
        else if(startHour <= 12 || startHour == 24)
            nightOrDay = "AM";

        return nightOrDay;
    }

    private int hourOfDay(int startHour){
        int hour = 0;
        if(startHour>=12 && startHour != 0) {
            hour = startHour - 12;
        }
        else if(startHour < 12 || startHour == 0) {
            if(startHour == 0)
                hour = 12;
            else
                hour = startHour;
        }

        return hour;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


}
