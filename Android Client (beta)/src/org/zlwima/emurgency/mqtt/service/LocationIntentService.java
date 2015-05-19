package org.zlwima.emurgency.mqtt.service;

import org.zlwima.emurgency.backend.model.EmrLocation;

import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;
import org.zlwima.emurgency.mqtt.android.config.Base;

public class LocationIntentService extends IntentService {

	public LocationIntentService() {
		super( "LocationIntentService" );
	}

	@Override
	protected void onHandleIntent( Intent intent ) {
            Location location = (Location) intent.getParcelableExtra( LocationClient.KEY_LOCATION_CHANGED );
            Base.log("--- Location has changed...");

            if( location != null ) {
                    APPLICATION.USER.setLocation(new EmrLocation( location.getLatitude(), location.getLongitude() ));
                    APPLICATION.updateLocation();

                    if(APPLICATION.missionActivity != null)
                        APPLICATION.missionActivity.updateMarker();
            }
	}

}
