package org.zlwima.emurgency.mqtt.android.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontHelper {
	private static Typeface typeface;
	private static Typeface typeface_bold;

	public FontHelper( Context context ) {
		typeface = Typeface.createFromAsset( context.getAssets(), "droid_sans.ttf" );
		typeface_bold = Typeface.createFromAsset( context.getAssets(), "droid_sans_bold.ttf" );
	}

	public void applyCustomFont( ViewGroup list ) {
		for( int i = 0; i < list.getChildCount(); i++ ) {
			View view = list.getChildAt( i );
			if( view instanceof ViewGroup ) {
				applyCustomFont( (ViewGroup) view );
			} else if( view instanceof TextView ) {
				((TextView) view).setTypeface( typeface );
			}
		}
	}
}
