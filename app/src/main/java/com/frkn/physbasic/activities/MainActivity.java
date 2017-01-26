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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.frkn.physbasic.Chapter;
import com.frkn.physbasic.R;
import com.frkn.physbasic.Specials;
import com.frkn.physbasic.Test;
import com.frkn.physbasic.adapters.ChapterAdapter;
import com.frkn.physbasic.adapters.SpecialsAdapter;
import com.frkn.physbasic.helper.DividerItemDecoration;
import com.frkn.physbasic.helper.Downloader;
import com.frkn.physbasic.helper.RecyclerTouchListener;
import com.frkn.physbasic.adapters.TestAdapter;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements BuyPremiumDialog.BuyPremiumListener {


    JSONObject inceptionJson = null;
    int chaptersCount, testsCount, specialsCount;

    private List<Chapter> chapterList = new ArrayList<>();
    private List<Test> testList = new ArrayList<>();
    private List<Specials> specialsList = new ArrayList<>();
    private ChapterAdapter chapterAdapter;
    private TestAdapter testAdapter;
    private SpecialsAdapter specialsAdapter;
    RecyclerTouchListener chapterTouchListener;
    RecyclerTouchListener testTouchListener;
    RecyclerTouchListener specialsTouchListener;
    int currentTouchListenerType = 0;

    Toolbar toolbar;
    RecyclerView recyclerView;
    BottomNavigationView bottomNavigationView;

    Downloader myDownloader;

    /**
     * 0 => nothing show, just 3-4 chapter which come initially can be opened, other things are paid.
     * 1 => bronz, all inception can be opened, but tests and special problems and tricks are paid.
     * 2 => silver, all inception and tests can be opened, but special problems and tricks are paid.
     * 3 => gold, everything can be opened
     */
    private int accountType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        chapterAdapter = new ChapterAdapter(chapterList);
        testAdapter = new TestAdapter(testList);
        specialsAdapter = new SpecialsAdapter(specialsList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        setup();
    }

    private void setup() {
        loadSettings();
        readInceptionJson();
        loadChaptersToRecycler();

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_tests:
                                loadTestsToRecycler();
                                break;
                            case R.id.action_all_chapters:
                                loadChaptersToRecycler();
                                break;
                            case R.id.action_specials_problems:
                                loadSpecialsToRecycler();
                                break;
                        }
                        return false;
                    }
                });


    }

    private void readInceptionJson(){
        try {
            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/PhysBasic/inception.json");
            InputStream is = new FileInputStream(f);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }

            inceptionJson = new JSONObject(writer.toString());
            JSONArray jsonArray = null;
            jsonArray = inceptionJson.getJSONArray("chapters");
            chaptersCount = jsonArray.length();

            jsonArray = inceptionJson.getJSONArray("tests");
            testsCount = jsonArray.length();

            jsonArray = inceptionJson.getJSONArray("specials");
            specialsCount = jsonArray.length();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadChapterList() {
        Log.d("setup", "loadChapterList()..");
        chapterList.clear();
        try {
            JSONArray jArray = inceptionJson.getJSONArray("chapters");
            for (int i = 0; i < jArray.length(); i++) {
                chapterList.add(new Chapter(jArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadChaptersToRecycler(){
        loadChapterList();
        recyclerView.setAdapter(chapterAdapter);
        chapterAdapter.notifyDataSetChanged();
        if(currentTouchListenerType == 1)
            return;

        if(currentTouchListenerType == 2)
            recyclerView.removeOnItemTouchListener(testTouchListener);
        else if(currentTouchListenerType == 3)
            recyclerView.removeOnItemTouchListener(specialsTouchListener);

        if(currentTouchListenerType != 1) {

            chapterTouchListener = new RecyclerTouchListener(getApplicationContext(), recyclerView,
                    new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {

                            Chapter chapter = chapterList.get(position);
                            if (accountType == 0 && chapter.isLock()) {
                                openUpgradeDialog(1, chapter.getId(), chapter.getTitle(), "1 TL");
                            } else {
                                //openChapter(chapter);
                                openSelectedItem(1, chapter.getId(), chapter.getLength());
                            }
                        }

                        @Override
                        public void onLongClick(View view, int position) {
                            //Chapter chapter = chapterList.get(position);
                            //showFormulas(chapter);
                        }
                    });
            recyclerView.addOnItemTouchListener(chapterTouchListener);
            currentTouchListenerType = 1;
        }
    }

    private void loadTestList() {
        Log.d("setup", "loadTestList()..");
        testList.clear();
        try {
            JSONArray jArray = inceptionJson.getJSONArray("tests");
            for (int i = 0; i < jArray.length(); i++) {
                testList.add(new Test(jArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadTestsToRecycler(){
        loadTestList();
        recyclerView.setAdapter(testAdapter);
        testAdapter.notifyDataSetChanged();
        if(currentTouchListenerType == 2)
            return;

        if(currentTouchListenerType == 1)
            recyclerView.removeOnItemTouchListener(chapterTouchListener);
        else if(currentTouchListenerType == 3)
            recyclerView.removeOnItemTouchListener(specialsTouchListener);

        if(currentTouchListenerType != 2) {

            testTouchListener = new RecyclerTouchListener(getApplicationContext(), recyclerView,
                    new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {

                            Test test = testList.get(position);
                            if (accountType <= 1) {
                                openUpgradeDialog(2, test.getId(), test.getTitle(), "2 TL");
                            } else {
                                openSelectedItem(2, test.getId(), 10);
                                //openChapter(test);
                            }
                        }

                        @Override
                        public void onLongClick(View view, int position) {
                            //Chapter chapter = chapterList.get(position);
                            //showFormulas(chapter);
                        }
                    });
            recyclerView.addOnItemTouchListener(testTouchListener);
            currentTouchListenerType = 2;
        }
    }

    private void loadSpecialList() {
        Log.d("setup", "loadSpecialList()..");
        specialsList.clear();
        try {
            JSONArray jArray = inceptionJson.getJSONArray("specials");
            for (int i = 0; i < jArray.length(); i++) {
                specialsList.add(new Specials(jArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadSpecialsToRecycler(){
        loadSpecialList();
        recyclerView.setAdapter(specialsAdapter);
        specialsAdapter.notifyDataSetChanged();
        if(currentTouchListenerType == 3)
            return;

        if(currentTouchListenerType == 1)
            recyclerView.removeOnItemTouchListener(chapterTouchListener);
        else if(currentTouchListenerType == 2)
            recyclerView.removeOnItemTouchListener(testTouchListener);

        if(currentTouchListenerType != 3) {

            specialsTouchListener = new RecyclerTouchListener(getApplicationContext(), recyclerView,
                    new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {

                            Specials specials = specialsList.get(position);
                            if (accountType <= 2) {
                                openUpgradeDialog(3, specials.getId(), specials.getTitle(), "3 TL");
                            } else {
                                //openChapter(test);
                                openSelectedItem(3, specials.getId(), 10);
                            }
                        }

                        @Override
                        public void onLongClick(View view, int position) {
                            //Chapter chapter = chapterList.get(position);
                            //showFormulas(chapter);
                        }
                    });
            recyclerView.addOnItemTouchListener(specialsTouchListener);
            currentTouchListenerType = 3;
        }
    }


    public final static String EXTRA_MESSAGE = "com.frkn.physbasic.MESSAGE";

    private void openChapter(Chapter chapter) {
        Log.d("openChapter", "chapterId: " + chapter.getId());
        Intent intent = new Intent(MainActivity.this, ShowChapter.class);
        intent.putExtra(EXTRA_MESSAGE + "_chapterId", String.valueOf(chapter.getId()));
        startActivity(intent);
    }

    private void openSelectedItem(int type, int id, int length) {
        Log.d("openSelectedItem", "type: " + type + ", id: " + id + ", length: " + length);
        Intent intent = new Intent(MainActivity.this, ShowImages.class);
        intent.putExtra(EXTRA_MESSAGE + "_type", String.valueOf(type));
        intent.putExtra(EXTRA_MESSAGE + "_id", String.valueOf(id));
        intent.putExtra(EXTRA_MESSAGE + "_length", String.valueOf(length));
        startActivity(intent);
    }


    /*************************************************************************************
     * Open Fragments
     *************************************************************************************/
    DialogFragment dialogFragment;

    private void openUpgradeDialog(int type, int id, String title, String price) {
        dialogFragment = new BuyPremiumDialog();
        Bundle args = new Bundle();
        args.putInt("accountType", accountType);
        args.putInt("buyingThingType", type);
        args.putInt("id", id);
        args.putString("title", title);
        args.putString("price", price);
        dialogFragment.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        dialogFragment.show(fm, "Buy Premium");
    }


    private void openDownloadDialog(){
        dialogFragment = new BuyPremiumDialog();
        Bundle args = new Bundle();
        args.putInt("accountType", accountType);
        dialogFragment.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        dialogFragment.show(fm, "Downloads");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        dialogFragment.onActivityResult(requestCode, resultCode, data);
        dialogFragment.onDestroyView();
    }

    @Override
    public void onFinishBuying(int _accountType, int _type, int _id) {
        int old_account_type = accountType;
        accountType = _accountType;
        Log.d("onFinishBuying", "accountType: " + accountType);
        saveSettings(accountType);
        if(old_account_type == 0){
            if(accountType == 0 && _type != -1 && _id != -1) {
                initializeDownloading();
                dowload_one_Chapter(_id);
            }
            if(accountType >= 1) {
                initializeDownloading();
                dowload_all_Chapters();
            }
            if(accountType >= 2) {
                initializeDownloading();
                dowload_all_Tests();
            }
            if(accountType >= 3) {
                initializeDownloading();
                dowload_all_Specials();
            }
        } else if(old_account_type == 1){
            if(accountType == 1 && _type != -1 && _id != -1) {
                initializeDownloading();
                dowload_one_Test(_id);
            }
            if(accountType >= 2) {
                initializeDownloading();
                dowload_all_Tests();
            }
            if(accountType >= 3) {
                initializeDownloading();
                dowload_all_Specials();
            }
        } else if(old_account_type == 2){
            if(accountType == 2 && _type != -1 && _id != -1) {
                initializeDownloading();
                dowload_one_Special(_id);
            }
            if(accountType >= 3) {
                initializeDownloading();
                dowload_all_Specials();
            }
        }
    }

    ProgressDialog progress;
    private void initializeDownloading(){
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
        progress = new ProgressDialog(this);
        progress.setMessage("Downloading.. :) ");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setProgress(0);
        progress.show();
    }

    private void dowload_all_Chapters(){
        String link = "https://www.dropbox.com/sh/z2n6pwz0e0i3n62/AACe-gTieVydGjf2ggtoXzeZa?dl=1";
        myDownloader = new Downloader();
        myDownloader.setContext(getApplicationContext());
        myDownloader.setDownloadLink(link);
        myDownloader.setBroadcastReceiver(downloadReceiver);
        myDownloader.setPath(Environment.DIRECTORY_DOWNLOADS + "/PhysBasic");
        myDownloader.setFilename("closeChapters.zip");
        myDownloader.startDownload();
    }

    private void dowload_all_Tests(){
        String link = "";
        myDownloader = new Downloader();
        myDownloader.setContext(getApplicationContext());
        myDownloader.setDownloadLink(link);
        myDownloader.setBroadcastReceiver(downloadReceiver);
        myDownloader.setPath(Environment.DIRECTORY_DOWNLOADS + "/PhysBasic");
        myDownloader.setFilename("tests.zip");
        myDownloader.startDownload();
    }

    private void dowload_all_Specials(){
        String link = "";
        myDownloader = new Downloader();
        myDownloader.setContext(getApplicationContext());
        myDownloader.setDownloadLink(link);
        myDownloader.setBroadcastReceiver(downloadReceiver);
        myDownloader.setPath(Environment.DIRECTORY_DOWNLOADS + "/PhysBasic");
        myDownloader.setFilename("specials.zip");
        myDownloader.startDownload();
    }

    private void dowload_one_Chapter(int id){
        myDownloader = new Downloader();
        myDownloader.setContext(getApplicationContext());
        myDownloader.setDownloadLink(chapterList.get(id-1).getLink());
        myDownloader.setBroadcastReceiver(downloadReceiver);
        myDownloader.setPath(Environment.DIRECTORY_DOWNLOADS + "/PhysBasic/chapters");
        myDownloader.setFilename(id + ".zip");
        myDownloader.startDownload();
    }

    private void dowload_one_Test(int id){
        myDownloader = new Downloader();
        myDownloader.setContext(getApplicationContext());
        //myDownloader.setDownloadLink(testList.get(id-1).getLink());
        myDownloader.setBroadcastReceiver(downloadReceiver);
        myDownloader.setPath(Environment.DIRECTORY_DOWNLOADS + "/PhysBasic/tests");
        myDownloader.setFilename(id + ".zip");
        myDownloader.startDownload();
    }

    private void dowload_one_Special(int id){
        myDownloader = new Downloader();
        myDownloader.setContext(getApplicationContext());
        //myDownloader.setDownloadLink(specialsList.get(id-1).getLink());
        myDownloader.setBroadcastReceiver(downloadReceiver);
        myDownloader.setPath(Environment.DIRECTORY_DOWNLOADS + "/PhysBasic/specials");
        myDownloader.setFilename(id + ".zip");
        myDownloader.startDownload();
    }

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
                String downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                String srcPath = downloadsPath + "/PhysBasic/closeChapters.zip";
                String destPath = downloadsPath + "/PhysBasic/";
                myDownloader.unzip(srcPath, destPath);
                saveSettings(accountType);
                unregisterReceiver(this);
            }
        }


    };


    /*************************************************************************************
     * Shared Preferences Functions
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
        editor.commit();
    }

    private void loadSettings() {
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        accountType = settings.getInt("accountType", 0);
    }


    /*************************************************************************************
     * Toolbar Menu
     *************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_upgrade:
                //openUpgradeDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
