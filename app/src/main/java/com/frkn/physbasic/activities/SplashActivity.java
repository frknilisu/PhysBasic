package com.frkn.physbasic.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.frkn.physbasic.R;
import com.frkn.physbasic.helper.Downloader;
import com.frkn.physbasic.helper.DownloaderAsync;

import java.io.File;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity{

    boolean firstTimeFlag = true;
    boolean downloadFlag = false;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 10;
    private DownloadManager downloadManager;
    //private long downloadReference;
    //String link = "https://www.dropbox.com/s/lgia8fx5ajrxq6j/inception.json?dl=1";
    String link2 = "https://www.dropbox.com/sh/dg37lnkoznbvp6a/AAC8s5ORn_YdHrhu_bO9V8Fma?dl=1";

    private final static String downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final static String srcPath = downloadsPath + "/PhysBasic/openChapters.zip";
    private final static String destPath = downloadsPath + "/PhysBasic/";

    ProgressDialog progress;

    Downloader myDownloader;

    /**
     * 0 => nothing show, just 3-4 chapter which come initially can be opened, other things are paid.
     * 1 => bronz, all inception can be opened, but tests and special problems and tricks are paid.
     * 2 => silver, all inception and tests can be opened, but special problems and tricks are paid.
     * 3 => gold, everything can be opened
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

        if (!checkPermissionWithSharedPref()) {
            takePermissions();
        } else {
            Log.d("Permissions", "already have permissions");
            setup();
        }

    }

    private void setup() {
        Log.d("SplashActivity", "setup()");
        loadSettings();
        if (firstTimeFlag) {
            //do you want to restore purchase?
            openRestorePurchaseDialog();
        } else {
            if (!downloadFlag) {

                //set filter to only when download is complete and register broadcast receiver
                /*IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                registerReceiver(downloadReceiver, filter);
                progress = new ProgressDialog(this);*/
                startDownload();

            } else {
                startMainActivity();
            }
        }
    }

    private void startMainActivity() {
        Intent i = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(i);

        // close this activity
        finish();
    }

    private void startDownload() {
        Log.d("SplashActivity", "startDownload()..");
        String URL = "https://www.dropbox.com/sh/x8lop196ibtuaw9/AAASyMe-90DBf6ouqzevU1b2a?dl=1";
        new DownloaderAsync(SplashActivity.this, new DownloaderAsync.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String response) {

            }
        }).execute(URL);
        /*
        progress.setMessage("Downloading Chapters.. :) ");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setProgress(0);
        progress.show();
        File rootFile = new File(downloadsPath, "PhysBasic");
        if (!rootFile.exists()) {
            boolean success = rootFile.mkdirs();
            if (!success)
                Log.e("Mkdirs()", "cannot create dir");
        }

        myDownloader = new Downloader();
        myDownloader.setContext(getApplicationContext());
        myDownloader.setDownloadLink(link2);
        myDownloader.setBroadcastReceiver(downloadReceiver);
        myDownloader.setPath(Environment.DIRECTORY_DOWNLOADS + "/PhysBasic");
        myDownloader.setFilename("openChapters.zip");
        myDownloader.startDownload();
        */

    }

    /*
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our Enqueued download

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.d("Extra", "ReferenceId: " + referenceId);
            if (myDownloader.downloadReference == referenceId) {
                //CheckDwnloadStatus(referenceId);
                progress.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Downloading of data just finished", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
                myDownloader.unzip(srcPath, destPath);
                downloadFlag = true;
                saveSettings(accountType);
                unregisterReceiver(this);
                startMainActivity();
            }
        }


    };*/

    private void CheckDwnloadStatus(long id) {

        // TODO Auto-generated method stub
        DownloadManager.Query query = new DownloadManager.Query();

        query.setFilterById(id);
        Cursor cursor = myDownloader.downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor
                    .getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            int columnReason = cursor
                    .getColumnIndex(DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(columnReason);

            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    String failedReason = "";
                    switch (reason) {
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            failedReason = "ERROR_CANNOT_RESUME";
                            break;
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            failedReason = "ERROR_DEVICE_NOT_FOUND";
                            break;
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            failedReason = "ERROR_FILE_ALREADY_EXISTS";
                            break;
                        case DownloadManager.ERROR_FILE_ERROR:
                            failedReason = "ERROR_FILE_ERROR";
                            break;
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                            failedReason = "ERROR_HTTP_DATA_ERROR";
                            break;
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            failedReason = "ERROR_INSUFFICIENT_SPACE";
                            break;
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            failedReason = "ERROR_TOO_MANY_REDIRECTS";
                            break;
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                            break;
                        case DownloadManager.ERROR_UNKNOWN:
                            failedReason = "ERROR_UNKNOWN";
                            break;
                    }

                    Toast.makeText(this, "FAILED: " + failedReason,
                            Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_PAUSED:
                    String pausedReason = "";

                    switch (reason) {
                        case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                            pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                            break;
                        case DownloadManager.PAUSED_UNKNOWN:
                            pausedReason = "PAUSED_UNKNOWN";
                            break;
                        case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                            pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                            break;
                        case DownloadManager.PAUSED_WAITING_TO_RETRY:
                            pausedReason = "PAUSED_WAITING_TO_RETRY";
                            break;
                    }

                    Toast.makeText(this, "PAUSED: " + pausedReason,
                            Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_PENDING:
                    Toast.makeText(this, "PENDING", Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_RUNNING:
                    Toast.makeText(this, "RUNNING", Toast.LENGTH_LONG).show();
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    //caluclateLoadingData();
                    Toast.makeText(this, "SUCCESSFUL", Toast.LENGTH_LONG).show();
                    // GetFile();
                    break;
            }
        }
    }

    DialogFragment dialogFragment;

    private void openRestorePurchaseDialog() {
        Log.d("SplashActivity", "openRestorePurchaseDialog()..");
        dialogFragment = new RestorePurchaseDialog(new RestorePurchaseDialog.RestorePurchaseListener() {
            @Override
            public void onFinishRestore(int _accountType, int _type, int _id) {
                Log.d("SplashActivity", "onFinishRestore()");
                accountType = _accountType;
                if (_accountType == 0) {

                } else {
                    saveSettings(_accountType);
                }
                if (!downloadFlag) {
                    //set filter to only when download is complete and register broadcast receiver
                    IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                    registerReceiver(downloadReceiver, filter);
                    progress = new ProgressDialog(this);
                    startDownload();
                }
            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(dialogFragment, "loading");
        transaction.commitAllowingStateLoss();
        //FragmentManager fm = getSupportFragmentManager();
        //dialogFragment.show(fm, "Restore Purchase");
    }



    /*************************************************************************************
     * ************************************************************************************
     * TAKE PERMISSIONS
     * ************************************************************************************
     *************************************************************************************/
    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void takePermissions() {
        Log.d("Permissions", "takePermissions()..");
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                Log.d("Permissions", "already have permissions");
                setup();
            }
        } else {
            Log.d("Permissions", "Under Lollipop version: already have permissions");
            setup();
        }
    }

    private boolean checkIfAlreadyhavePermission() {
        for (int i = 0; i < permissions.length; i++) {
            int result = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, permissions, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean flag = true;
        switch (requestCode) {
            case 101:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.e("Permissions", "No permission: " + permissions[i].toString());
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    Log.d("Permissions", "all permissions are taken");
                    setup();
                } else {
                    Log.e("Permissions", "some permission has not taken");
                    takePermissions();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /*************************************************************************************
     * ************************************************************************************
     * Shared Preferences Functions
     * ************************************************************************************
     *************************************************************************************/
    private static final String PREFS_NAME = "MySharedPrefName";
    SharedPreferences settings;

    private boolean checkPermissionWithSharedPref() {
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean("Permissions", false);
    }

    private void saveSettings(int _accountType) {
        SharedPreferences.Editor editor;
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putInt("accountType", _accountType);
        editor.putBoolean("firstTime", false);
        editor.putBoolean("downloadOk", true);
        editor.commit();
    }

    private void loadSettings() {
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        accountType = settings.getInt("accountType", 0);
        firstTimeFlag = settings.getBoolean("firstTime", true);
        downloadFlag = settings.getBoolean("downloadOk", false);
    }

}
