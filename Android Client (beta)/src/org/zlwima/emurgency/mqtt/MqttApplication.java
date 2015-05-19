package org.zlwima.emurgency.mqtt;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import com.google.gson.Gson;

import java.util.ArrayList;

import org.zlwima.emurgency.backend.model.EmrCaseData;
import org.zlwima.emurgency.backend.model.EmrUser;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttMessageHandler;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttMessageReceiver;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttStatusHandler;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttStatusReceiver;
import org.zlwima.emurgency.mqtt.android.DashboardActivity;
import org.zlwima.emurgency.mqtt.android.MissionActivity;
import org.zlwima.emurgency.mqtt.android.NotificationActivity;
import org.zlwima.emurgency.mqtt.android.config.Base;
import org.zlwima.emurgency.mqtt.service.MqttService;

import static android.content.Context.POWER_SERVICE;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.zlwima.emurgency.backend.model.EmrLocation;
import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;
import org.zlwima.emurgency.mqtt.android.config.SharedPrefs;
import org.zlwima.emurgency.mqtt.android.ui.ApplicationDialog;
import org.zlwima.emurgency.mqtt.service.LocationIntentService;
import static org.zlwima.emurgency.mqtt.service.MqttService.MAX_MQTT_CLIENTID_LENGTH;

public class MqttApplication extends Application implements MqttStatusHandler, MqttMessageHandler, ConnectionCallbacks, OnConnectionFailedListener  {

    public static MqttApplication APPLICATION = null;

    public DashboardActivity dashboardActivity;
    public MissionActivity missionActivity;
    private ApplicationDialog dialogHelper;

    // shared application data
    public final ArrayList<EmrCaseData> activeCases = new ArrayList<EmrCaseData>();
    public EmrUser USER = new EmrUser();
    public String DISPLAYED_CASE_ID = "blank";
    public boolean LOGOUT = false;

    // This is how the Android client app will identify itself to the  message broker (UNIQUE!) 
    private String mqttClientId = null;
    private MqttMessageReceiver msgReceiver;
    private MqttStatusReceiver statusReceiver;

    private Ringtone ringtone;
    private SharedPrefs sharedPrefs;

    private MediaPlayer mp;
    private AudioManager mAudioManager;
    private int defaultVolume;
    private Uri alarmSound;
    
    private static final long UPDATE_INTERVAL = 10 * 15 * 2 * 10;    //All 5min.
    private static final long FASTEST_INTERVAL = 10 * 15 * 1;        //All 15sec
    private static final long SLOW_UPDATE_INTERVAL = 10 * 15 * 1 * 60;           //All 15min.
    private static final int MIN_DISTANCE = 5;  //in m
    private static final int MED_DISTANCE = 50;	//in m
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private LocationRequest mLocationRequestLow;
    private LocationRequest mLocationRequestHigh;
    private PendingIntent mPendingIntent;
    private Intent mLocationIntent;
    
    private final DialogInterface.OnClickListener okDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            switch (id) {
                case DialogInterface.BUTTON_POSITIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            stopSound();
        }
    };

    class SleepTask extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    public void playSound(int volume, int length) {
        try {
            if (!mp.isPlaying()) {
                mp.setDataSource(this, alarmSound);
                mp.setAudioStreamType(AudioManager.STREAM_ALARM);
                mp.setLooping(true);
                mp.prepare();
                mp.start();

                Timer timer = new Timer("timer", true);
                timer.schedule(new SleepTask(), length * 1000);
            }

        } catch (IOException e) {
            Base.log("Sound not possible to play");
        }
        // set the volume to what we want it to be.  In this case it's max volume for the alarm stream.
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)*volume/100, AudioManager.FLAG_PLAY_SOUND);
    }

    public void stopSound() {
        // reset the volume to what it was before we changed it.
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, defaultVolume, AudioManager.FLAG_PLAY_SOUND);
        mp.stop();
        mp.reset();
    }

    public void releasePlayer() {
        mp.release();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Base.log("******************** ONCREATE APPLICATION ******************** should be null (" + APPLICATION + ")");
        APPLICATION = this;
        bindStatusReceiver();
        bindMessageReceiver();

        sharedPrefs = new SharedPrefs(getApplicationContext());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
        defaultVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        mp = new MediaPlayer();

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);

        sharedPrefs = new SharedPrefs(getApplicationContext());
        
        TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        //Location service
        mLocationRequestLow = LocationRequest.create();
        if (telMgr.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
            Toast.makeText(this, getString(R.string.bubble_no_sim), Toast.LENGTH_LONG).show();
            mLocationRequestLow.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        } else {
            mLocationRequestLow.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        }
        mLocationRequestLow.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequestLow.setInterval(SLOW_UPDATE_INTERVAL);
        mLocationRequestLow.setSmallestDisplacement(MED_DISTANCE);
        
        mLocationRequestHigh = LocationRequest.create();
        mLocationRequestHigh.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHigh.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequestHigh.setInterval(UPDATE_INTERVAL);
        mLocationRequestHigh.setSmallestDisplacement(MIN_DISTANCE);
        
        //standard
        mLocationRequest = mLocationRequestLow;

        mLocationClient = new LocationClient(this, this, this);

        mLocationIntent = new Intent(this, LocationIntentService.class);
        mPendingIntent = PendingIntent.getService(this, 0, mLocationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public void startLocationUpdates(){
        if (servicesConnected()) {
            Base.log("--- Location updates started");
            mLocationClient.connect();
        }
    }
    
    public void stopLocationUpdates(){
        if (mLocationClient.isConnected()) {
            mLocationClient.disconnect();
            mPendingIntent.cancel();
            Base.log("--- Location updates stopped");
        }
    }
    
    public void changeLocationUpdates(boolean high){
        stopLocationUpdates();
        mLocationRequest = (high ? mLocationRequestHigh : mLocationRequestLow);
        startLocationUpdates();
        Base.log("--- Location updates changed to: " + (high ? "high" : "low"));
    }
    
    public void setDialogHelper(ApplicationDialog helper){
        dialogHelper = helper;
    }
    
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode
                = GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Base.log("Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error dialog from Google Play services
            
            /* Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            
            -- SKIP THIS... JUST ERROR OUT

            if (errorDialog != null) {
                errorDialog.show();
            }
            
            */
            
            return false;
        }
    }
    
    public void locationReconnect(){
        mLocationClient.disconnect();
        mLocationClient.connect();
    }

    public void clear() {
        USER = new EmrUser();
        activeCases.clear();
    }

    public void closeConnection() {
        Base.log("CLEANING UP (MqttApplication)");
        LOGOUT = true;
        MqttServiceDelegate.stopService(getApplicationContext());
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    // MQTT spec does not allow client ids longer than 23 chars
    public String getAndroidClientId() {
        Base.log("getAndroidClientId(): " + mqttClientId);
        if (mqttClientId == null) {
            String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (android_id.length() > MAX_MQTT_CLIENTID_LENGTH) {
                android_id = android_id.substring(0, MAX_MQTT_CLIENTID_LENGTH);
            }
            mqttClientId = android_id;
        }
        return mqttClientId;
    }

    public void startMissionActivity(EmrCaseData caseData) {
        Base.log("*** startMissionActivity() ***");
        Intent missionIntent = new Intent();
        missionIntent.setClass(getApplicationContext(), MissionActivity.class);
        missionIntent.putExtra("CASEID", caseData.getCaseId());
        missionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(missionIntent);
    }

    @Override
    public void handleMessage(String topic, byte[] payload) {
        // client only listens to server/{id}/# so we are interested in the last topic
        Base.log("handleMessage() MqttApplication: topic=" + topic + ", message=" + new String(payload));
        String subTopic = topic.substring(topic.lastIndexOf("/"));
        String payloadString = new String(payload);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "caseDataUpdate");
        wakeLock.acquire();

        EmrCaseData receivedCaseData = new Gson().fromJson(payloadString, EmrCaseData.class);
        
        if (subTopic.equals("/updateCase")) {
            receivedCaseData.setCaseArrivedOnClientTimeMillis(SystemClock.elapsedRealtime() - receivedCaseData.getCaseRunningTimeMillis());
            
            // check if this case is new to the android client
            if (!hasCase(receivedCaseData.getCaseId())) {
                //ringtone.play();
                playSound(sharedPrefs.getVolume(), sharedPrefs.getSoundLength());
                dashboardActivity.addCaseToAdapter(receivedCaseData);
            } else {
                dashboardActivity.updateCaseInAdapter(receivedCaseData);
            }
            startMissionActivity(receivedCaseData);

        } else if (subTopic.equals("/closeCase")) {
            // serverMessages are mainly handled in LoginActivity, but a realtime UserStatusUpdate arrives only here	
            if (hasCase(receivedCaseData.getCaseId())) {
                dashboardActivity.removeRunningCase(receivedCaseData.getCaseId());
            }
        }

        wakeLock.release();
    }

    @Override
    public void handleStatus(MqttService.ConnectionStatus status, String reason) {
        Base.log("*** handleStatus() MqttApplication *** " + reason);
        boolean CONNECTED = (status == MqttService.ConnectionStatus.CONNECTED);
        Intent notificationIntent = new Intent(getApplicationContext(), NotificationActivity.class);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("EMuRgency")
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
                .setContentText(CONNECTED ? "VNS connection available :)" : "No connection to VNS server :(")
                .setSmallIcon(CONNECTED ? R.drawable.icon_notification_color : R.drawable.icon_notification_grey)
                .getNotification();
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;

        if (!LOGOUT) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Base.TAG, 255, notification);
        }
    }
    
    public void addCase(EmrCaseData caseData){
        if(!hasCase(caseData.getCaseId())){
            activeCases.add(null);
        }
    }
    
    public boolean hasCase(String caseId){
        return (getCaseById(caseId) != null);
    }

    public EmrCaseData getCaseById(String caseId) {
        for (EmrCaseData oneCase : activeCases) {
            if (oneCase.getCaseId().equals(caseId)) {
                return oneCase;
            }
        }
        return null;
    }
    
    public void onConnected(Bundle dataBundle) {
        Location lastLocation = mLocationClient.getLastLocation();

        Base.log("Location Client onConnect [Bundle]:\n\t -> " + new Gson().toJson(dataBundle, Bundle.class).toString());

        Base.log("Location Client Values: ");
        Base.log("\t -> onConnected: " + mLocationClient.isConnected());
        Base.log("\t -> onConnecting: " + mLocationClient.isConnecting());
        Base.log("Location Client getLastLocation: " + lastLocation.getLatitude() + "/" + lastLocation.getLongitude());

        if (lastLocation != null && lastLocation.getLatitude() != 0.0 && lastLocation.getLongitude() != 0.0) {
            EmrLocation location = new EmrLocation();
            location.setLatitude(lastLocation.getLatitude());
            location.setLongitude(lastLocation.getLongitude());

            APPLICATION.USER.setLocation(location);

            Base.log("lastLocation:" + lastLocation.getLatitude() + "/" + lastLocation.getLongitude());
        } else {
            Base.log("lastLocation: NO LOCATION!");
        }

        //fixes...
        mLocationClient.requestLocationUpdates(mLocationRequest, mPendingIntent);
    }
    

    //@Override
    public void onDisconnected() {
        // Display the connection status
        dialogHelper.messageDialog(R.string.dialog_resolution_error_title, R.string.dialog_resolution_error_message, ApplicationDialog.MESSAGE_OK, okDialogListener).show();
    }
    
    public final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 991;

    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            //try {
                // Start an Activity that tries to resolve the error
                
                //TODO WEIRDLY HACKY
                
                //connectionResult.startResolutionForResult(dashboardActivity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            // } catch (IntentSender.SendIntentException e) {
                dialogHelper.messageDialog(R.string.dialog_resolution_error_title, connectionResult.getResolution().toString(), ApplicationDialog.MESSAGE_OK, okDialogListener).show();
            //}
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            dialogHelper.messageDialog(R.string.dialog_resolution_error_title, R.string.dialog_resolution_notfound_message, ApplicationDialog.MESSAGE_OK, okDialogListener).show();
        }
    }

    public void bindMessageReceiver() {
        msgReceiver = new MqttMessageReceiver();
        msgReceiver.registerHandler(this);
        registerReceiver(msgReceiver, new IntentFilter(MqttService.MQTT_MSG_RECEIVED_INTENT));
    }

    private void bindStatusReceiver() {
        statusReceiver = new MqttStatusReceiver();
        statusReceiver.registerHandler(this);
        registerReceiver(statusReceiver, new IntentFilter(MqttService.MQTT_STATUS_INTENT));
    }

    // client only listens to server/{id}/# so we tell the server our {id} in the loginRequest
    public void login(EmrUser loginUser) {
        String topic = "client/login";
        MqttServiceDelegate.publish(this, topic, loginUser.toJson().getBytes());
    }

    public void sms(String message) {
        Base.log("SMS() CALLED " + message);
        String topic = "client/sms";
        MqttServiceDelegate.publish(this, topic, message.getBytes());
    }

    public void registration(EmrUser registerUser) {
        String topic = "client/registration";
        MqttServiceDelegate.publish(this, topic, registerUser.toJson().getBytes());
    }

    public void logout() {
        stopLocationUpdates();
        String topic = "client/" + APPLICATION.getAndroidClientId() + "/logout";
        MqttServiceDelegate.publish(this, topic, APPLICATION.USER.toJson().getBytes());
    }

    public void acceptCase() {
        String topic = "client/" + APPLICATION.getAndroidClientId() + "/acceptCase";
        MqttServiceDelegate.publish(this, topic, DISPLAYED_CASE_ID.getBytes());
    }

    public void updateLocation() {
        String topic = "client/" + APPLICATION.getAndroidClientId() + "/updateLocation";
        MqttServiceDelegate.publish(this, topic, APPLICATION.USER.toJson().getBytes());
    }

    public void arrivedAtCase(String caseId) {
        String topic = "client/" + APPLICATION.getAndroidClientId() + "/arrivedAtCase";
        MqttServiceDelegate.publish(this, topic, APPLICATION.DISPLAYED_CASE_ID.getBytes());
    }

}
