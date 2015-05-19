package org.zlwima.emurgency.mqtt.android.ui;

import org.zlwima.emurgency.mqtt.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.Resources;

public class ApplicationDialog {
        public static final int MESSAGE_NEUTRAL = 0;
        public static final int MESSAGE_CHOICE = 1;
        public static final int MESSAGE_OK = 2;
        
        private final Activity activity;
	private final Resources resources;
	private final Builder messageDialog;
	private final ProgressDialog progressDialog;
        

	/*
	 * Constructor to bind activity and resources to current running activity.
	 * Pre- construct message- and progress dialog.
	 */
	public ApplicationDialog( Activity currentActivity ) {
		activity = currentActivity;
		resources = activity.getResources();
		messageDialog = new AlertDialog.Builder( activity );
		progressDialog = new ProgressDialog( activity );
	}

	/*
	 * Simple progress dialog without interactive buttons
	 */
	public ProgressDialog progressDialog( int resTitle, int resMessage ) {
		progressDialog.setTitle( R.string.login_loading_title );
		progressDialog.setMessage( resources.getText( resMessage ) );
		return progressDialog;
	}

	public Builder messageDialog( int resTitle, String resMessage, int type, DialogInterface.OnClickListener listener ) {
		return messageDialogBuild( resTitle, resMessage, type, listener );
	}

	public Builder messageDialog( int resTitle, int resMessageId, int type, DialogInterface.OnClickListener listener ) {
		return messageDialogBuild( resTitle, resMessageId, type, listener );
	}

	/*
	 * Actual implementation of messageDialog. Object -> Integer/String only
	 */
	private Builder messageDialogBuild( int resTitle, Object resMessage, int type, DialogInterface.OnClickListener listener ) {
            int resMessageId = (Integer) ((resMessage instanceof Integer) ? resMessage : -1);
            String resMessageText = (String) ((resMessage instanceof String) ? resMessage : null);

            messageDialog.setTitle( resTitle );

            if( resMessageText != null ) {
                messageDialog.setMessage( resMessageText );
            } else {
                messageDialog.setMessage( resMessageId );
            }

            if( type == MESSAGE_NEUTRAL ) {
                messageDialog.setPositiveButton( null, null );
                messageDialog.setNegativeButton( null, null );
                messageDialog.setNeutralButton( R.string.button_neutral, listener );
            } else if ( type == MESSAGE_CHOICE ) {
                messageDialog.setPositiveButton( R.string.button_positive_ok, listener );
                messageDialog.setNegativeButton( R.string.button_negative_cancel, listener );
                messageDialog.setNeutralButton( null, null );
            } else {
                messageDialog.setPositiveButton( R.string.button_positive_ok, listener );
                messageDialog.setNegativeButton( null, null );
                messageDialog.setNeutralButton( null, null );
            }

            messageDialog.create();

            return messageDialog;
	}

}
