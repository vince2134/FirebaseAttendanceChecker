package com.mobapde.vince.mobapde;

/**
 * Created by avggo on 11/12/2016.
 */

public class AttendanceFilter {
    private String building, rotationId, status, filterString;
    private long startMillis;
    private  int tab;

    public AttendanceFilter(){
        startMillis = 0;
        status = "_";
        building = "_";
        rotationId = "_";
        tab = 0;
        updateFilterString();
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
        updateFilterString();
    }

    public String getRotationId() {
        return rotationId;
    }

    public void setRotationId(String rotationId) {
        this.rotationId = rotationId;
        updateFilterString();
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
        updateFilterString();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        updateFilterString();
    }

    public int getTab(){
        return tab;
    }

    public void setTab(int i){
        this.tab = i;

        if(i == 0)
            status = "PENDING";
        if(i == 1)
            status = "DONE";

        updateFilterString();
    }

    public String getFilterString() {
        return filterString;
    }

    private void updateFilterString(){
        String startMillisString = startMillis + "";

        if(startMillisString.equals("0"))
            startMillisString = "_";

        this.filterString = rotationId + "-" + status + "-" + building + "-" + startMillisString;
    }
}
