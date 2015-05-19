/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zlwima.emurgency.mqtt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;
import org.zlwima.emurgency.mqtt.android.config.Base;

/**
 *
 * @author Tom
 */
public class SmsReceiver extends BroadcastReceiver {

    //TODO find out dispatchCenterAddress
    private String dispatchCenterAddress = "";
    
    
    @Override
    public void onReceive(Context cntxt, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get( "pdus" );
        SmsMessage sms = SmsMessage.createFromPdu( (byte[]) pdus[0] );
        
        String messageBody = sms.getMessageBody();
        String originAddress = sms.getOriginatingAddress();
        
        if( originAddress.equals( dispatchCenterAddress ) )
            APPLICATION.sms( messageBody );
    }
    
}
