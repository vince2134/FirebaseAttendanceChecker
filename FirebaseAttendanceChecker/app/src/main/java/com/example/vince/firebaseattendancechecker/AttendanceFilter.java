package com.example.vince.firebaseattendancechecker;

/**
 * Created by avggo on 11/12/2016.
 */

public class AttendanceFilter {
    private String building, RID, status;
    private long startMillis;
    private boolean done;
    private boolean submitted;
    private  int tab;


    public AttendanceFilter(){
        startMillis = 0;
        status = "";
        done = false;
        submitted = false;
        tab = 0;

    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public String getRID() {
        return RID;
    }

    public void setRID(String RID) {
        this.RID = RID;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getDone(){
        return done;
    }

    public void setDone(boolean b){
        this.done = b;
    }

    public boolean getSubmitted(){
        return submitted;
    }

    public void setSubmitted(boolean b){this.submitted = b;}

    public int getTab(){
        return tab;
    }

    public void setTab(int i){this.tab = i;}
}
