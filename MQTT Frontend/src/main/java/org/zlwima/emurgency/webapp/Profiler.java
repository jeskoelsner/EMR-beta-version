package org.zlwima.emurgency.webapp;

import java.util.List;
import org.zlwima.emurgency.backend.Backend;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.model.EmrCaseData;
import org.zlwima.emurgency.backend.model.EmrUser;

public class Profiler {
	private static Profiler INSTANCE = null;

	// prevent instantiation
	private Profiler() {
	}

	// singleton implementation
	public static Profiler getProfiler() {
		if( INSTANCE == null ) {
			INSTANCE = new Profiler();
		}
		return INSTANCE;
	}

	public void setVolunteersByRadius( EmrCaseData caseData ) {
		caseData.getNotifiedUsers().clear();

		double latitude = caseData.getCaseLocation().getLatitude();
		double longitude = caseData.getCaseLocation().getLongitude();

		for( EmrUser aUser : Backend.getInstance().getLogedInUsers() ) {
			int distance = (int) Shared.calculateDistance(
					latitude, longitude, aUser.getLocation().getLatitude(), aUser.getLocation().getLongitude() );

			if( distance < aUser.getNotificationRadius() ) {
				System.out.println( "ALARMING " + aUser.getClientId() + " with distance " + distance );
				caseData.getNotifiedUsers().add( aUser );
			} else {
				System.out.println( "USER TOO FAR AWAY " + aUser.getClientId() + " with distance " + distance );
			}
		}

	}
        
        public void setVolunteersByTargets( EmrCaseData caseData, List<String> targets ) {
		caseData.getNotifiedUsers().clear();

		for( EmrUser aUser : Backend.getInstance().getUsersByFieldFromList(Shared.USER_EMAIL, targets)) {
                    caseData.getNotifiedUsers().add( aUser );
		}

	}

}
