package org.zlwima.emurgency.mqtt.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

import java.util.HashMap;
import org.w3c.dom.Document;

import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.model.EmrCaseData;
import org.zlwima.emurgency.backend.model.EmrLocation;
import org.zlwima.emurgency.backend.model.EmrVolunteer;
import org.zlwima.emurgency.mqtt.R;
import org.zlwima.emurgency.mqtt.android.config.Base;
import org.zlwima.emurgency.mqtt.android.ui.FontHelper;
import org.zlwima.emurgency.mqtt.android.ui.UnlockListener;
import org.zlwima.emurgency.mqtt.android.ui.UnlockView;

import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;
import org.zlwima.emurgency.mqtt.android.ui.GMapDirections;

public class MissionActivity extends Activity implements View.OnClickListener, UnlockListener, CompoundButton.OnCheckedChangeListener {

    private GoogleMap map;
    private GMapDirections mapd;
    private Document directions;
    private Polyline lastPolyline;
    private EmrCaseData activeCaseData;
    private ViewSwitcher flipBack;
    private TextView caseAddress;
    private TextView caseMeters;
    private TextView caseMetersHint;
    private TextView caseVolunteers;
    private Toast caseNote;
    private RelativeLayout unlockScreen;
    private RelativeLayout overlay;
    private UnlockView unlockView;
    private ToggleButton toggleZoom;
    private ToggleButton toggleVolunteers;
    private Chronometer caseTimer;
    private Button caseBack;
    private Button buttonHelp;
    private Button buttonArrived;
    private Resources resources;
    private MarkerOptions selfMarker = new MarkerOptions();
    private MarkerOptions caseMarker = new MarkerOptions();
    private MarkerOptions newVolunteerMarker = new MarkerOptions();
    private HashMap<String, MarkerOptions> volunteerMarkers = new HashMap<String, MarkerOptions>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APPLICATION.missionActivity != null) {
            finish();
            return;
        }
        APPLICATION.missionActivity = this;
        APPLICATION.DISPLAYED_CASE_ID = getIntent().getExtras().getString("CASEID");

        Base.log("ONCREATE CASEID MISSION: " + APPLICATION.DISPLAYED_CASE_ID);

        setContentView(R.layout.screen_mission);

        resources = getResources();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // globally add 'droid sans' font again on samsung devices
        FontHelper fHelper = new FontHelper(this);
        fHelper.applyCustomFont((RelativeLayout) findViewById(R.id.missionRoot));

        // Get UI elements
        unlockScreen = (RelativeLayout) findViewById(R.id.unlockScreen);
        unlockView = (UnlockView) findViewById(R.id.unLocker);
        unlockView.setOnUnlockListener(this);

        overlay = (RelativeLayout) findViewById(R.id.overlay);
        overlay.setOnClickListener(this);

        flipBack = (ViewSwitcher) findViewById(R.id.flipBack);
        flipBack.setDisplayedChild(0);

        caseAddress = (TextView) findViewById(R.id.caseAddress);
        caseMeters = (TextView) findViewById(R.id.caseMeters);
        caseVolunteers = (TextView) findViewById(R.id.caseVolunteers);

        caseMetersHint = (TextView) findViewById(R.id.caseMetersHint);

        caseBack = (Button) findViewById(R.id.caseBack);
        caseTimer = (Chronometer) findViewById(R.id.caseTimer);
        toggleZoom = (ToggleButton) findViewById(R.id.toolbarAutozoom);
        toggleVolunteers = (ToggleButton) findViewById(R.id.toolbarVolunteers);

        toggleZoom.setChecked(true);
        toggleVolunteers.setChecked(true);

        toggleZoom.setOnCheckedChangeListener(this);
        toggleVolunteers.setOnCheckedChangeListener(this);

        buttonHelp = (Button) findViewById(R.id.toolbarHelp);
        buttonArrived = (Button) findViewById(R.id.toolbarArrived);

        buttonHelp.setOnClickListener(this);
        buttonArrived.setOnClickListener(this);
        caseBack.setOnClickListener(this);
        caseAddress.setOnClickListener(this);

        // Design Marker
        selfMarker = new MarkerOptions();
        selfMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.case_self_marker));
        caseMarker = new MarkerOptions();
        caseMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.case_target_marker));
        newVolunteerMarker = new MarkerOptions();
        newVolunteerMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.case_volunteer_marker));

        volunteerMarkers = new HashMap<String, MarkerOptions>();

        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_fragment))
                .getMap();

        mapd = new GMapDirections(this);
        directions = mapd.refreshDocument(
            new LatLng(APPLICATION.USER.getLocation().getLatitude(), APPLICATION.USER.getLocation().getLongitude()), 
            new LatLng(APPLICATION.getCaseById(APPLICATION.DISPLAYED_CASE_ID).getCaseLocation().getLatitude(), APPLICATION.getCaseById(APPLICATION.DISPLAYED_CASE_ID).getCaseLocation().getLongitude()),
            GMapDirections.MODE_WALKING);

        if (map != null) {
            map.setIndoorEnabled(true);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            
            updateCase();
        } else {
            //Maps not installed!
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
            startActivity(intent);

            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Base.log("MissionActivity: onNewIntent()");
        super.onNewIntent(intent);

        if (APPLICATION.DISPLAYED_CASE_ID.equals(intent.getExtras().getString("CASEID"))) {
            updateCase();
        } else {
            Base.log("MissionActivity: onNewIntent() - " + APPLICATION.DISPLAYED_CASE_ID + " ... " + intent.getExtras().getString("CASEID"));
        }
    }

    @Override
    protected void onDestroy() {
        Base.log("MissionActivity: onDestroy()");
        if (APPLICATION.missionActivity != null) {
            if (APPLICATION.missionActivity.equals(this)) {
                APPLICATION.missionActivity = null;
            }
            getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        APPLICATION.changeLocationUpdates(false);
        super.onDestroy();
    }

    @Override
    public void onUnlock() {
        Base.log("MissionActivity: onUnlock()");
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation anim) {
                unlockScreen.setVisibility(View.GONE);

                flipBack.setDisplayedChild(1);
                map.getUiSettings().setAllGesturesEnabled(true);
                
                APPLICATION.acceptCase();
                APPLICATION.changeLocationUpdates(true);
            }

            public void onAnimationRepeat(Animation anim) { /* DO NOTHING */

            }

            public void onAnimationStart(Animation anim) { /* DO NOTHING */

            }
        });
        unlockScreen.startAnimation(fadeOut);
    }

    /**
     * Show case on the map
     */
    private void updateCase() {
        Base.log("MissionActivity: updateCase() " + APPLICATION.DISPLAYED_CASE_ID);
        activeCaseData = APPLICATION.getCaseById(APPLICATION.DISPLAYED_CASE_ID);

        caseAddress.setText(activeCaseData.getCaseAddress());
        caseNote = Toast.makeText(MissionActivity.this, activeCaseData.getCaseNotes(), Toast.LENGTH_LONG);
        caseVolunteers.setText(
                String.valueOf(activeCaseData.getVolunteers().size()));

        caseTimer.setBase(activeCaseData.getCaseArrivedOnClientTimeMillis());
        caseTimer.start();

        MarkerOptions tmpMarker;
        for (EmrVolunteer volunteer : activeCaseData.getVolunteers()) {
            if (volunteer.getEmail().equals(APPLICATION.USER.getEmail())) {
                return;
            }
            if (volunteerMarkers.containsKey(volunteer.getEmail())) {
                tmpMarker = volunteerMarkers.get(volunteer.getEmail());
                tmpMarker.position(getLatLng(volunteer.getLocation()));
            } else {
                //TODO check if marker gets updated after insertion
                tmpMarker = newVolunteerMarker;
                tmpMarker.position(getLatLng(volunteer.getLocation()));
                volunteerMarkers.put(volunteer.getEmail(), tmpMarker);
            }
        }

        //do visual marker magic
        updateMarker();
    }

    public void updateMarker() {
        Base.log("MissionActivity: updateMarker()");
        selfMarker.position(getLatLng(APPLICATION.USER.getLocation()));
        caseMarker.position(getLatLng(activeCaseData.getCaseLocation()));
        
        map.clear();
        map.addMarker(selfMarker);
        map.addMarker(caseMarker);

        if (toggleVolunteers.isChecked()) {
            for (MarkerOptions volunteer : volunteerMarkers.values()) {
                map.addMarker(volunteer);
            }
        }

        //zoom in to fit markers
        updateZoom();
    }

    private void updateZoom() {
        Base.log("MissionActivity: updateZoom()");
        if (toggleZoom.isChecked()) {

            Builder boundsBuilder = new LatLngBounds.Builder();

            boundsBuilder.include(selfMarker.getPosition());
            boundsBuilder.include(caseMarker.getPosition());

            if (toggleVolunteers.isChecked()) {
                for (MarkerOptions option : volunteerMarkers.values()) {
                    boundsBuilder.include(option.getPosition());
                }
            }

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(),
                    this.getResources().getDisplayMetrics().widthPixels,
                    this.getResources().getDisplayMetrics().heightPixels,
                    100));
        }

        //calculate new distance to case
        updateDistance(activeCaseData);
    }

    public void updateDistance(EmrCaseData caseData) {
        Base.log("MissionActivity: updateDistance()");
        double distance = Shared.calculateDistance(
                caseData.getCaseLocation().getLatitude(),
                caseData.getCaseLocation().getLongitude(),
                APPLICATION.USER.getLocation().getLatitude(),
                APPLICATION.USER.getLocation().getLongitude());
        if (distance > 10000) {
            caseMeters.setText("10+");
            caseMetersHint.setText(getString(R.string.case_distance_kilometers_full));
        } else if (distance > 1000) {
            distance /= 1000;
            caseMeters.setText(String.format("%.1f", distance));
            caseMetersHint.setText(getString(R.string.case_distance_kilometers_full));

        } else {
            caseMeters.setText(String.format("%d", (int) distance));
            caseMetersHint.setText(getString(R.string.case_distance_meters_full));
            if (distance < 50 && unlockScreen.getVisibility() == View.GONE) {
                buttonHelp.setVisibility(View.GONE);
                buttonArrived.setVisibility(View.VISIBLE);
            }
        }
        updateRoute();
    }

    public void updateRoute() {
        if(lastPolyline != null)
            lastPolyline.remove();

        ArrayList<LatLng> directionPoint = mapd.getDirection(directions);
        PolylineOptions rectLine = new PolylineOptions().width(10).color( 0xFF0A8CD2 );

        for (int i = 0; i < directionPoint.size(); i++) {
            rectLine.add(directionPoint.get(i));
        }
        
        lastPolyline = map.addPolyline(rectLine);
    }

    public void closeCase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MissionActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(resources.getString(R.string.case_alert_title));
        builder.setMessage(resources.getString(R.string.case_alert_text_shutdown));
        builder.setPositiveButton(resources.getString(R.string.button_positive_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.create().show();
    }

    private void showHelp(boolean show) {
        Base.log("Help Overlay: " + getResources().getResourceEntryName(overlay.getId()));
        if (show) {
            overlay.setVisibility(View.VISIBLE);
        } else {
            overlay.setVisibility(View.GONE);
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        Base.log("Clicked Element: " + getResources().getResourceEntryName(id));

        if (id == R.id.caseAddress) {
            caseNote.show();
        }
        if (id == R.id.caseBack) {
            if (unlockScreen.getVisibility() == View.GONE) {
                Toast.makeText(this, getString(R.string.bubble_backbutton_disabled), Toast.LENGTH_LONG).show();
            } else {
                finish();
            }
        }
        if (id == R.id.toolbarHelp) {
            Base.log("Toolbar Help Clicked");
            showHelp(true);
        }
        if (id == R.id.toolbarArrived) {
            APPLICATION.arrivedAtCase(APPLICATION.DISPLAYED_CASE_ID);

            buttonArrived.setEnabled(false);
            buttonArrived.setClickable(false);
        }
        if (id == R.id.overlay) {
            Base.log("Help Overlay Clicked");
            showHelp(false);
        }
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int id = compoundButton.getId();

        if (id == R.id.toolbarAutozoom) {
            updateZoom();
        }
        if (id == R.id.toolbarVolunteers) {
            updateMarker();
        }
    }

    private LatLng getLatLng(EmrLocation location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (unlockScreen.getVisibility() == View.GONE) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Toast.makeText(this, getString(R.string.bubble_backbutton_disabled), Toast.LENGTH_LONG).show();
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_HOME) {
                Toast.makeText(this, getString(R.string.bubble_homebutton_disabled), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);

    }
}
