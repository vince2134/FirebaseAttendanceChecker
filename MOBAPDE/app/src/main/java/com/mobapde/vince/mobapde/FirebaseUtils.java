package com.mobapde.vince.mobapde;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 4/1/2017.
 */

public class FirebaseUtils {

    private static boolean isInitialized = false;
    private static DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private static final String TAG = "FirebaseUtils";
    private static Attendance a2;

    public static void addAttendance(Attendance a){
        List<String> combinationFilters = a.getCombinationFilters();
        if(combinationFilters == null){
            a.generateFilters();
            combinationFilters = a.getCombinationFilters();
        }
        for (String filter: combinationFilters) {
            DatabaseReference r = ref.child(filter).push();
            a.addFirebaseId(r.getKey());
            r.setValue(a);
        }
    }

    public static void updateAttendance(final Attendance updatedAttendance){
        List<String> ids = updatedAttendance.getFirebaseIds();
        List<String> combinationFilters = updatedAttendance.getCombinationFilters();

        for(String filter: combinationFilters){
            for(int i = ids.size()-1; i >= 0; i--){
                String id = ids.get(i);
                ref.child(filter).child(id).setValue(null);
                updatedAttendance.removeFirebaseId(id);
            }
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