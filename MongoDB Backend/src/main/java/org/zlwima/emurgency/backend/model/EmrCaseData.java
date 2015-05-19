package org.zlwima.emurgency.backend.model;

import com.google.gson.Gson;

import java.util.ArrayList;

public class EmrCaseData {
	private long caseStartTimeMillis = 0;
	private long caseRunningTimeMillis = 0;
	private long caseArrivedOnClientTimeMillis = 0;
	private long caseTimeOutValue = 0;
	private String caseId;
	private String caseAddress;
	private String caseNotes;
	private EmrLocation caseLocation = new EmrLocation();
	private ArrayList<EmrUser> notifiedUsers = new ArrayList<EmrUser>();
	private ArrayList<EmrVolunteer> volunteers = new ArrayList<EmrVolunteer>();
	private String caseInitializerId;
	/*
	 * GETTERS AND SETTERS
	 */
	public String getCaseInitializerId() {
		return caseInitializerId;
	}

	public void setCaseInitializerId( String caseInitializerId ) {
		this.caseInitializerId = caseInitializerId;
	}
	
	public long getCaseStartTimeMillis() {
		return caseStartTimeMillis;
	}

	public void setCaseStartTimeMillis( long caseStartTimeMillis ) {
		this.caseStartTimeMillis = caseStartTimeMillis;
	}

	public long getCaseRunningTimeMillis() {
		return caseRunningTimeMillis;
	}

	public void setCaseRunningTimeMillis( long caseRunningTimeMillis ) {
		this.caseRunningTimeMillis = caseRunningTimeMillis;
	}

	public long getCaseArrivedOnClientTimeMillis() {
		return caseArrivedOnClientTimeMillis;
	}

	public void setCaseArrivedOnClientTimeMillis( long caseArrivedOnClientTimeMillis ) {
		this.caseArrivedOnClientTimeMillis = caseArrivedOnClientTimeMillis;
	}

	public long getCaseTimeOutValue() {
		return caseTimeOutValue;
	}

	public void setCaseTimeOutValue( long caseTimeOutValue ) {
		this.caseTimeOutValue = caseTimeOutValue;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId( String id ) {
		this.caseId = id;
	}

	public String getCaseAddress() {
		return caseAddress;
	}

	public void setCaseAddress( String caseAddress ) {
		this.caseAddress = caseAddress;
	}

	public String getCaseNotes() {
		return caseNotes;
	}

	public void setCaseNotes( String caseNotes ) {
		this.caseNotes = caseNotes;
	}

	public EmrLocation getCaseLocation() {
		return caseLocation;
	}

	public void setCaseLocation( EmrLocation caseLocation ) {
		this.caseLocation = caseLocation;
	}

	public ArrayList<EmrVolunteer> getVolunteers() {
		return volunteers;
	}

	public void setVolunteers( ArrayList<EmrVolunteer> volunteers ) {
		this.volunteers = volunteers;
	}

	public ArrayList<EmrUser> getNotifiedUsers() {
		return notifiedUsers;
	}

	public void setNotifiedUsers( ArrayList<EmrUser> notifiedUsers ) {
		this.notifiedUsers = notifiedUsers;
	}

	/*
	 * returning dummy instance NOT INCLUDING notifiedUserList & websocketList
	 */
	public EmrCaseData getTrimmedCaseData() {
		EmrCaseData trimmed = new EmrCaseData();
		trimmed.setCaseStartTimeMillis( this.caseStartTimeMillis );
		trimmed.setCaseRunningTimeMillis( this.caseRunningTimeMillis );
		trimmed.setCaseId( this.caseId );
		trimmed.setCaseAddress( this.caseAddress );
		trimmed.setCaseLocation( this.caseLocation );
		trimmed.setVolunteers( this.volunteers );
		return trimmed;
	}

	/*
	 * Constructor definition... no param constructer not allowed for public access !
	 */
	private EmrCaseData() {
	}

	public EmrCaseData( String caseInitializerId, long caseStartTimeMillis, String caseAddress, String caseNotes, EmrLocation caseLocation ) {
		this.caseInitializerId = caseInitializerId;
		this.caseId = "C-" + caseStartTimeMillis;
		this.caseStartTimeMillis = caseStartTimeMillis;
		this.caseAddress = caseAddress;
		this.caseNotes = caseNotes;
		this.caseLocation = caseLocation;
	}
	
	public String toJson() {
		return new Gson().toJson( this );
	}	

}
