package com.mobapde.vince.mobapde;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Bryan on 4/1/2017.
 */

public class FirebaseUtils {

    private static boolean isInitialized = false;
    private static DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private static final String TAG = "FirebaseUtils";
    private static Attendance a2;
    private static ValueEventListener addAdmin = null;
    private static ValueEventListener submit = null;
    public static List<Attendance> allAttendance = new ArrayList<Attendance>();

    public static boolean canSubmit(){
        boolean canSubmit = true;
        long timeNow = Calendar.getInstance().getTimeInMillis();
        for(Attendance a:allAttendance){
            long classTime = a.getEndTime();
            if(timeNow <= classTime)
                canSubmit = false;
        }
        return canSubmit;
    }

    private static void addCount(String filter, final int change){
        DatabaseReference countref = ref.child("filterCounts").child(filter).child("count");
        countref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);
                } else {
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count + change);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean success, DataSnapshot dataSnapshot) {
                // Analyse databaseError for any error during increment
            }
        });
    }

    private static void addToList(Attendance a){
        boolean hasDuplicate = false;
        for(Attendance a1: allAttendance){
            if(a1.getAdminAttendanceId().equals(a.getAdminAttendanceId()))
                hasDuplicate = true;
        }
        if(!hasDuplicate){
            allAttendance.add(a);
        }
    }

    private static void removeFromList(Attendance a){
        for(int i = allAttendance.size()-1; i >= 0; i++){
            Attendance a1 = allAttendance.get(i);
            if(a1.getAdminAttendanceId().equals(a.getAdminAttendanceId())) {
                allAttendance.remove(i);
                break;
            }
        }
    }

    private static void updateFromList(Attendance a){
        for(int i = allAttendance.size()-1; i >= 0; i++){
            if(i < allAttendance.size()){
            Attendance a1 = allAttendance.get(i);
            if(a1.getAdminAttendanceId().equals(a.getAdminAttendanceId())) {
                allAttendance.set(i,a);
                break;
            }}
        }
    }

    public static void submit(){

        final DatabaseReference f = FirebaseDatabase.getInstance().getReference();

        Calendar calendar = Calendar.getInstance();

        //replace the current time by the time provided in the parameter
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        calendar.set(Calendar.MILLISECOND, 0);

        f.child("SubmittedDates").child(calendar.getTimeInMillis() + "");

        for(final Attendance a: allAttendance){
            a.setStatus("SUBMITTED");
            f.child("AdminAttendance").child(a.getAdminAttendanceId()).setValue(a);
            a.setCombinationFilters(null);

            List<String> combinationFilters = a.getCombinationFilters();
            if(combinationFilters == null){
                a.generateFilters();
                combinationFilters = a.getCombinationFilters();
            }
            for (final String filter: combinationFilters) {
                if(!"count".equals(filter) && "SUBMITTED".equalsIgnoreCase(filter.split("-")[1])){
                    Log.i(TAG,"going to add to filter `"+filter+"`");
                    DatabaseReference r = ref.child(filter).push();
                    a.addFirebaseId(r.getKey());
                    r.setValue(a,new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            addCount(filter,1);
                            updateFromList(a);
                        }
                    });

                }
            }
        }

        submit = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tableSnapshot : dataSnapshot.getChildren()) {
                    Log.d("WTF", tableSnapshot.getValue() + "");
                    String tableName = (String) tableSnapshot.getValue();
                    if(!"Building".equalsIgnoreCase(tableName) &&
                            !"Course".equalsIgnoreCase(tableName) &&
                            !"CourseOffering".equalsIgnoreCase(tableName) &&
                            !"Faculty".equalsIgnoreCase(tableName) &&
                            !"Room".equalsIgnoreCase(tableName) &&
                            !"Users".equalsIgnoreCase(tableName) &&
                            !"filterCounts".equalsIgnoreCase(tableName)&&
                            !"SUBMITTED".equalsIgnoreCase(tableName.split("-")[1])){
                        f.child(tableName).setValue(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.removeEventListener(addAdmin);
        f.addValueEventListener(submit);
    }

    public static void addAttendance(final Attendance a){
        List<String> combinationFilters = a.getCombinationFilters();
        if(combinationFilters == null){
            a.generateFilters();
            combinationFilters = a.getCombinationFilters();
        }
        for (final String filter: combinationFilters) {
            if(!"count".equals(filter)){
                Log.i(TAG,"going to add to filter `"+filter+"`");
                DatabaseReference r = ref.child(filter).push();
                a.addFirebaseId(r.getKey());
                r.setValue(a,new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        addCount(filter,1);
                        addToList(a);
                    }
                });

            }
        }
    }

    //only updates all attendances with same id. does not transfer them to tables
    public static void updateAttendanceByIdOnly(final Attendance updatedAttendance){
        Log.i(TAG,"FIREBASE update id only");
        final List<String> ids = updatedAttendance.getFirebaseIds();
        final List<String> combinationFilters = updatedAttendance.getCombinationFilters();

        DatabaseReference r = FirebaseDatabase.getInstance().getReference().child("AdminAttendance");
        updateFromList(updatedAttendance);
        String adminId = updatedAttendance.getAdminAttendanceId();
        r.child(adminId).setValue(updatedAttendance);

        final DatabaseReference r2 = FirebaseDatabase.getInstance().getReference();

        r2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(String filter: combinationFilters){
                    for (int i = ids.size() - 1; i >= 0; i--) {
                        String id = ids.get(i);
                        Log.i(TAG,"firebase id u[dTTE "+id);

                        //if (dataSnapshot.hasChild(filter)) {
                            for(DataSnapshot a:dataSnapshot.getChildren()){
                                if(a.getKey().equalsIgnoreCase(id))
                                    r2.child(filter).child(id).setValue(updatedAttendance);
                            }
                        //}
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static void updateAttendance(final Attendance updatedAttendance){
        Log.i(TAG,"FIREBASE attendance update adddelete ");
        List<String> ids = updatedAttendance.getFirebaseIds();
        List<String> combinationFilters = updatedAttendance.getCombinationFilters();

        DatabaseReference r = FirebaseDatabase.getInstance().getReference().child("AdminAttendance");
        updateFromList(updatedAttendance);

        for(int i = 0; i < ids.size();i++){
            String id = ids.get(i);
            Log.i(TAG, "before delete id: "+id);
        }
        for(String id:combinationFilters){
            Log.i(TAG, "before delete filters: "+id);
        }


        for(final String filter: combinationFilters){
            for (int i = ids.size() - 1; i >= 0; i--) {
                String id = ids.get(i);
                String adminId = updatedAttendance.getAdminAttendanceId();
                Log.i(TAG, id + ", "+adminId+", "+filter+"hahaha");
                r.child(adminId).setValue(updatedAttendance);

                DatabaseReference r1 = FirebaseDatabase.getInstance().getReference();
                r1.child(filter).child(id).setValue(null, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        addCount(filter,-1);
                    }
                });
            }
        }

        for(int i = ids.size()-1; i >= 0; i--){
            String id = ids.get(i);
            updatedAttendance.removeFirebaseId(id);
        }

        updatedAttendance.setCombinationFilters(null);
        addAttendance(updatedAttendance);
    }

    public static void initialize(){

        addAdmin = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot attendanceSnapshot : dataSnapshot.getChildren()) {
                    Log.i(TAG, attendanceSnapshot.getKey() + ", key");
                    Attendance a = attendanceSnapshot.getValue(Attendance.class);
                    Log.i(TAG, a.getRotationId() + ", "+a.getStatus());
                    a.setAdminAttendanceId(attendanceSnapshot.getKey());
                    Log.i(TAG, "hahaha" + a.getAdminAttendanceId());
                    if("PENDING".equalsIgnoreCase(a.getStatus()))
                        addAttendance(a);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference t = ref.child("AdminAttendance");

        Log.i(TAG,"firebase is initialized: "+isInitialized);
        if(!isInitialized){
            t.addValueEventListener(addAdmin);
        }
        isInitialized = true;

    }

}