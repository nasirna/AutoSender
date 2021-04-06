package com.nasirna.autosender;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Samir KHan on 7/19/2016.
 */

/*     THIS CLASS PROVIDE INFORMATION ABOUT CURRENT NETWORK CONNECTIVITY STATE..    */
public class NetworkConnection {

    Context context;

    public NetworkConnection(Context context) {
        this.context = context;
    }

    public Boolean isConnected() {
        ConnectivityManager cm;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
