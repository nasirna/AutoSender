package com.nasirna.autosender;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Samir KHan on 7/18/2016.
 */

/*  THIS CLASS REDIRECT USER TO ANOTHER ACTIVITY ACCOR TO OPTION MENU ITEM  */
public class OptionMenuListener {

    Context context;
    public OptionMenuListener(Context context){
        this.context = context;
    }

    public void Perform(int id){
        Intent intent = null;
        if (id == R.id.menuitem_settings) {
            intent = new Intent(context, SettingsActivity.class);

        } /*else if (id == R.id.menuitem_about) {
            intent = new Intent(context, AboutActivity.class);

        }*/
        context.startActivity(intent);
    }
}
