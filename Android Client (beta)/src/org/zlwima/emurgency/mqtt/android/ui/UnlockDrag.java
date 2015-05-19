package org.zlwima.emurgency.mqtt.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import org.zlwima.emurgency.mqtt.android.config.Base;

public class UnlockDrag extends Button {
	private int marginTop;
	private int marginRight;
	private int marginBottom;
	private int marginLeft;
	private int buttonWidth;
	private int buttonHeight;
	private int panelWidth;
	private int unlockPosition;
	private ViewParent viewParent;
	private UnlockView unlockPanel;
	private MarginLayoutParams layoutParams;
	private LayoutParams layoutSetup;
	private boolean locked;

	public UnlockDrag( Context context, AttributeSet attrs ) {
		super( context, attrs );
		locked = false;
	}

	@Override
	protected void onSizeChanged( int width, int height, int oldwidth, int oldheight ) {
		super.onSizeChanged( width, height, oldwidth, oldheight );
		Log.d( "EMR", "onSizeChanged invoked!" );

		// only on initial
		if( oldwidth == 0 && oldheight == 0 ) {
			layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
			marginLeft = layoutParams.leftMargin;
			marginTop = layoutParams.topMargin;
			marginRight = layoutParams.rightMargin;
			marginBottom = layoutParams.bottomMargin;
			buttonHeight = layoutParams.height;
			buttonWidth = layoutParams.width;

			layoutSetup = new RelativeLayout.LayoutParams( buttonWidth, buttonHeight );

			viewParent = getParent();
			if( viewParent instanceof UnlockView ) {
				unlockPanel = (UnlockView) viewParent;
				panelWidth = unlockPanel.getWidth();
			}

			unlockPosition = panelWidth - buttonWidth;
		}
	}

	/**
	 * TODO: make it an animation!
	 */
	private void relock() {
		setMargins( 0 );
	}

	/**
	 * TODO: add animation to lock point
	 */
	private void unlock() {
            Base.log("UnlockDrag: unlock()");
		if( unlockPanel.unlockListener != null ) {
			unlockPanel.unlockListener.onUnlock();
		}
		locked = true;
	}

	/**
	 * Basic transformation from left
	 *
	 * @param offset
	 */
	public void setMargins( int offset ) {
		layoutSetup.setMargins( offset + marginLeft, marginTop, marginRight,
				marginBottom );
		setLayoutParams( layoutSetup );
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		if( !locked ) {
			int posX;
			switch( event.getAction() ) {
				case MotionEvent.ACTION_DOWN:
					// do nothing
					break;
				case MotionEvent.ACTION_UP:
					relock();
					break;
				case MotionEvent.ACTION_MOVE:
					posX = (int) event.getRawX() - buttonWidth;

					if( posX >= unlockPosition ) {
						unlock();
						return false;
					} else if( posX < marginLeft ) {
						setMargins( 0 );
					} else if( posX >= marginLeft && posX <= unlockPosition ) {
						setMargins( posX );
					}
					break;
			}
			return true;
		}
		return false;
	}
}
