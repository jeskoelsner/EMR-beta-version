package org.zlwima.emurgency.mqtt.android.adapter.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableNewsRessource implements Parcelable {
	private String id;
	private String author;
	private String displayName;
	private String url;

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor( String author ) {
		this.author = author;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName( String displayName ) {
		this.displayName = displayName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl( String url ) {
		this.url = url;
	}

	/*
	 * Native constructor
	 */
	public ParcelableNewsRessource( String id ) {
		this.id = id;
	}

	//reading from a parcel
	public ParcelableNewsRessource( Parcel in ) {
		readFromParcel( in );
	}

	private void readFromParcel( Parcel in ) {
		setId( in.readString() );
	}

	//implementation of a parcelable
	public int describeContents() {
		return 0;
	}

	public void writeToParcel( Parcel dest, int flags ) {
		dest.writeString( getId() );
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
