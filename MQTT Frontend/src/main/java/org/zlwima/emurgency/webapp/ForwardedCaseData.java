package org.zlwima.emurgency.webapp;

public class ForwardedCaseData {
	private String caseId;
	private double latitude;
	private double longitude;
	private String getCaseAddress;	
	private String notes;
	private int notified;

	public ForwardedCaseData( String caseId, double latitude, double longitude, String getCaseAddress, String notes, int notified) {
		this.caseId = caseId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.getCaseAddress = getCaseAddress;
		this.notes = notes;
		this.notified = notified;
	}

	/*
	 * GETTER AND SETTER
	 */
	
	public String getCaseId() {
		return caseId;
	}

	public void setCaseId( String caseId ) {
		this.caseId = caseId;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude( double latitude ) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude( double longitude ) {
		this.longitude = longitude;
	}

	public String getGetCaseAddress() {
		return getCaseAddress;
	}

	public void setGetCaseAddress( String getCaseAddress ) {
		this.getCaseAddress = getCaseAddress;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes( String notes ) {
		this.notes = notes;
	}

	public int getNotified() {
		return notified;
	}

	public void setNotified( int notified ) {
		this.notified = notified;
	}
	
}