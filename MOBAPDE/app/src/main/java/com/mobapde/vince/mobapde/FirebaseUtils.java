package com.mobapde.vince.mobapde;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.List;

/**
 * Created by Bryan on 4/1/2017.
 */

public class FirebaseUtils {

    private static boolean isInitialized = false;
    private static DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private static final String TAG = "FirebaseUtils";
    private static Attendance a2;

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

    public static void addAttendance(Attendance a){
        List<String> combinationFilters = a.getCombinationFilters();
        if(combinationFilters == null){
            a.generateFilters();
            combinationFilters = a.getCombinationFilters();
        }
        for (String filter: combinationFilters) {
            if(!"count".equals(filter)){
                Log.i(TAG,"going to add to filter `"+filter+"`");
                DatabaseReference r = ref.child(filter).push();
                a.addFirebaseId(r.getKey());
                r.setValue(a);

                addCount(filter,1);
            }
        }
    }

    //only updates all attendances with same id. does not transfer them to tables
    public static void updateAttendanceByIdOnly(Attendance updatedAttendance){
        List<String> ids = updatedAttendance.getFirebaseIds();
        List<String> combinationFilters = updatedAttendance.getCombinationFilters();

        for(String filter: combinationFilters){
            for (int i = ids.size() - 1; i >= 0; i--) {
                String id = ids.get(i);

                ref.child(filter).child(id).setValue(updatedAttendance);
            }
        }
    }

    public static void updateAttendance(final Attendance updatedAttendance){
        List<String> ids = updatedAttendance.getFirebaseIds();
        List<String> combinationFilters = updatedAttendance.getCombinationFilters();

        for(String id:ids){
            Log.i(TAG, "before delete id: "+id);
        }
        for(String id:combinationFilters){
            Log.i(TAG, "before delete filters: "+id);
        }


        for(String filter: combinationFilters){
            for (int i = ids.size() - 1; i >= 0; i--) {
                String id = ids.get(i);

                ref.child(filter).child(id).setValue(null);
                addCount(filter,-1);
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

        Attendance a = new Attendance();
        a.setFacultyName("Rafogi Cabredo");
        a.setRoom("G211");
        a.setCourseCode("CSETHICS");
        a.setCourseName("Computer Science ethics");
        a.setStartTime(AttendanceUtils.replaceCurrentTime("12:45"));
        a.setEndTime(AttendanceUtils.replaceCurrentTime("14:15"));
        a.setCode(null); //??????????????? is the code a status?
        a.setEmail("rafael.cabredo@dlsu.edu.ph");
        a.setRemarks(null);
        a.setCollege("CCS");
        a.setReason(null);//???????????????? what is reason?
        a.setSubName(null);
        a.setNewRoom(null);
        a.setSubPic(null);
        a.setPic(null);
        a.setRotationId("A");
        a.setStatus("PENDING");
        a.setBuilding("GOKONGWEI");

        Attendance a1 = new Attendance();
        a1.setFacultyName("Antionio Contreras Duterte Lover");
        a1.setRoom("A903");
        a1.setCourseCode("SOCTEC1");
        a1.setCourseName("Society and Technology 1");
        a1.setStartTime(AttendanceUtils.replaceCurrentTime("9:15"));
        a1.setEndTime(AttendanceUtils.replaceCurrentTime("10:45"));
        a1.setCode(null); //??????????????? is the code a status?
        a1.setEmail("antonio.contreras+duterte_lover@dlsu.edu.ph");
        a1.setRemarks(null);
        a1.setCollege("CCS");
        a1.setReason(null);//???????????????? what is reason?
        a1.setSubName(null);
        a1.setNewRoom(null);
        a1.setSubPic(null);
        a1.setPic(null);
        a1.setRotationId("B");
        a1.setStatus("PENDING");
        a1.setBuilding("ANDREW");

        a2 = new Attendance();
        a2.setFacultyName("Cuteney Ngo");
        a2.setRoom("G301");
        a2.setCourseCode("MOBAPDE");
        a2.setCourseName("Mobile Application and Development");
        a2.setStartTime(AttendanceUtils.replaceCurrentTime("14:30"));
        a2.setEndTime(AttendanceUtils.replaceCurrentTime("17:45"));
        a2.setCode(null); //??????????????? is the code a status?
        a2.setEmail("courtney.ngo@dlsu.edu.ph");
        a2.setRemarks(null);
        a2.setCollege("CCS");
        a2.setReason(null);//???????????????? what is reason?
        a2.setSubName(null);
        a2.setNewRoom(null);
        a2.setSubPic(null);
        a2.setPic(null);
        a2.setRotationId("A");
        a2.setStatus("PENDING");
        a2.setBuilding("GOKONGWEI");

        ref = FirebaseDatabase.getInstance().getReference();

        Log.i(TAG,"firebase is initialized: "+isInitialized);
        if(!isInitialized){
            addAttendance(a);
            addAttendance(a1);
            addAttendance(a2);

            Log.i(TAG,"firebase added attendance");
        }
        isInitialized = true;

    }

    public static void testUpdate(){
        //update the a2 instance from FirebaseUtils.initialize()
        //Attendance a2 = new Attendance();
        a2.setFacultyName("Cuteney Ngo");
        a2.setRoom("G301");
        a2.setCourseCode("MOBAPDE");
        a2.setCourseName("Mobile Application and Development");
        a2.setStartTime(AttendanceUtils.replaceCurrentTime("14:30"));
        a2.setEndTime(AttendanceUtils.replaceCurrentTime("17:45"));
        a2.setCode(null); //??????????????? is the code a status?
        a2.setEmail("courtney.ngo@dlsu.edu.ph");
        a2.setRemarks(null);
        a2.setCollege("CCS");
        a2.setReason(null);//???????????????? what is reason?
        a2.setSubName(null);
        a2.setNewRoom(null);
        a2.setSubPic(null);
        a2.setPic(null);
        a2.setRotationId("D");
        a2.setStatus("SUBMITTED");
        a2.setBuilding("GOKONGWEI");

        updateAttendance(a2);
    }
}