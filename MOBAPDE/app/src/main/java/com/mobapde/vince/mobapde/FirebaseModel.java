package com.mobapde.vince.mobapde;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 4/9/2017.
 */

public class FirebaseModel {
    //debugging constants
    private static final String TAG = "FirebaseModel";

    //tags for shared preferences
    private static final String STORED_DATA_TAG = "stored data";

    //variables to be loaded from preferences
    private static StoredData storedData;

    private static DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();

    public static StoredData getStoredData() {
        return storedData;
    }

    public static void saveState(Context context){
        SharedPreferences mPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(storedData);
        prefsEditor.putString(STORED_DATA_TAG, json);
        prefsEditor.commit();
        Log.i(TAG, "Firebase.saveState() size of all attendance is " + storedData.getAllAttendanceOfTheDay().size());
        Log.i(TAG, "Firebase.saveState() size of submittedDates is " + storedData.getSubmittedDateSize());
        Log.i(TAG, "Firebase.saveState() size of initializedDates is " + storedData.getInitializedDateSize());
    }

    public static void resumeState(Context context){

        SharedPreferences app_preferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        Gson gson = new Gson();
        String json = app_preferences.getString(STORED_DATA_TAG, null);
        storedData = gson.fromJson(json, StoredData.class);
        Log.i("tagg", "SubmitManager.resumeState() dateModel is null? " + (storedData == null));
        if(storedData == null)
            storedData = new StoredData();
    }


    public static void initialize(){
        //if today is not initialized, then initialize
        if(!storedData.hasInitializedToDate(StoredData.getDateToday())){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("AdminAttendance");
            //retrieve all attendance. For checker, load all attendance

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot attendanceSnapshot : dataSnapshot.getChildren()) {
                        Attendance a = attendanceSnapshot.getValue(Attendance.class);

                        a.setAdminId(attendanceSnapshot.getKey());
                        a.addId("AdminAttendance",attendanceSnapshot.getKey());

                        Log.i(TAG, a.getRotationId() + ", "+a.getStatus() + " isPending? "+"PENDING".equalsIgnoreCase(a.getStatus()));
                        Log.i(TAG, attendanceSnapshot.getKey() + ", key");
                        Log.i(TAG, "adminAttendance id: " + a.getAdminId() + " startTime: "+a.getStartTime());

                        //only add if the attendance is pending status
                        if("PENDING".equalsIgnoreCase(a.getStatus())){

                            a.generateFilters(); // to initialize the filters combinations
                            storedData.addAttendance(a);
                        }
                    }
                }
                @Override public void onCancelled(DatabaseError databaseError) {}
            });

            storedData.initialize();
        }
    }

    public static void updateAttendance(Attendance a){
        //get all filtercombination. don't regenerate yet as it will remove the old data
        List<String> filters = a.getCombinationFilters();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        //delete all
        for(String filter: filters){
            Log.d("UPDATEDATTENDANCEModel", filter + "FILTER");

            String id = a.getIdOfTable(filter);
            if(id != null){
            Log.d("UPDATEDATTENDANCEModel", id + "ID");
            ref.child(filter).child(id).setValue(null);
            a.removeId(filter);
            decrementCount(filter);}
        }

        //regenerate filters then add. don't forget to update the storedData
        filters = a.generateFilters();
        for(String filter: filters){
            DatabaseReference r = ref.child(filter).push();

            a.addId(filter, r.getKey());
            a.setAdminId(r.getKey());
            //add the attendance
            r.setValue(a);
            incrementCount(filter);
        }
        storedData.updateAttendance(a);
    }

    public static void submit(){
        if(!storedData.hasSubmittedToDate(StoredData.getDateToday())){
        List<Attendance> allAttendance = storedData.getAllAttendanceOfTheDay();
        //(1) update the attendance status to Submitted (for guarantee)
        //(2) update the attendance from the AdminAttendance table
        //(3) update the attendance to transfer
        //(4) then delete all filter tables except the submitted table

        for(Attendance a: allAttendance) {
            a.setStatus("SUBMITTED");// (1)
            String adminId = a.getAdminId();
            ref1.child("AdminAttendance").child(adminId).setValue(a); //(2)

            updateAttendance(a); // (3)
        }

        ref1.child("SubmittedDates").child(StoredData.getDateToday()+"").setValue("date");

        ValueEventListener submit = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange for submit. dataSnapshot key is: "+dataSnapshot.getKey());
                for (DataSnapshot tableSnapshot : dataSnapshot.getChildren()) {
                    Log.i(TAG, "onDataChange for submit. tableSnapshot key is: ");
                    //Log.d("WTF", tableSnapshot.getValue() + "");
                    Log.i(TAG, "key "+tableSnapshot.getKey()); //+ " value: " + tableSnapshot.getValue());
                    String tableName = tableSnapshot.getKey().toString();
                    String[] filters = tableName.split("-");

                    String statusFilter = (filters.length > 1) ? tableName.split("-")[1] : "SUBMITTED";
                    if(!"Building".equalsIgnoreCase(tableName) &&
                            !"Course".equalsIgnoreCase(tableName) &&
                            !"CourseOffering".equalsIgnoreCase(tableName) &&
                            !"Faculty".equalsIgnoreCase(tableName) &&
                            !"Room".equalsIgnoreCase(tableName) &&
                            !"Users".equalsIgnoreCase(tableName) &&
                            !"filterCounts".equalsIgnoreCase(tableName)&&
                            !"SUBMITTED".equalsIgnoreCase(statusFilter)){
                        ref1.child(tableName).setValue(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        DatabaseReference submitRef = FirebaseDatabase.getInstance().getReference();
        submitRef.addValueEventListener(submit);

            storedData.submit();
            storedData.resetForNewDay();
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

    private static void decrementCount(String filter){
        DatabaseReference countref = FirebaseDatabase.getInstance().getReference().child("FilterCounts").child(filter).child("count");
        countref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);
                } else {
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count - 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean success, DataSnapshot dataSnapshot) {
                // Analyse databaseError for any error during increment
                Log.i(TAG, "AdminFirebaseModel decrementCount ERROR!!!!!");
            }
        });
    }


}
