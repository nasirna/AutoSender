package data;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.nasirna.autosender.GsonRequest;
import com.nasirna.autosender.R;
import com.nasirna.autosender.SuccessObject;
import com.nasirna.autosender.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;


public class DownloadService extends Service {

    //Context context;
    public static Boolean isThreadRunning = false;
    int SYNC_THREAD_TIMER = 1000 * 30;
    int SMS_THREAD_TIMER = 1000 * 5;
    ServerManager serverManager;
    data.DBHelper dbHelper;
    String[] data;
    String tempQuery;
    int smsNotSent = 0;

    private String mId, mTlp, mPesan, mJenis, mStatus;

    public DownloadService() {
        // this.context = context;
    }

    /*  THIS IS THE MAIN POOL,
    *   2 threads are created, one to retreive
    *   new records from server and put it in a local...
    *   second one get one by one sms from local and send it
    *   to the given number..  */
    @Override
    public void onCreate() {

        isThreadRunning = true;

        Thread syncThread = new Thread() {
            @Override
            public void run() {

                while (isThreadRunning) {

                    // startHttpRequest in background, also put those new records
                    // to local also..
                    serverManager = new ServerManager(getBaseContext());
                    serverManager.execute();
                    Log.d("Thread 1", "Running");

                    // Sleep the thread for ..
                    try {
                        Thread.sleep(SYNC_THREAD_TIMER);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // play a tune if thread gonna stop, i-e: while loop ended..
                MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.tune);
                mediaPlayer.start();
                mediaPlayer.release();
            }
        };
        // start thread to start syncing data..
        syncThread.start();

        Thread sendThread = new Thread() {
            @Override
            public void run() {

                dbHelper = new data.DBHelper(getBaseContext());

                while (isThreadRunning) {

                    // get sms from local
                    data = dbHelper.topSMS();

                    // check if record isn't empty, i-e: no new sms
                    if (data != null && data[0] != null) {

                        // smsManager instance and SentPI which fire when sms sent..
                        SmsManager smsManager = SmsManager.getDefault();
                        PendingIntent sentPI = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent("SENT"), 0);

                        // check whether sms sent successfully or not..
                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                switch (getResultCode()) {

                                    // if it sent the okay, :)
                                    case Activity.RESULT_OK:
                                        break;

                                    default:

                                        /* if sms have tried for 4 time and had not sent,
                                        *  that means, there is an error, either in network
                                        *  or credablity.. so stop thread and show toast   */
                                        if (smsNotSent > 4) {

                                            DownloadService.isThreadRunning = false;
                                        }

                                        tempQuery = "update sms set status = 0 where id = " + data[0];
                                        dbHelper.execute(tempQuery);
                                        Toast.makeText(getBaseContext(), "error ", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, new IntentFilter("SENT"));

                        // send sms
                        //smsManager.sendTextMessage(data[1], null, data[2], sentPI, null);

                        Log.d("Thread 22222", "Masuk");

                        mId = data[0];
                        mTlp = data[1];
                        mPesan = data[2];
                        mJenis = data[3];
                        mStatus = data[4];

                        //editDataSmsServer(mId,mJenis);

                        String tempUrl = getApplication().getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);
                        tempUrl = tempUrl+"/andara/rest/update-sms.php";

                        try {
                            httpPost(tempUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("Thread 2", "Running");

                    // stop thread for..
                    try {
                        Thread.sleep(SMS_THREAD_TIMER);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                // play a tune when the thread stopped
                MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.tune);
                mediaPlayer.start();
            }
        };
        sendThread.start();
    }


    // show intent when service started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, getResources().getString(R.string.app_name) + " started", Toast.LENGTH_SHORT).show();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    // show intent when service  ended..
    @Override
    public void onDestroy() {
        isThreadRunning = false;
        Toast.makeText(this, getResources().getString(R.string.app_name) + " finished", Toast.LENGTH_SHORT).show();
    }

    // actually i do not end binding..
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //private void editDataSmsServer(String id, String jenis){
    /*private void editDataSmsServer(String id_list){
        Map<String, String> params = new HashMap<String,String>();
        //params.put("id", id);
        //params.put("jenis", jenis);
        params.put("array", id_list);

        String tempUrl = getApplication().getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);
        tempUrl = tempUrl+"/andara/rest/update-sms.php";

        GsonRequest<SuccessObject> serverRequest = new GsonRequest<SuccessObject>(
                Request.Method.POST,
                tempUrl,//Helper.PATH_TO_EDIT_USER,
                SuccessObject.class,
                params,
                createRequestSuccessListener(),
                createRequestErrorListener());

        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getApplication()).addToRequestQueue(serverRequest);
    }

    private Response.Listener<SuccessObject> createRequestSuccessListener() {
        return new Response.Listener<SuccessObject>() {
            @Override
            public void onResponse(SuccessObject response) {
                try {
                    Log.d(TAG, "Json Response " + response.getSuccess());
                    if(response.getSuccess() == 1){
                        //clear shared session
                        // remove added input content
                        Toast.makeText(getApplication(), "yessss", Toast.LENGTH_LONG).show();
                        //username.setText("");
                        //email.setText("");
                        //address.setText("");
                        //phoneNumber.setText("");

                    }else{
                        Toast.makeText(getApplication(), "nooo", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener createRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }
*/


    private String httpPost(String myUrl) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. build JSON object
        JSONObject jsonObject = buidJsonObject();

        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);

        // 4. make POST request to the given URL
        conn.connect();

        // 5. return response message
        return conn.getResponseMessage()+"";

    }

    private JSONObject buidJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("id", mId);
        jsonObject.accumulate("jenis",  mJenis);
        //jsonObject.accumulate("twitter",  etTwitter.getText().toString());

        return jsonObject;
    }

    /*private JSONObject buidJsonObject() throws JSONException {

        mDatabase = new Query(getActivity());
        List<HistoryObject> orderHistory = mDatabase.listHistoryObject();

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        for (int i = 0; i < orderHistory.size(); i++) {

            jsonObject.put("USER_ID", 1);
            jsonObject.put("QUANTITY",  2);
            jsonObject.put("PRICE",  999);
            jsonObject.put("PAYMENT",  "Cash");
            jsonObject.put("TABLE",  7);
            jsonObject.put("ORDER_LIST",  "");

            jsonArr.put(jsonObject);
        }

        return jsonObject;
    }*/

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        //Log.d("Thread 22222", "Masuk");

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        //Log.i(SettingFragment.class.toString(), jsonObject.toString());

        Log.d("Thread 333", jsonObject.toString());

        writer.flush();
        writer.close();
        os.close();
    }

}
