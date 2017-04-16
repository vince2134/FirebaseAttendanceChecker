package com.mobapde.vince.mobapde;

import android.util.Log;

import com.mobapde.vince.mobapde.AttendanceUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vince on 3/22/2017.
 */

public class Attendance implements Serializable{

    public static final String ATTENDANCE = "ATTENDANCE_ITEM";
    public static final String ADMIN_TABLE = "name ng table";

    private String adminId;
    private HashMap<String, String> ids;
    private String  facultyName;
    private String  room;
    private String  courseCode;
    private String  courseName;
    private long    startTime;
    private long    endTime;
    private String  code;
    private String  email;
    private String  remarks;
    private String  college;
    private String  reason;
    public String   subName;
    private String  newRoom;
    private String  subPic;
    private String  pic;
    private String  date;
    private boolean isDone = false;
    //filters
    private String  rotationId;
    private String  status;
    private String  building;
    private long    startTimeFilter;
    //needed for firebase
    private List<String> combinationFilters;

    public Attendance() {
    }

    //hashmap is used to ensure no duplicates of this attendance in a table
    public void addId(String tableName, String id){
        if(ids == null)
            ids = new HashMap<String, String>();
        ids.put(tableName, id);
    }

    public void removeId(String tableName){
        ids.put(tableName,null);
    }

    public String getIdOfTable(String tableName){
        return ids.get(tableName);
    }

    public boolean hasTableWithValue(String tableName){
        boolean hasTable = false;

        if(ids == null)
            ids = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : ids.entrySet()) {
            String t = entry.getKey();
            String child = entry.getValue();
            if(t.equals(tableName) && child != null)
                hasTable = true;
        }
        return hasTable;
    }

    public boolean sameAs(Attendance a){
        return adminId.equals(a.getAdminId());
    }

    public List<String> generateFilters(){
        combinationFilters = AttendanceUtils.getCombinationFilters(this);
        return combinationFilters;
    }


    //---------------- GETTERS AND SETTERS -------------------------------------


    public String getAdminId() {
        if(ids.get("AdminAttendance") != null)
            adminId = ids.get("AdminAttendance");
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
        addId("AdminAttendance",adminId);
    }

    public HashMap<String, String> getIds() {
        return ids;
    }

    public void setIds(HashMap<String, String> ids) {
        this.ids = ids;
    }

    public Attendance(String name){
        this.facultyName = name;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getNewRoom() {
        return newRoom;
    }

    public void setNewRoom(String newRoom) {
        this.newRoom = newRoom;
    }

    public String getSubPic() {
        return subPic;
    }

    public void setSubPic(String subPic) {
        this.subPic = subPic;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getRotationId() {
        return rotationId;
    }

    public void setRotationId(String rotationId) {
        this.rotationId = rotationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public long getStartTimeFilter() {
        return startTimeFilter;
    }

    public void setStartTimeFilter(long startTimeFilter) {
        this.startTimeFilter = startTimeFilter;
    }

    public List<String> getCombinationFilters() {
        return combinationFilters;
    }

    public void setCombinationFilters(List<String> combinationFilters) {
        this.combinationFilters = combinationFilters;
    }
}
