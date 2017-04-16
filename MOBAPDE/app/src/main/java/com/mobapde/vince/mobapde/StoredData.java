package com.mobapde.vince.mobapde;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 4/9/2017.
 */

public class StoredData {

    private static final String TAG = "stored data";

    //shared preferences
    private ArrayList<Long> submittedDates;
    private ArrayList<Long> initializedDates;
    private List<Attendance> allAttendanceOfTheDay; //it is not automatically verified if a day has passed. so manually empty it instead

    public StoredData(){
        submittedDates = new ArrayList<Long>();
        initializedDates = new ArrayList<Long>();
        allAttendanceOfTheDay = new ArrayList<Attendance>();
    }

    public void initialize(){
        addInitializedDate(getDateToday());
    }

    public void submit(){
        addSubmittedDate(getDateToday());
    }

    public int getSubmittedDateSize(){
        return submittedDates.size();
    }

    public boolean hasSubmittedToDate(long date){
        return submittedDates.contains(date);
    }

    public void addSubmittedDate(long date) {
        if (!submittedDates.contains(date)) {
            submittedDates.add(date);
        }
    }

    public int getInitializedDateSize(){
        return initializedDates.size();
    }

    public boolean hasInitializedToDate(long date){
        return initializedDates.contains(date);
    }

    public void addInitializedDate(long date) {
        if (!initializedDates.contains(date)) {
            initializedDates.add(date);
        }
    }

    public void resetForNewDay(){
        allAttendanceOfTheDay.clear();
    }

    public void addAttendance(Attendance a){
        boolean hasDuplicate = false;
        for(Attendance a1: allAttendanceOfTheDay){
            if(a1.sameAs(a))
                hasDuplicate = true;
        }
        if(!hasDuplicate){
            allAttendanceOfTheDay.add(a);
        }
    }

    public void removeAttendance(Attendance a){
        for(int i = allAttendanceOfTheDay.size()-1; i >= 0; i++){
            Attendance a1 = allAttendanceOfTheDay.get(i);
            if(a1.sameAs(a)) {
                allAttendanceOfTheDay.remove(i);
                break;
            }
        }
    }

    public void updateAttendance(Attendance a){
        for(int i = allAttendanceOfTheDay.size()-1; i >= 0 && i <= allAttendanceOfTheDay.size()-1; i++){
            Attendance a1 = allAttendanceOfTheDay.get(i);
            if(a1.sameAs(a)) {
                allAttendanceOfTheDay.set(i,a);
                break;
            }
        }
    }

    public List<Attendance> getAllAttendanceOfTheDay(){
        return allAttendanceOfTheDay;
    }


    public static long getDateToday(){
        //get current time
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

        Log.i(TAG, "getDateToday: "+calendar.getTimeInMillis());

        return calendar.getTimeInMillis();
    }

}
