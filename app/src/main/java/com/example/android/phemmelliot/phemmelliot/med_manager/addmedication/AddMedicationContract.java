package com.example.android.phemmelliot.phemmelliot.med_manager.addmedication;

import android.content.Context;

import com.example.android.phemmelliot.phemmelliot.med_manager.BasePresenter;
import com.example.android.phemmelliot.phemmelliot.med_manager.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface AddMedicationContract {

    interface View extends BaseView<Presenter> {

        void showEmptyMedicationError();

        void showMedicationsList();

        void setTitle(String title);

        void setDescription(String description);

        void setDate(String editText, int dayOfMonth, int month, int year);

        void setTime(int hour, int minute, String editText);

        void setFrequency(String frequency);

        void setStartDate(String startDate);

        void setEndDate(String endDate);

        boolean isActive();
    }

    interface Presenter extends BasePresenter {

        void saveTask(String title, String description, String frequency, String start, String end);

        void populateMedication();

        void setMyCalendarAndDate();

        void onClickDate(Context context, String editText);

        void onClickTime(Context context, String editText);

        boolean isDataMissing();
    }
}
