package org.zlwima.emurgency.mqtt.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.TextView;

import org.zlwima.emurgency.mqtt.R;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.model.EmrCaseData;
import org.zlwima.emurgency.backend.model.EmrLocation;

import org.zlwima.emurgency.mqtt.android.config.Base;

import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;

public class CaseAdapter extends ArrayAdapter<EmrCaseData> {
	private final static int RESSOURCE = R.layout.part_caselist_element;
	private final Activity context;

	class ViewHolder {
		public TextView caseAddress;
		public TextView caseDistance;
		public Chronometer caseTime;
		public TextView caseVolunteers;
		public TextView caseDistanceBase;
	}

	public CaseAdapter( Activity context ) {
		super( context, RESSOURCE, APPLICATION.activeCases );
		this.context = context;
		this.setNotifyOnChange( true );
	}

	@Override
	public void add( EmrCaseData caseData ) {
		caseData.setCaseStartTimeMillis( caseData.getCaseArrivedOnClientTimeMillis() );
		APPLICATION.activeCases.add( caseData );
		notifyDataSetChanged();
	}

	public void removeCaseById( String caseId ) {
		EmrCaseData upToDateCaseData = APPLICATION.getCaseById( caseId );
                if(upToDateCaseData != null){
                    APPLICATION.activeCases.remove( upToDateCaseData );
                    notifyDataSetChanged();
                }
	}

	public void update( EmrCaseData caseData ) {
		EmrCaseData oldCaseData = APPLICATION.getCaseById( caseData.getCaseId() );
		int entry = APPLICATION.activeCases.indexOf( oldCaseData );
		if( entry != -1 ) {
			Base.log( "********** UPDATING CASE IN ADAPTER :) **********" );
			APPLICATION.activeCases.remove( oldCaseData );
			APPLICATION.activeCases.add( entry, caseData );
			notifyDataSetChanged();
		} else {
			Base.log( "********** CASEUPDATE FAILED SINCE CASE WAS NOT FOUND **********" );
		}
	}

	@Override
	public View getView( final int position, View convertView, ViewGroup parent ) {
		View view = convertView;
		// if view is not inflated yet, lets inflate it
		if( view == null ) {
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = layoutInflater.inflate( RESSOURCE, null );
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.caseAddress = (TextView) view.findViewById( R.id.caseAddress );
			viewHolder.caseVolunteers = (TextView) view.findViewById( R.id.caseMiniVolunteers );
			viewHolder.caseTime = (Chronometer) view.findViewById( R.id.caseMiniTimer );
			viewHolder.caseDistance = (TextView) view.findViewById( R.id.caseMiniDistance );
			viewHolder.caseDistanceBase = (TextView) view.findViewById( R.id.caseMiniDistanceBase );
			view.setTag( viewHolder );
		}

		final ViewHolder holder = (ViewHolder) view.getTag();
		holder.caseAddress.setText( getItem( position ).getCaseAddress() );
		holder.caseVolunteers.setText( "" + getItem( position ).getVolunteers().size() );
		holder.caseTime.setBase( getItem( position ).getCaseArrivedOnClientTimeMillis());
		holder.caseTime.start();
		calculateDistance( getItem( position ).getCaseLocation(), holder );
		return view;
	}

	private void calculateDistance( EmrLocation caseLocation, ViewHolder holder ) {
		EmrLocation userLocation = APPLICATION.USER.getLocation();

		double distance = Shared.calculateDistance( caseLocation.getLatitude(), caseLocation.getLongitude(), userLocation.getLatitude(), userLocation.getLongitude() );
		if( distance > 10000 ) {
			holder.caseDistance.setText( "10+" );
			holder.caseDistanceBase.setText( context.getString( R.string.case_distance_kilometers ) );
		} else if( distance > 1000 ) {
			distance /= 1000;
			holder.caseDistance.setText( String.format( "%.1f", distance ) );
			holder.caseDistanceBase.setText( context.getString( R.string.case_distance_kilometers ) );
		} else {
			holder.caseDistance.setText( String.format( "%d", (int) distance ) );
			holder.caseDistanceBase.setText( context.getString( R.string.case_distance_meters ) );
		}
	}

}
