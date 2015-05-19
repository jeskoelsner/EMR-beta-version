package org.zlwima.emurgency.mqtt.android;

import android.app.Activity;
import android.os.Bundle;

/* 
	a simple doing nothing activity that gets closed
	as soon as it is called from the notification intent.
	why ? after closing it will automatically return to 
	the last activity that a user was on, like this
	restoring the application from memoey on click :)
*/

/*
	This won't work if your application isn't already running. 
	However, you have 2 options to deal with that:
	1. Make sure the notification isn't present in the notification bar when your application is not running.
	2. In the onCreate() method of the NotificationActivity, check if your application is running, and if it isn't running call startActivity() and launch your application. If you do this, be sure to set the flag Intent.FLAG_ACTIVITY_NEW_TASK when starting the application so that the root activity of the task is not NotificationActivity.
*/

public class NotificationActivity extends Activity {
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		finish();
	}
}
