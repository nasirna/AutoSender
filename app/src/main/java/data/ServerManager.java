package data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nasirna.autosender.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Samir KHan on 7/18/2016.
 */

/* THIS CLASS ALWAYS RUN IN A BACKGROUND..
*  IT GET NEW RECORDS FROM SERVER AND PUT THOSE
*  IN LOCAL..   */
public class ServerManager extends AsyncTask<String, Void, String> {

    Context context;
    HttpURLConnection connection;
    URL url;
    BufferedReader reader;
    data.DBHelper dbHelper;

    public ServerManager(Context context) {
        this.context = context;
    }


    @Override
    protected String doInBackground(String... params) {

        dbHelper = new data.DBHelper(this.context);
        try {
            JSONArray jsonArray;
            JSONObject jsonObject;

            // get URL from sharedPrefrences, and then open connection..
            String tempUrl = context.getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);
            tempUrl = tempUrl+"/andara/rest/get-sms-all.php";
            url = new URL(tempUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // get the stream and put into json array
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            //jsonArray = new JSONArray(bufferedReaderToString(reader));
            String jsonStr = bufferedReaderToString(reader);

            JSONObject jsonObj = new JSONObject(jsonStr);

            // Getting JSON Array node
            JSONArray sms = jsonObj.getJSONArray("sms");

            MainActivity.mJml = sms.length();

            // get each record from array and save it at local..
            //for (int i = 0; i < jsonArray.length(); i++) {
            for (int i = 0; i < sms.length(); i++) {
                jsonObject = sms.getJSONObject(i);
                String id = jsonObject.getString("id");
                //String id ="";
                String contact = jsonObject.getString("telp");
                String message = jsonObject.getString("pesan");
                String jenis = jsonObject.getString("jenis");
                String status = "0";

                dbHelper.deleteItem(id);
                dbHelper.insert(id, contact, message, jenis, status);
                //dbHelper.insert(contact, message, status);
            }
        } catch (MalformedURLException e) {
            DownloadService.isThreadRunning = false;
            Log.d("ServerManager", "URL format error");
            e.printStackTrace();
        } catch (IOException e) {
            DownloadService.isThreadRunning = false;
            Log.d("ServerManager", "server not available");

            e.printStackTrace();
        } catch (JSONException e) {
            DownloadService.isThreadRunning = false;
            Log.d("ServerManager", "data format error");
            e.printStackTrace();
        }

        return null;
    }

    // convert bufferReader to String...
    public String bufferedReaderToString(BufferedReader reader) throws IOException {
        String data = "";
        String line;
        while ((line = reader.readLine()) != null) {
            data = data + line + "\n";
        }
        return data;
    }

}
