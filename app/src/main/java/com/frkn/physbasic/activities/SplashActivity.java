package com.frkn.physbasic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.frkn.physbasic.R;
import com.frkn.physbasic.helper.DownloaderAsync;
import com.frkn.physbasic.helper.RestorePurchase;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 50;
    boolean firstTimeFlag = true;

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
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);


        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("SplashActivity", "Refreshed token: " + refreshedToken);;
        Log.d("SplashActivity", "int: " + R.drawable.thermomet2);
        setup();
    }

    private void setup() {
        Log.d("SplashActivity", "setup()");
        loadSettings();
        if (firstTimeFlag) {
            if (isOnline()) {
                //do you want to restore purchase?
                Log.d("SplashActivity", "restorePurchase()..");
                RestorePurchase restorePurchase = new RestorePurchase(this, SplashActivity.this, new RestorePurchase.RestorePurchaseListener() {
                    @Override
                    public void onRestoreCompleted(int _accountType, int _type, int _id) {
                        Log.d("SplashActivity", "onRestoreCompleted()");
                        accountType = _accountType;
                        saveSettings();
                        Toast.makeText(getApplicationContext(),
                                "You have " + accountTypeNames() + " account", Toast.LENGTH_SHORT).show();
                        reloadInception();
                    }

                    @Override
                    public void onRestoreFailed(String response) {
                        Log.d("SplashActivity", "onRestoreFailed()");
                        Toast.makeText(getApplicationContext(),
                                "Restore Purchase failed: " + response + "\n" + "Check your internet connection!",
                                Toast.LENGTH_SHORT).show();
                        reloadInception();
                    }
                });
                restorePurchase.restore();
                //reloadInception();
            } else {
                TextView txt = (TextView) findViewById(R.id.textView2);
                txt.setText("App cannot start because of internet connection!");
            }
        } else {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startMainActivity();
                }
            }, SPLASH_TIME_OUT);
        }
    }

    private void reloadInception() {
        Log.d("SplashActivity", "reloadInception()");
        download_inception_json();
    }

    DownloaderAsync.OnTaskCompleted onTaskCompleted = new DownloaderAsync.OnTaskCompleted() {
        @Override
        public void onTaskCompleted(String response) {
            Log.d("SplashActivity", "onTaskCompleted: " + response);
            saveSettings();
            startMainActivity();
        }
    };

    private void startMainActivity() {
        Intent i = new Intent(SplashActivity.this, MainActivity.class);
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
        downloaderAsync.setContext(SplashActivity.this);
        downloaderAsync.setListener(onTaskCompleted);
        downloaderAsync.setProcessMessage("Downloading inception json..");
        downloaderAsync.setParentFolderName(null);
        downloaderAsync.setFileName("inception");
        downloaderAsync.setFileExtension(".json");
        downloaderAsync.setFileLength(2607);
        if (isOnline()) {
            Log.d("isOnline", "Your are online. Now can start download");
            downloaderAsync.execute(URL);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /*************************************************************************************
     * ************************************************************************************
     * Shared Preferences Functions
     * ************************************************************************************
     *************************************************************************************/
    private static final String PREFS_NAME = "MySharedPrefName";
    SharedPreferences settings;

    private void saveSettings() {
        SharedPreferences.Editor editor;
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putInt("accountType", accountType);
        editor.putBoolean("firstTime", false);
        editor.commit();
    }

    private void loadSettings() {
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        accountType = settings.getInt("accountType", 0);
        firstTimeFlag = settings.getBoolean("firstTime", true);
    }

}
