package com.nasirna.autosender;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import data.DownloadService;
import data.SimpleIntentService;

public class MainActivity extends AppCompatActivity {

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }*/

    Button btnStart;

    public static int mJml = 0;

    private TextView mInfoTextView;
    private ProgressBar mProgressBar;

    private MyBroadcastReceiver mMyBroadcastReceiver;
    private UpdateBroadcastReceiver mUpdateBroadcastReceiver;

    private Intent mMyServiceIntent;
    private int mNumberOfIntentService;

    private TextView textView;
    private TextView textViewPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);

        setTitle(getString(R.string.my_tiitle));

        // service start/stop button
        btnStart = (Button) findViewById(R.id.btn_start);

        mInfoTextView = (TextView) findViewById(R.id.textView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        textView = (TextView) findViewById(R.id.main_log);
        textViewPercent = (TextView) findViewById(R.id.textView_percent);

        String tempUrl = getApplication().getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);

        textView.setText("");
        if (tempUrl != null) {
            textView.setText("IP Server : "+tempUrl);
        }else{
            textView.setText("please set IP");
        }

        //mProgressBar.setMax(7);

        // check if app service is already running, if yes
        // update text and text_color..

        if (DownloadService.isThreadRunning) {
            Toast.makeText(this, "app running..", Toast.LENGTH_SHORT).show();

            btnStart.setText("Stop");
            btnStart.setTextColor(Color.RED);
        }

        // buttonStart actionListener
        /*btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Button btnStart = (Button) v;

                // check whether user have set server URL, if not
                // Alert them, AND ALSO RETURN...
                String url = getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);
                if (url == null) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Put Server URL in Settings");
                    dialog.setMessage("\n");
                    dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    return;
                }

                // check whether network connectivity is available, if not
                // Alert them, AND ALSO RETURN..
                if (!new NetworkConnection(getBaseContext()).isConnected()) {
                    DownloadService.isThreadRunning = false;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Network Connection Error");
                    dialog.setMessage("\n");
                    dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    return;
                }

                // check if service already running stop it,
                // else start it and change text and textColor accord..
                if (!DownloadService.isThreadRunning) {
                    DownloadService.isThreadRunning = true;
                    startService(new Intent(getBaseContext(), DownloadService.class));
                    btnStart.setText("Stop");
                    btnStart.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    DownloadService.isThreadRunning = false;
                    stopService(new Intent(getBaseContext(), DownloadService.class));
                    btnStart.setText("Start");
                    btnStart.setTextColor(getResources().getColor(android.R.color.white));
                }


            }
        });*/

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Button btnStart = (Button) v;

                mNumberOfIntentService++;

                // Запускаем свой IntentService
                mMyServiceIntent = new Intent(MainActivity.this,
                        SimpleIntentService.class);

                //startService(mMyServiceIntent.putExtra("time", 3).putExtra("task", "Погладить кота"));
                startService(mMyServiceIntent.putExtra("time", 1).putExtra("task", "Task1"));
                startService(mMyServiceIntent.putExtra("time", 3).putExtra("task", "Task2"));
                //startService(mMyServiceIntent.putExtra("time", 4).putExtra("task", "Поиграть с котом"));


            }
        });

        /*stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMyServiceIntent != null) {
                    stopService(mMyServiceIntent);
                    mMyServiceIntent = null;
                }
            }
        });*/

        mNumberOfIntentService = 0;

        mMyBroadcastReceiver = new MyBroadcastReceiver();
        mUpdateBroadcastReceiver = new UpdateBroadcastReceiver();

        // регистрируем BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(
                SimpleIntentService.ACTION_MYINTENTSERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mMyBroadcastReceiver, intentFilter);

        // Регистрируем второй приёмник
        IntentFilter updateIntentFilter = new IntentFilter(
                SimpleIntentService.ACTION_UPDATE);
        updateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mUpdateBroadcastReceiver, updateIntentFilter);
    }

    // inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // menuitem actionListener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        new OptionMenuListener(this).Perform(item.getItemId());
        return super.onOptionsItemSelected(item);
    }


    //nb

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMyBroadcastReceiver);
        unregisterReceiver(mUpdateBroadcastReceiver);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent
                    .getStringExtra(SimpleIntentService.EXTRA_KEY_OUT);
            //mInfoTextView.setText(result);  dikosongkan nasir

        }
    }

    public class UpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int update = intent
                    .getIntExtra(SimpleIntentService.EXTRA_KEY_UPDATE, 0);
            mProgressBar.setProgress(update);

            //textViewPercent.setText(update + " % Loaded");
            textViewPercent.setText(update + " % ");

            if (update == 100) {
                textViewPercent.setText("Completed");
            }
        }
    }

    @Override
    protected void onResume() {

        String tempUrl = getApplication().getSharedPreferences("com.nasirna.autosender.file", Context.MODE_PRIVATE).getString("url", null);

        textView.setText("");
        if (tempUrl != null) {
            textView.setText("IP Server : "+tempUrl);
        }else{
            textView.setText("please set IP");
        }

        super.onResume();

    }
}
