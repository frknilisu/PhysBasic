package com.frkn.physbasic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.frkn.physbasic.R;
import com.frkn.physbasic.functions.DownloaderAsync;
import com.frkn.physbasic.functions.RestorePurchase;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;

public class SplashScreen extends AppCompatActivity implements View.OnClickListener {

    private static int SPLASH_TIME_OUT = 100;
    boolean restoreFlag = false;
    boolean downloadFlag = false;

    /**
     * 0 => free: nothing show, just 3-4 chapter which come initially can be opened, other things are paid.
     * 1 => standart: all chapters can be opened, but tests and special problems and tricks are paid.
     * 2 => premium: all chapters and tests can be opened, but special problems and tricks are paid.
     * 3 => vip: everything can be opened
     */
    private int accountType = 0;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SplashScreen", "onCreate()");
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(this);
        setup();
    }

    private void setup() {
        Log.d("SplashScreen", "setup()");
        loadSettings();
        if (!restoreFlag) {
            //do you want to restore purchase?
            Log.d("SplashScreen", "restorePurchase()..");
            RestorePurchase restorePurchase = new RestorePurchase(this, SplashScreen.this, new RestorePurchase.RestorePurchaseListener() {
                @Override
                public void onRestoreCompleted(int _accountType, int _type, int _id) {
                    Log.d("SplashScreen", "onRestoreCompleted()");
                    accountType = _accountType;
                    restoreFlag = true;
                    saveAccountType();
                    saveRestoreFlag();
                    Toast.makeText(getApplicationContext(),
                            "You have " + accountTypeNames() + " account", Toast.LENGTH_SHORT).show();
                    reloadInception();
                }

                @Override
                public void onRestoreFailed(String response) {
                    Log.d("SplashScreen", "onRestoreFailed()");
                    restoreFlag = false;
                    saveRestoreFlag();
                    Toast.makeText(getApplicationContext(),
                            "Restore Purchase failed: " + response + "\n" + "Check your internet connection!",
                            Toast.LENGTH_SHORT).show();
                    reloadInception();
                }
            });
            restorePurchase.restore();
            //reloadInception();
        } else {
            if (!downloadFlag) {
                reloadInception();
            } else {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        startMainActivity();
                    }
                }, SPLASH_TIME_OUT);
            }
        }
    }

    private void reloadInception() {
        Log.d("SplashScreen", "reloadInception()");
        download_inception_json();
    }

    DownloaderAsync.DownloadListener downloadListener = new DownloaderAsync.DownloadListener() {
        @Override
        public void onTaskCompleted(String response) {
            Log.d("SplashScreen", "onTaskCompleted: " + response);
            downloadFlag = true;
            saveDownloadFlag();
            startMainActivity();
        }

        @Override
        public void onTaskFailed(String response) {
            Log.d("SplashScreen", "onTaskFailed: " + response);
            downloadFlag = false;
            saveDownloadFlag();
            Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.ic_report_problem);
            TextView txt = (TextView) findViewById(R.id.textView);
            txt.setText("Application cannot start");
        }
    };

    private void startMainActivity() {
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private String accountTypeNames() {
        String ret = "";
        switch (accountType) {
            case 0:
                ret = "Free";
                break;
            case 1:
                ret = "Standart";
                break;
            case 2:
                ret = "Premium";
                break;
            case 3:
                ret = "Vip";
                break;
            default:
                ret = "Free";
                break;
        }
        return ret;
    }

    public void download_inception_json() {
        Log.d("Functions", "download_inception_json()..");
        String URL = "https://www.dropbox.com/s/u6q6uno2ro50mv4/inception.json?dl=1";
        DownloaderAsync downloaderAsync = new DownloaderAsync();
        downloaderAsync.setContext(SplashScreen.this);
        downloaderAsync.setListener(downloadListener);
        downloaderAsync.setProcessMessage("Downloading inception json..");
        downloaderAsync.setParentFolderName(null);
        downloaderAsync.setFileName("inception");
        downloaderAsync.setFileExtension(".json");
        downloaderAsync.setFileLength(2607);
        downloaderAsync.execute(URL);
    }

    /*************************************************************************************
     * ************************************************************************************
     * Shared Preferences Functions
     * ************************************************************************************
     *************************************************************************************/
    private static final String PREFS_NAME = "MySharedPrefName";
    SharedPreferences settings;

    private void saveRestoreFlag() {
        SharedPreferences.Editor editor;
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putBoolean("restoreIsOk", restoreFlag);
        editor.commit();
    }

    private void saveDownloadFlag() {
        SharedPreferences.Editor editor;
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putBoolean("downloadIsOk", downloadFlag);
        editor.commit();
    }

    private void saveAccountType() {
        SharedPreferences.Editor editor;
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putInt("accountType", accountType);
        editor.commit();
    }

    private void loadSettings() {
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        accountType = settings.getInt("accountType", 0);
        restoreFlag = settings.getBoolean("restoreIsOk", false);
        downloadFlag = settings.getBoolean("downloadIsOk", false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton:
                setup();
        }
    }

}
