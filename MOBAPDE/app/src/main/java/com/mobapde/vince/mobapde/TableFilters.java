package com.mobapde.vince.mobapde;

public class TableFilters{
	private String[] rotationids 	= {"A", "B", "C", "D", "E", "_"}; 				//6 default values
	private String[] statuses 		= {"PENDING","DONE", "_"};						//3 default values
	private String[] buildings		= {"GOKONGWEI","ANDREW","ALL_BUILDINGS", "_"};	//4 default values 
	private String[] startTimes		= {"_"};										//1 default value

	public TableFilters() {
	}

	public String[] getRotationids() {
		return rotationids;
	}

	public void setRotationids(String[] rotationids) {
		this.rotationids = rotationids;
	}

	public String[] getStatuses() {
		return statuses;
	}

	public void setStatuses(String[] statuses) {
		this.statuses = statuses;
	}

	public String[] getBuildings() {
		return buildings;
	}

	public void setBuildings(String[] buildings) {
		this.buildings = buildings;
	}

	public String[] getStartTimes() {
		return startTimes;
	}

	public void setStartTimes(String[] startTimes) {
		this.startTimes = startTimes;
	}
}