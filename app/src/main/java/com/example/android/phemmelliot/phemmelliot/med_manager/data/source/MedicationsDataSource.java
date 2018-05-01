package com.example.android.phemmelliot.phemmelliot.med_manager.data.source;

import android.support.annotation.NonNull;

import com.example.android.phemmelliot.phemmelliot.med_manager.data.Medication;

import java.util.List;

/**
 * Main entry point for accessing messages data.
 * <p>
 * For simplicity, only getMessages() and getMessage() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new message is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface MedicationsDataSource {

    interface LoadMessagesCallback {

        void onMessagesLoaded(List<Medication> medications);

        void onDataNotAvailable();
    }

    interface GetMedicationsCallback {

        void onMessagesLoaded(Medication medication);

        void onDataNotAvailable();
    }

    void getMessages(@NonNull LoadMessagesCallback callback);

    void getMessage(@NonNull String messageId, @NonNull GetMedicationsCallback callback);

    void saveMessage(@NonNull Medication medication);

    void completeMessage(@NonNull Medication medication);

    void completeMessage(@NonNull String messageId);

    void activateMessage(@NonNull Medication medication);

    void activateMessage(@NonNull String messageId);

    void clearCompletedMessages();

    void refreshMessages();

    void deleteAllMessages();

    void deleteMessage(@NonNull String messageId);
}
