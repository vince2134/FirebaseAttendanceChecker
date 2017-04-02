package com.mobapde.vince.mobapde;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtils {

	/*public static void addAttendance(Attendance a){
		String filters = a.getAllFilters();

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(filters);
		DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("attendance");

		ref.push().setValue(a);
		ref1.push().setValue(a);
	}*/

	public static void generateTables(TableFilters t){
		String[] rotationIds = t.getRotationids();
		String[] statuses	= t.getStatuses();
		String[] buildings 	= t.getBuildings();
		String[] startTimes = t.getStartTimes();

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

		for(int i = 0; i < rotationIds.length; i++){
			for (int j = 0; j < statuses.length; j++) {
				for (int k = 0; k < buildings.length; k++) {
					for (int l = 0; l < startTimes.length; l++) {
						String tableName = rotationIds[i] + "-" + statuses[j] + "-" + buildings[k] + "-" + startTimes[l];

						Map<String, String> keyValue = new HashMap<String, String>();
                        keyValue.put("filters", tableName);

						ref.child(tableName).push().setValue(keyValue);
					}
				}
			}
		}
	}

	public static void dropTable(){

	}
}