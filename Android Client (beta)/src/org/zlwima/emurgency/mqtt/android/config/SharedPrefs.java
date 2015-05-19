package org.zlwima.emurgency.mqtt.android.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefs {

	private static final String PREFS_NAME = "EMR_storage";
	// TODO outsource, declare for easy inspection
	private static final String EMAIL = "email";
	private static final String EMAIL_DEFAULT = "";
	private static final String PASSWORD = "pass";
	private static final String PASSWORD_DEFAULT = "";
	private static final String AUTOFILL = "autofill";
	private static final boolean AUTOFILL_DEFAULT = false;
        private static final String VOLUME = "vol";
	private static final int VOLUME_DEFAULT = 100;
        private static final String LENGTH = "len";
	private static final int LENGTH_DEFAULT = 2;
        private static final String SOUNDOPTION = "opt";
	private static final int SOUNDOPTION_DEFAULT = 0;
        
	private static SharedPreferences sharedPrefs;
	private static Context context;

	public SharedPrefs( Context context ) {
		SharedPrefs.context = context;
	}

	/**
	 * If SharedPreferences manager not set, create one
	 *
	 * @return SharedPreferences Manager
	 */
	private SharedPreferences getSharedData() {
		if( sharedPrefs == null ) {
			sharedPrefs = context.getSharedPreferences( PREFS_NAME,
					Context.MODE_PRIVATE );
			if( !getAutoFill() ) {
				resetPrefs();
			}
		}
		return sharedPrefs;
	}

	private void resetPrefs() {
		setEmail( EMAIL_DEFAULT );
	}

	/**
	 * Saves booleans and strings
	 *
	 * @param key
	 * @param value
	 * either string or boolean
	 */
	private void setValue( String key, Object value ) {
		Editor editor = getSharedData().edit();
		if( value instanceof String ) {
			editor.putString( key, (String) value );
		} else if( value instanceof Boolean ) {
			editor.putBoolean( key, (Boolean) value );
		} else if( value instanceof Integer ) {
			editor.putInt(key, (Integer) value );
		}
		editor.commit();
	}

	public String getEmail() {
		return getSharedData().getString( EMAIL, EMAIL_DEFAULT );
	}
	
	public String getPassword() {
		return getSharedData().getString( PASSWORD, PASSWORD_DEFAULT );
	}

	public Boolean getAutoFill() {
		return getSharedData().getBoolean( AUTOFILL, AUTOFILL_DEFAULT );
	}
        
        public Integer getVolume() {
		return getSharedData().getInt( VOLUME, VOLUME_DEFAULT );
	}
        
        public Integer getSoundLength() {
		return getSharedData().getInt(LENGTH, LENGTH_DEFAULT );
	}
        
        public Integer getSoundOption() {
		return getSharedData().getInt( SOUNDOPTION, SOUNDOPTION_DEFAULT );
	}

	public void setEmail( String value ) {
		setValue( EMAIL, value );
	}
	
	public void setPassword( String value) {
		setValue( PASSWORD, value );
	}

	public void setAutoFill( Boolean value ) {
		setValue( AUTOFILL, value );
	}
        
        public void setVolume( int volume ) {
		setValue( VOLUME, volume );
	}
        
        public void setSoundLength( int length ) {
		setValue( LENGTH, length );
	}
        
        public void setSoundOption( int option ) {
		setValue( SOUNDOPTION, option );
	}
}
