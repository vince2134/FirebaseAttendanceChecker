package com.mobapde.vince.mobapde;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Vince on 3/24/2017.
 */

public class AttendanceChecker extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
