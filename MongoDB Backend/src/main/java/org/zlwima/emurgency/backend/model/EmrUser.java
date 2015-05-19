package org.zlwima.emurgency.backend.model;

import com.google.gson.Gson;
import java.util.Date;

public class EmrUser {
	private String email = "";
	private String password = "";
	private int level = -1;
	private String clientId = "";
	private String firstName = "";
	private String lastName = "";
	private String gender = "";
	private String mobilePhone = "";
	private String zipcode = "";
	private String city = "";
	private String street = "";
	private String country = "";
	private Date birthdate = null;
	private Date creationDate = null;
	private long notificationRadius = 1000;
	private Boolean receivesNotifications = true;
	private Boolean loginStatus = false;
	private EmrLocation location = new EmrLocation();
	private EmrLocation staticLocation = new EmrLocation();

        public void setLoginStatus(Boolean loginStatus) {
            this.loginStatus = loginStatus;
        }

        public Boolean isReceivesNotifications() {
            return receivesNotifications;
        }

        public Boolean isLoginStatus() {
            return loginStatus;
        }

	/*
	 * GETTERS AND SETTERS
	 */
	public boolean getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus( boolean loginStatus ) {
		this.loginStatus = loginStatus;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId( String clientId ) {
		this.clientId = clientId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName( String firstName ) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName( String lastName ) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender( String gender ) {
		this.gender = gender;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone( String mobilePhone ) {
		this.mobilePhone = mobilePhone;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet( String street ) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity( String city ) {
		this.city = city;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode( String zipcode ) {
		this.zipcode = zipcode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry( String country ) {
		this.country = country;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate( Date birthdate ) {
		this.birthdate = birthdate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate( Date creationDate ) {
		this.creationDate = creationDate;
	}

	public long getNotificationRadius() {
		return notificationRadius;
	}

	public void setNotificationRadius( long notificationRadius ) {
		this.notificationRadius = notificationRadius;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel( int level ) {
		this.level = level;
	}

	public Boolean getReceivesNotifications() {
		return receivesNotifications;
	}

	public void setReceivesNotifications( Boolean receivesNotifications ) {
		this.receivesNotifications = receivesNotifications;
	}

	public EmrLocation getLocation() {
		return location;
	}

	public void setLocation( EmrLocation location ) {
		this.location = location;
	}

	public EmrLocation getStaticLocation() {
		return staticLocation;
	}

	public void setStaticLocation( EmrLocation staticLocation ) {
		this.staticLocation = staticLocation;
	}

	@Override
	public String toString() {
		return "[firstName=" + getFirstName()
				+ ", lastName=" + getLastName()
				+ ", email=" + getEmail()
				+ ", loginStatus=" + getLoginStatus()
				+ ", password=" + getPassword()
				+ ", notificationRadius=" + getNotificationRadius()
				+ ", birthdate=" + getBirthdate()
				+ ", gender=" + getGender()
				+ ", mobilePhone=" + getMobilePhone()
				+ ", street=" + getStreet()
				+ ", city=" + getCity()
				+ ", zipcode=" + getZipcode()
				+ ", country=" + getCountry()
				+ ", creationDate=" + getCreationDate()
				+ ", level=" + getLevel()
				+ ", receivesNotifications=" + getReceivesNotifications()
				+ ", " + (location != null ? location.toString() : "(no location data)")
				+ ", " + (staticLocation != null ? staticLocation.toString() : "(no staticLocation data)")
				+ ", clientId=" + getClientId()
				+ "]";
	}

	public EmrUser() {
	}

	public String toJson() {
		return new Gson().toJson( this );
	}

}
