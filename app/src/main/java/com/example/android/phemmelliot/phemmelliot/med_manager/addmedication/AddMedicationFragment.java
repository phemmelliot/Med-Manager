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
        if(startHour != 2000) {
            if (startMinute == 0)
                mStartTime.setText(String.format(Locale.US, "%d : %d0", startHour, startMinute));
            else
                mStartTime.setText(String.format(Locale.US, "%d : %d", startHour, startMinute));
        }
    }

    @Override
    public void setMidTime(int midHour, int midMinute) {
        if(midHour != 2000) {
            if (midMinute == 0)
                mMidTime.setText(String.format(Locale.US, "%d : %d0", midHour, midMinute));
            else
                mMidTime.setText(String.format(Locale.US, "%d : %d", midHour, midMinute));
        }
    }

    @Override
    public void setEndTime(int endHour, int endMinute) {
        if(endHour != 2000) {
            if (endMinute == 0)
                mEndTime.setText(String.format(Locale.US, "%d : %d0", endHour, endMinute));
            else
                mEndTime.setText(String.format(Locale.US, "%d : %d", endHour, endMinute));
        }
    }

    @Override
    public void setStartDate(String startDate) {
          mStart.setText(startDate);
    }

    @Override
    public void setEndDate(String endDate) {
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
            mStart.setText(String.format(Locale.US, "%d/%d/%d", dayOfMonth, month, year));
        }
        else if(editText.equals("end")) {
            endDay = dayOfMonth;
            endMonth = month;
            endYear = year;
            mEnd.setText(String.format(Locale.US, "%d/%d/%d", dayOfMonth, month, year));
        }
    }

    @Override
    public void setTime(int hour, int minute, String editText) {
        switch (editText) {
            case "startTime":
                startHour = hour;
                startMinute = minute;
                if(minute == 0)
                    mStartTime.setText(String.format(Locale.US, "%d : %d0", hour, minute));
                else
                    mStartTime.setText(String.format(Locale.US, "%d : %d", hour, minute));
                break;
            case "endTime":
                endHour = hour;
                endMinute = minute;
                if(minute == 0)
                    mEndTime.setText(String.format(Locale.US, "%d : %d0", hour, minute));
                else
                    mEndTime.setText(String.format(Locale.US, "%d : %d", hour, minute));
                break;
            case "midTime":
                midHour = hour;
                midMinute = minute;
                if(minute == 0)
                    mMidTime.setText(String.format(Locale.US, "%d : %d0", hour, minute));
                else
                    mMidTime.setText(String.format(Locale.US, "%d : %d", hour, minute));
                break;
        }

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


}
