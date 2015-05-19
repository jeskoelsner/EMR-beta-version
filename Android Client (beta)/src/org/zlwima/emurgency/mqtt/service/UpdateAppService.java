/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zlwima.emurgency.mqtt.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.zlwima.emurgency.mqtt.android.config.Base;



public class UpdateAppService extends IntentService{
    private static String updateUrl;
    private static String updateVersion;

    public UpdateAppService() {
        super("EMurgency Updater");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        updateUrl = intent.getStringExtra("url");
        Base.log("Malformed before too? ->" + updateUrl);
        updateVersion = intent.getStringExtra("version");
        Update(updateUrl);
        Base.log("Updating from Intent");
    }
    
    public void Update(String apkurl){
      Base.log("Updating from function");
      try {
            Base.log("Malformed url?! ->" + apkurl);
            URL url = new URL(apkurl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            Base.log("Updater connected");

            String PATH = Environment.getExternalStorageDirectory() + "/download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, "emurgency-"+updateVersion+".apk");
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();
            
            Base.log("Update filed downloaded");
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "emurgency-"+updateVersion+".apk")), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);  
            
        } catch (IOException e) {
            Base.log("Update ERROR " + e.getLocalizedMessage());
        }
  }  
}
