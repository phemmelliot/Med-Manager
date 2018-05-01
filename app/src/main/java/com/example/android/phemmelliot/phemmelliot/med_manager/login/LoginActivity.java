package com.example.android.phemmelliot.phemmelliot.med_manager.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.android.phemmelliot.phemmelliot.med_manager.R;
import com.example.android.phemmelliot.phemmelliot.med_manager.medications.MedicationsActivity;
import com.example.android.phemmelliot.phemmelliot.med_manager.profile.Profile;
import com.example.android.phemmelliot.phemmelliot.med_manager.profile.ProfileActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    public GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabaseRef;
    private Profile profile;
    private FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        frameLayout = findViewById(R.id.login_layout);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        sharedPreferences = getSharedPreferences(getString(R.string.preferenceKey), MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, MedicationsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void login(final View view) {
        if(isNetworkAvailable())
            googleSignIn();
        else {
            Snackbar.make(frameLayout, "No internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            login(view);
                        }
                    }).show();
        }
    }

    public void googleSignIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Signing in with google has failed, " +
                "please check your internet connection and try again.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...

            }
        }
    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        progressDialog.setMessage("Logging you in");
        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                             // String userId = mAuth.getCurrentUser().getUid();
                              checkIfUserExistsAndLogin();
//                              sharedPreferences.edit().putString("userId", userId).apply();
//                              Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
//                              startActivity(intent);
                        }else{
                            progressDialog.hide();
                            Toast.makeText(LoginActivity.this, "Task unsuccesful", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkIfUserExistsAndLogin() {
        if(mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            sharedPreferences.edit().putString("userId", userId).apply();
            mDatabaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                      profile = dataSnapshot.getValue(Profile.class);
                      if(profile != null){
                          Intent intent = new Intent(LoginActivity.this, MedicationsActivity.class);
                          startActivity(intent);
                      }else{
                          Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                          startActivity(intent);
                      }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.hide();
                    Toast.makeText(LoginActivity.this, "Try again", Toast.LENGTH_LONG).show();
                }
            });
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
