package com.example.android.phemmelliot.phemmelliot.med_manager.profile;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.phemmelliot.phemmelliot.med_manager.R;
import com.example.android.phemmelliot.phemmelliot.med_manager.medications.MedicationsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity{
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private EditText mFirstNameEditText, mLastNameEditText, mUserNameEditText;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setting up the views
        mFirstNameEditText = findViewById(R.id.first_name_et);
        mLastNameEditText = findViewById(R.id.last_name_et);
        mUserNameEditText = findViewById(R.id.user_name_et);
        Button mSetButton = findViewById(R.id.btn_set_up);

        // Setting up the coordinatorlayout for the snackbar
        coordinatorLayout = findViewById(R.id.profile_id);

        //setting up Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("users");

        //checking for users action with the editText
        mFirstNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    handled = true;
                    mLastNameEditText.requestFocus();
                }
                return handled;
            }
        });
        mLastNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    handled = true;
                    mUserNameEditText.requestFocus();
                }
                return handled;
            }
        });
        mUserNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    handled = true;
                }
                return handled;
            }
        });

        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfile();
            }
        });
    }

    private void setProfile(){
        String userId = mAuth.getCurrentUser().getUid();
        if(isComplete()) {
            if (isNetworkAvailable()) {
                Profile profile = new Profile(mFirstNameEditText.getText().toString(), mLastNameEditText.getText().toString(),
                        mUserNameEditText.getText().toString());
                mDatabaseRef.child(userId).setValue(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(ProfileActivity.this, MedicationsActivity.class);
                            startActivity(intent);
                            finish();
                        }else
                            Toast.makeText(ProfileActivity.this, "There was a problem, Try again", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else
                Snackbar.make(coordinatorLayout, "No internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setProfile();
                            }
                        }).show();
        }
    }
    private boolean isComplete(){
        boolean check = false;
        if(mFirstNameEditText.getText().toString().isEmpty())
            mFirstNameEditText.setError("Required");
        else if(mLastNameEditText.getText().toString().isEmpty())
            mLastNameEditText.setError("Required");
        else if(mUserNameEditText.getText().toString().isEmpty())
            mUserNameEditText.setError("Required");
        else
            check = true;

        return check;
    }
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        return activeInfo != null;

    }
}
