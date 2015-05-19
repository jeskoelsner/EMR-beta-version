package org.zlwima.emurgency.mqtt.android.adapter.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableNewsObject implements Parcelable {
	private ParcelableNewsActor actor;
	private ParcelableNewsRessource object;
	private String published;
	private String verb;

	public ParcelableNewsActor getActor() {
		return actor;
	}

	public void setActor( ParcelableNewsActor parcelable ) {
		this.actor = parcelable;
	}

	public ParcelableNewsRessource getRessource() {
		return object;
	}

	public void setRessource( ParcelableNewsRessource ressource ) {
		this.object = ressource;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished( String published ) {
		this.published = published;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb( String verb ) {
		this.verb = verb;
	}

	/*
	 * Native constructor
	 */
	public ParcelableNewsObject( ParcelableNewsActor actor, ParcelableNewsRessource ressource, String published, String verb ) {
		this.actor = actor;
		this.object = ressource;
		this.published = published;
		this.verb = verb;
	}

	//reading from a parcel
	public ParcelableNewsObject( Parcel in ) {
		readFromParcel( in );
	}

	private void readFromParcel( Parcel in ) {
		actor = in.readParcelable( ParcelableNewsActor.class.getClassLoader() );
		object = in.readParcelable( ParcelableNewsRessource.class.getClassLoader() );
		setPublished( in.readString() );
		setVerb( in.readString() );
	}

	//implementation of a parcelable
	public int describeContents() {
		return 0;
	}

	public void writeToParcel( Parcel dest, int flags ) {
		dest.writeParcelable( getActor(), flags );
		dest.writeParcelable( getRessource(), flags );
		dest.writeString( getPublished() );
		dest.writeString( getVerb() );
	}
	//CREATOR instance
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public ParcelableNewsObject createFromParcel( Parcel in ) {
			return new ParcelableNewsObject( in );
		}

		public ParcelableNewsObject[] newArray( int size ) {
			return new ParcelableNewsObject[size];
		}
	};
}
