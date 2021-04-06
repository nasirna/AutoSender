package data;


import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nasirna.autosender.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.app.DownloadManager.STATUS_RUNNING;
import static android.content.ContentValues.TAG;
import static android.drm.DrmInfoStatus.STATUS_ERROR;


public class SimpleIntentService extends IntentService {

    private final String TAG = "SMSServiceLogs";

    public static final String ACTION_MYINTENTSERVICE = "ru.alexanderklimov.intentservice.RESPONSE";
    public static final String EXTRA_KEY_OUT = "EXTRA_OUT";
    public static final String ACTION_UPDATE = "ru.alexanderklimov.intentservice.UPDATE";
    public static final String EXTRA_KEY_UPDATE = "EXTRA_UPDATE";
    public static final String JML_SMS = "JML_SMS";
    private static final int NOTIFICATION_ID = 1;
    String extraOut = "Кота накормили, погладили и поиграли с ним";
    private NotificationManager mNotificationManager;

    private boolean mIsSuccess;
    private boolean mIsStopped;


    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    BufferedReader reader;
    data.DBHelper dbHelper;
    String[] data;
    String tempQuery;
    int smsNotSent = 0;

    private String mId, mTlp, mPesan, mJenis, mStatus;
    public static Boolean isThreadRunning = false;
    int SYNC_THREAD_TIMER = 1000 * 30;
    int SMS_THREAD_TIMER = 1000 * 5;

    private int j,k,hasil;
    private double a,b;

    private BroadcastReceiver sendBroadcastReceiver;
    private BroadcastReceiver deliveryBroadcastReceiver;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";


    public SimpleIntentService() {
        super("SimpleIntentService");
        mIsSuccess = false;
        mIsStopped = false;
    }

    public void onCreate() {
        super.onCreate();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "onCreate");

        isThreadRunning = true;

        sendBroadcastReceiver = new BroadcastReceiver()
        {

            public void onReceive(Context arg0, Intent arg1)
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        deliveryBroadcastReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context arg0, Intent arg1)
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
        registerReceiver(sendBroadcastReceiver , new IntentFilter(SENT));

    }

    @Override
    public void onDestroy() {
        String notice;

        mIsStopped = true;

        if (mIsSuccess) {
            //notice = "onDestroy with success";
            notice = "Success";

        } else {
            //notice = "onDestroy WITHOUT success!";
            notice = "Gagal!";
        }

        // play a tune when the thread stopped
        MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.tune);
        mediaPlayer.start();

        Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG)
                .show();

        unregisterReceiver(sendBroadcastReceiver);
        unregisterReceiver(deliveryBroadcastReceiver);

        super.onDestroy();

    }

    /*@Override
    protected void onHandleIntent(Intent intent) {

        int tm = intent.getIntExtra("time", 0);
        String label = intent.getStringExtra("task");
        Log.d(TAG, "onHandleIntent start: " + label);
        try {
            TimeUnit.SECONDS.sleep(tm);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onHandleIntent end: " + label);

        for (int i = 0; i <= 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(mIsStopped){
                break;
            }

            // посылаем промежуточные данные
            Intent updateIntent = new Intent();
            updateIntent.setAction(ACTION_UPDATE);
            updateIntent.addCategory(Intent.CATEGORY_DEFAULT);
            updateIntent.putExtra(EXTRA_KEY_UPDATE, i);
            sendBroadcast(updateIntent);

            mIsSuccess = true;

            // формируем уведомление
            String notificationText = String.valueOf((100 * i / 10))
                    + " %";
            Notification notification = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Progress")
                        .setContentText(notificationText)
                        .setTicker("Notification!")
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher)
                        .build();
            }

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }

        // возвращаем результат
        Intent responseIntent = new Intent();
        responseIntent.setAction(ACTION_MYINTENTSERVICE);
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
        responseIntent.putExtra(EXTRA_KEY_OUT, extraOut);
        sendBroadcast(responseIntent);
    }*/

    @Override
    protected void onHandleIntent(Intent intent) {

        int tm = intent.getIntExtra("time", 0);
        String label = intent.getStringExtra("task");
        Log.d(TAG, "onHandleIntent start: " + label);
        try {
            TimeUnit.SECONDS.sleep(tm);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onHandleIntent end: " + label);

        Log.d(TAG, "Service Started!");


        if (label.equals("Task1")) {

            dbHelper = new data.DBHelper(this.getApplication());

            //while (isThreadRunning) {
                final ResultReceiver receiver = intent.getParcelableExtra("receiver");
                // String url = intent.getStringExtra("url");

                String url = getApplication().getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);
                url = "http://"+ url + "/andara/rest/get-sms-all.php";

                //Bundle bundle = new Bundle();

                if (!TextUtils.isEmpty(url)) {

                    /* Update UI: Download Service is Running */
                    //receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                    Log.d(TAG,"task 1 is running");

                    try {
                        String[] results = downloadData(url);

                        /* Sending result back to activity */
                        if (null != results && results.length > 0) {

                            //bundle.putStringArray("result", results);
                            //receiver.send(STATUS_FINISHED, bundle);

                        }
                    } catch (Exception e) {

                        /* Sending error message back to activity */
                        //bundle.putString(Intent.EXTRA_TEXT, e.toString());
                        //receiver.send(STATUS_ERROR, bundle);
                    }

                    // Sleep the thread for ..
                    /*try {
                        Thread.sleep(SYNC_THREAD_TIMER);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }

                // play a tune if thread gonna stop, i-e: while loop ended..
                MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.tune);
                mediaPlayer.start();
                mediaPlayer.release();

            //}

        } else {
            dbHelper = new data.DBHelper(this.getApplication());

            while (isThreadRunning) {

                // get sms from local
                data = dbHelper.topSMS();

                // check if record isn't empty, i-e: no new sms
                if (data != null && data[0] != null) {

                    // smsManager instance and SentPI which fire when sms sent..
                    //SmsManager smsManager = SmsManager.getDefault();
                    //PendingIntent sentPI = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent("SENT"), 0);

                    sendSMS(data[1],data[2]);

                    // check whether sms sent successfully or not..
                   /*registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            switch (getResultCode()) {

                                // if it sent the okay, :)
                                case Activity.RESULT_OK:
                                    break;

                                default:

                                    *//* if sms have tried for 4 time and had not sent,
                                     *  that means, there is an error, either in network
                                     *  or credablity.. so stop thread and show toast   *//*
                                    if (smsNotSent > 4) {

                                        DownloadService.isThreadRunning = false;
                                    }

                                    tempQuery = "update sms set status = 0 where id = " + data[0];
                                    dbHelper.execute(tempQuery);
                                    Toast.makeText(getBaseContext(), "error ", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            unregisterReceiver(this);//add here
                        }
                    }, new IntentFilter("SENT"));*/

                    // send sms
                    //smsManager.sendTextMessage(data[1], null, data[2], sentPI, null);

                    Log.d(TAG, "task 2 SMS Masuk");

                    mId = data[0];
                    mTlp = data[1];
                    mPesan = data[2];
                    mJenis = data[3];
                    mStatus = data[4];

                    //editDataSmsServer(mId,mJenis);

                    String tempUrl = getApplication().getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);
                    tempUrl = "http://"+ tempUrl + "/andara/rest/update-sms.php";

                    try {
                        httpPost(tempUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {

                    isThreadRunning = false;
                }
                Log.d(TAG,"task 2 is running");

                // stop thread for..
                /*try {
                    Thread.sleep(SMS_THREAD_TIMER);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

            }
            // play a tune when the thread stopped
            //MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.tune);
            //mediaPlayer.start();

        }

        Log.d(TAG, "Service Stopping!");
        //this.stopSelf();

        // возвращаем результат
        Intent responseIntent = new Intent();
        responseIntent.setAction(ACTION_MYINTENTSERVICE);
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
        responseIntent.putExtra(EXTRA_KEY_OUT, extraOut);
        sendBroadcast(responseIntent);

    }

    //nb

    public void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        SmsManager sms = SmsManager.getDefault();
        //sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        String toPhoneNumber = phoneNumber;
        String smsMessage = message;

        //SmsManager smsManager = SmsManager.getDefault();

        ArrayList<String> parts = sms.divideMessage(smsMessage);
        //smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        sms.sendMultipartTextMessage(toPhoneNumber, null, parts, null, null);
    }

    private String[] downloadData(String requestUrl) throws IOException, DownloadException {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        /* forming th java.net.URL object */

        URL url = new URL(requestUrl);
        urlConnection = (HttpURLConnection) url.openConnection();

        /* optional request header */
        urlConnection.setRequestProperty("Content-Type", "application/json");

        /* optional request header */
        urlConnection.setRequestProperty("Accept", "application/json");

        /* for Get request */
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();

        if (statusCode == 200) {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String response = convertInputStreamToString(inputStream);
            String[] results = parseResult(response);
            return results;
        } else {
            throw new DownloadException("Failed to fetch data!!");
        }

        /*reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String response = convertInputStreamToString(inputStream);
        String[] results = parseResult(response);
        return results;*/
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        /* Close Stream */
        if (null != inputStream) {
            inputStream.close();
        }

        return result;
    }

    private String[] parseResult(String result) {

        String[] blogTitles = null;
        try {
            JSONObject response = new JSONObject(result);
            JSONArray sms = response.optJSONArray("sms");
            blogTitles = new String[sms.length()];

            /*for (int i = 0; i < sms.length(); i++) {
                JSONObject post = sms.optJSONObject(i);
                String title = post.optString("title");
                blogTitles[i] = title;
            }*/

           // JSONObject jsonObject = new JSONObject(result);

            // посылаем промежуточные данные
            /*Intent updateIntent1 = new Intent();
            updateIntent1.setAction(ACTION_UPDATE);
            updateIntent1.addCategory(Intent.CATEGORY_DEFAULT);
            updateIntent1.putExtra(JML_SMS, sms.length());
            sendBroadcast(updateIntent1);*/


            for (int i = 0; i < sms.length(); i++) {
                response = sms.getJSONObject(i);
                String id = response.getString("id");
                //String id ="";
                String contact = response.getString("telp");
                String message = response.getString("pesan");
                String jenis = response.getString("jenis");
                String status = "0";

                dbHelper.deleteItem(id);
                dbHelper.insert(id, contact, message, jenis, status);

                j=sms.length();
                k=sms.length()-1;
                //a = i / j;

                double angka1 = Double.parseDouble(String.valueOf(i));
                double angka2 = Double.parseDouble(String.valueOf(j));
                a = angka1 / angka2;

                b = 100* a;

                hasil = (int) b;

                String notificationText;
                if (i==k){
                    hasil=100;
                    notificationText = String.valueOf(100) + " %";
                } else {
                    notificationText = String.valueOf((100 * hasil / sms.length())) + " %";
                }


                //nb

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(mIsStopped){
                    break;
                }

                // посылаем промежуточные данные
                Intent updateIntent = new Intent();
                updateIntent.setAction(ACTION_UPDATE);
                updateIntent.addCategory(Intent.CATEGORY_DEFAULT);
                updateIntent.putExtra(EXTRA_KEY_UPDATE, hasil);
                sendBroadcast(updateIntent);

                mIsSuccess = true;

                // формируем уведомление
                //String notificationText = String.valueOf((100 * j / sms.length())) + " %";

                Notification notification = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    notification = new Notification.Builder(getApplicationContext())
                            .setContentTitle("Progress")
                            .setContentText(notificationText)
                            .setTicker("Notification!")
                            .setWhen(System.currentTimeMillis())
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher)
                            .build();
                }

                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return blogTitles;
    }

    public class DownloadException extends Exception {

        public DownloadException(String message) {
            super(message);
        }

        public DownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    //nb lagi

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

        Log.d(TAG, jsonObject.toString());

        writer.flush();
        writer.close();
        os.close();
    }

}
