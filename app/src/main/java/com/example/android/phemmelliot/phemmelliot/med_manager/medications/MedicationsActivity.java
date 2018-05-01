/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.phemmelliot.phemmelliot.med_manager.medications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.IdlingResource;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.phemmelliot.phemmelliot.med_manager.Injection;
import com.example.android.phemmelliot.phemmelliot.med_manager.R;
import com.example.android.phemmelliot.phemmelliot.med_manager.profile.Profile;
import com.example.android.phemmelliot.phemmelliot.med_manager.statistics.StatisticsActivity;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.ActivityUtils;
import com.example.android.phemmelliot.phemmelliot.med_manager.util.EspressoIdlingResource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MedicationsActivity extends AppCompatActivity {

    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";

    private DrawerLayout mDrawerLayout;

    private MedicationsPresenter mMedicationsPresenter;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseRef;

    private TextView mNameTV, mHandleTV;

    private SharedPreferences sharedPreferences;
    private Profile profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medications_act);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.transparent);
        NavigationView navigationView =  findViewById(R.id.nav_view);
        mNameTV = navigationView.getHeaderView(0).findViewById(R.id.name_tv);
        mHandleTV = navigationView.getHeaderView(0).findViewById(R.id.handle_tv);

        loadNavHeader();

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        MedicationsFragment medicationsFragment =
                (MedicationsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (medicationsFragment == null) {
            // Create the fragment
            medicationsFragment = MedicationsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), medicationsFragment, R.id.contentFrame);
        }

        // Create the presenter
        mMedicationsPresenter = new MedicationsPresenter(
                Injection.provideTasksRepository(getApplicationContext()), medicationsFragment);

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
            MedicationsFilterType currentFiltering =
                    (MedicationsFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mMedicationsPresenter.setFiltering(currentFiltering);
        }

        sharedPreferences = this.getSharedPreferences(getString(R.string.preferenceKey), Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(getString(R.string.nameKey), "MT");
        String handle = sharedPreferences.getString(getString(R.string.handleKey), "@medtracker");

        mNameTV.setText(name);
        mHandleTV.setText(handle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_KEY, mMedicationsPresenter.getFiltering());

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.statistics_navigation_menu_item:
                                Intent intent =
                                        new Intent(MedicationsActivity.this, StatisticsActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void loadNavHeader() {
        if(mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();
            mDatabaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //VendorProfile is null as far as I'm concerned.
                    profile = dataSnapshot.getValue(Profile.class);
                    if (profile != null) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Character first = profile.getFirstName().charAt(0);
                        Character second = profile.getLastName().charAt(0);
                        String firstString = first.toString();
                        String secondString = second.toString();
                        String name = firstString + secondString;
                        editor.putString(getString(R.string.nameKey), name);
                        editor.putString(getString(R.string.handleKey), "@"+profile.getUserName());
                        editor.apply();
                        mNameTV.setText(name);
                        mHandleTV.setText(String.format("@%s", profile.getUserName()));
                    } else
                        Toast.makeText(MedicationsActivity.this, "This user does not have a profile " + userId, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(getClass().getSimpleName(), "loadPost:onCancelled", databaseError.toException());
                    Toast.makeText(MedicationsActivity.this, "Access to database denied", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
