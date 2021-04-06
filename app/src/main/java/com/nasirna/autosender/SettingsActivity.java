package com.nasirna.autosender;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    Button button;
    EditText urlTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* submit button which save given url
         * and edit text which have URL     */
        button = (Button) findViewById(R.id.btn_url_submit);
        urlTextView = (EditText) findViewById(R.id.text_url);

        // auto enter current URL if exists..
        String currentURL = getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);
        if (currentURL != null)
            urlTextView.setText(currentURL);

        // btnSusbmit clickListener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*  get URL for textEdit
                *   Save it
                *   Clear it and Toast User..   */
                String Url = urlTextView.getText().toString().trim();

                //getSharedPreferences("com.samirkhan.apps.autosender.file", Context.MODE_PRIVATE).getString("url", null);
                SharedPreferences.Editor editor = getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).edit();
                editor.putString("url", Url);
                editor.apply();

                //urlTextView.setText("");

                Toast.makeText(getBaseContext(), "URL saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // menuitem listener..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        new OptionMenuListener(this).Perform(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

}
