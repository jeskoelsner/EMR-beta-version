package org.zlwima.emurgency.mqtt.android.ui;

import org.zlwima.emurgency.mqtt.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class UnlockView extends RelativeLayout {
	private static int unlockBuffer;
	public UnlockListener unlockListener;

	public UnlockView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		TypedArray attrsArray = context.getTheme().obtainStyledAttributes( attrs, R.styleable.UnlockView, 0, 0 );

		try {
			unlockBuffer = attrsArray.getInteger( R.styleable.UnlockView_unlockBuffer, 0 );
		} finally {
			attrsArray.recycle();
		}

		// TODO as attribute
		LayoutInflater.from( context ).inflate( R.layout.part_unlock_caption, this );
		LayoutInflater.from( context ).inflate( R.layout.part_unlock_drag, this );
	}

	public void setOnUnlockListener( UnlockListener uListener ) {
		unlockListener = uListener;
	}
}
