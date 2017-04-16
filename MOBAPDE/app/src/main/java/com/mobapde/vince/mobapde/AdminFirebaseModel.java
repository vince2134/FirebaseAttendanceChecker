package com.mobapde.vince.mobapde;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Bryan on 4/9/2017.
 */

public class AdminFirebaseModel {

    //debugging constants
    private static final String TAG = "AdminFirebaseModel";

    //tags for shared preferences
    private static final String ADMIN_STORED_DATA_TAG = "admin stored data";

    //variables to be loaded from preferences
    private static StoredData adminStoredData;


    public static void saveState(Context context){
        SharedPreferences mPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(adminStoredData);
        prefsEditor.putString(ADMIN_STORED_DATA_TAG, json);
        prefsEditor.commit();
        Log.i(TAG, "Firebase.saveState() size of submittedDates is " + adminStoredData.getSubmittedDateSize());
    }

    public static void resumeState(Context context){

        SharedPreferences app_preferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        Gson gson = new Gson();
        String json = app_preferences.getString(ADMIN_STORED_DATA_TAG, null);
        adminStoredData = gson.fromJson(json, StoredData.class);
        Log.i("tagg", "SubmitManager.resumeState() dateModel is null? " + (adminStoredData == null));
    }

    public static void initialize(){
        //if today is not initialized, then initialize
        if(!adminStoredData.hasInitializedToDate(AdminStoredData.getDateToday())){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("AdminAttendance");

            //retrieve all attendance, create tables, and transfer' to those tables
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot attendanceSnapshot : dataSnapshot.getChildren()) {
                        Attendance a = attendanceSnapshot.getValue(Attendance.class);
                        a.addId("AdminAttendance",attendanceSnapshot.getKey());//----------------addddddd
                        Log.i(TAG, a.getRotationId() + ", "+a.getStatus() + " isPending? "+"PENDING".equalsIgnoreCase(a.getStatus()));
                        Log.i(TAG, attendanceSnapshot.getKey() + ", key");

                        //only add if the attendance is pending status
                        if("PENDING".equalsIgnoreCase(a.getStatus()))
                            addAttendance(a);
                    }
                }
                @Override public void onCancelled(DatabaseError databaseError) {}
            });
            adminStoredData.initialize();
        }
    }

    private static void addAttendance(Attendance a){
        List<String> combinationFilters = a.getCombinationFilters();

        if(combinationFilters == null){
            Log.i(TAG,"null combination filter");
            a.generateFilters();
            combinationFilters = a.getCombinationFilters();
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        for (String filter: combinationFilters) {
            Log.i(TAG, "addAttendance() combinationFilter: "+filter);

            //ensures only one unique attendance in filter
            if(!a.hasTableWithValue(filter)){
                DatabaseReference r = ref.child(filter).push();

                a.addId(filter, r.getKey());
                a.setAdminId(r.getKey());

                //add the attendance
                r.setValue(a);

                incrementCount(filter);
            }
        }
    }

    private static void incrementCount(String filter){
        DatabaseReference countref = FirebaseDatabase.getInstance().getReference().child("FilterCounts").child(filter).child("count");
        countref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);
                } else {
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean success, DataSnapshot dataSnapshot) {
                // Analyse databaseError for any error during increment
                Log.i(TAG, "AdminFirebaseModel incrementCount ERROR!!!!!");
            }
        });
    }

}
