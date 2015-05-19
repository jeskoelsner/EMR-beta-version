package org.zlwima.emurgency.mqtt.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpVersion;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.Shared.Rest;
import org.zlwima.emurgency.mqtt.android.config.Base;

public class RestService extends IntentService {

    public static final int STATUS_RUNNING = 0x1;
    public static final int STATUS_ERROR = 0x2;
    public static final int STATUS_FINISHED = 0x3;

    public static final String ERROR_MESSAGE = "errorMsg";
    public static final String IS_ONLINE = "isOnline";
    public static final String IS_ONLINE_CALLBACK = "isOnline";

    public static final String ACTIVITY_STREAM_URL = "http://as-emurgency.appspot.com/api/activities";
    public static final String ACTIVITY_STREAM_CALLBACK = "callbackAS";
    public static final String ACTIVITY_STREAM_COMMAND = "commandAS";

    public static final String UPDATE_URL = "http://cloud.emurgency.tk/android.php";
    public static final String UPDATE_CALLBACK = "callbackUP";
    public static final String UPDATE_COMMAND = "commandUP";

    public static final String MISSION_COMMAND = "commandMission";
    public static final String PARAMETER_PAGE = "pageAS";

    private ResultReceiver restReceiver;
    private String command;

    public static final int HTTPS_PORT = 443;
    public static final int MAX_CONNECTIONS = 10;
    public static final int SOCKET_BUFFER_SIZE = 8192;
    public static HttpClient client = null;

    public RestService() {
        super("RestProcessor started...");
        Base.log("RestProcessor() (RestProcessor)");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Base.log("onHandleIntent (RestProcessor)");
        restReceiver = intent.getParcelableExtra(Shared.RECEIVER);
        command = intent.getStringExtra(Shared.COMMAND);

        // STARTING
        Bundle bundle = new Bundle();
        if (restReceiver != null) {
            restReceiver.send(STATUS_RUNNING, Bundle.EMPTY);
        }

        try {
            if (command.equals(ACTIVITY_STREAM_COMMAND)) {
                int page = intent.getIntExtra(PARAMETER_PAGE, 0);
                bundle.putString(ACTIVITY_STREAM_CALLBACK, getNews(page));
            } else if (command.equals(UPDATE_COMMAND)) {
                bundle.putString(UPDATE_CALLBACK, getUpdates());
            }
			//user = intent.getParcelableExtra( Shared.USER_OBJECT );
            //bundle.putInt( LOCATION_SERVICE, RestClient.updateLocation( user ) );
        } catch (HttpResponseException e) {
            bundle.putString(ERROR_MESSAGE, e.getMessage() + " " + e.getStatusCode());
            if (restReceiver != null) {
                restReceiver.send(STATUS_ERROR, bundle);
            }
        }

        // FINISHED
        if (restReceiver != null) {
            restReceiver.send(STATUS_FINISHED, bundle);
        }
    }

    public static String getNews(int page) throws HttpResponseException {
        String newsUrl = ACTIVITY_STREAM_URL + "?page=" + page;
        HttpResponse response = httpGet(newsUrl);
        String content;
        try {
            content = streamtoString(response.getEntity().getContent());
        } catch (IllegalStateException e) {
            throw new HttpResponseException(0, null);
        } catch (IOException e) {
            throw new HttpResponseException(0, null);
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            return content;
        } else {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(), null);
        }
    }

    public static String getUpdates() throws HttpResponseException {
        String updatesUrl = UPDATE_URL;
        HttpResponse response = httpGet(updatesUrl);
        String content;
        try {
            content = streamtoString(response.getEntity().getContent());
        } catch (IllegalStateException e) {
            throw new HttpResponseException(0, null);
        } catch (IOException e) {
            throw new HttpResponseException(0, null);
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            return content;
        } else {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(), null);
        }
    }

    public static String streamtoString(InputStream is) throws HttpResponseException {
        BufferedReader r;
        try {
            r = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new HttpResponseException(0, null);
        }

        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            throw new HttpResponseException(0, null);
        }

        return total.toString();
    }

    public static HttpResponse httpGet(String url) throws HttpResponseException {
        HttpGet httpGet = new HttpGet(URI.create(url));
        httpGet.setHeader("Accept", "application/json");
        Base.log("\tRequest [GET][JSON] to url: " + url);
        return httpExecute(httpGet);
    }

    private static HttpResponse httpPost(String url, String body) throws HttpResponseException {
        HttpPost httpPost = new HttpPost(URI.create(url));
        httpPost.setHeader("Content-Type", Rest.CONTENT_TYPE_JSON);
        httpPost.setHeader("Accept", Rest.CONTENT_TYPE_JSON);
        try {
            httpPost.setEntity(new ByteArrayEntity(body.getBytes("UTF8")));
        } catch (UnsupportedEncodingException e) {
            throw new HttpResponseException(0, null);
        }
        Base.log("\tRequest [POST][JSON]: " + body);
        return httpExecute(httpPost);
    }

    public static HttpResponse httpExecute(HttpUriRequest httpRequest) throws HttpResponseException {
        HttpResponse response = null;

        try {
            response = getClient().execute(httpRequest);
        } catch (ClientProtocolException e) {
            throw new HttpResponseException(0, null);
        } catch (IOException e) {
            throw new HttpResponseException(0, null);
        }

        return response;
    }

    public static HttpClient getClient() {
        if (client == null) {
            client = new DefaultHttpClient();

            // get default parameters to pass on thread-safe client
            HttpParams params = client.getParams();

            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            // some webservers have problems if this is set to true
            HttpProtocolParams.setUseExpectContinue(params, false);

            HttpConnectionParams.setConnectionTimeout(params, Rest.TIMEOUT_MILLIS);
            HttpConnectionParams.setSoTimeout(params, Rest.TIMEOUT_MILLIS);
            HttpConnectionParams.setSocketBufferSize(params, SOCKET_BUFFER_SIZE);
            ConnManagerParams.setMaxTotalConnections(params, MAX_CONNECTIONS);

            SchemeRegistry reg = new SchemeRegistry();
            reg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), Rest.DEFAULT_PORT));
            reg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), HTTPS_PORT));

            // create default client with thread-safe parameters
            client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, reg), params);
        }
        return client;
    }

}
