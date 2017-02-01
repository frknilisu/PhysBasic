package com.frkn.physbasic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.frkn.physbasic.Chapter;
import com.frkn.physbasic.R;
import com.frkn.physbasic.Specials;
import com.frkn.physbasic.Test;
import com.frkn.physbasic.adapters.ChapterAdapter;
import com.frkn.physbasic.adapters.SpecialsAdapter;
import com.frkn.physbasic.adapters.TestAdapter;
import com.frkn.physbasic.helper.DividerItemDecoration;
import com.frkn.physbasic.helper.DownloaderAsync;
import com.frkn.physbasic.helper.RecyclerTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BuyPremiumDialog.BuyPremiumListener {


    public static JSONObject inceptionJson = null;
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

    int BUFFER_SIZE = 1024;

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
        chapterAdapter.setAccountType(accountType);
        testAdapter.setAccountType(accountType);
        specialsAdapter.setAccountType(accountType);
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

    private void readInceptionJson() {
        try {
            FileInputStream fin = new FileInputStream(new File(this.getFilesDir(), "inception.json"));
            Writer writer = new StringWriter();
            char[] buffer = new char[BUFFER_SIZE];
            Reader reader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
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
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

    private void loadChaptersToRecycler() {
        loadChapterList();
        recyclerView.setAdapter(chapterAdapter);
        chapterAdapter.notifyDataSetChanged();
        if (currentTouchListenerType == 1)
            return;

        if (currentTouchListenerType == 2)
            recyclerView.removeOnItemTouchListener(testTouchListener);
        else if (currentTouchListenerType == 3)
            recyclerView.removeOnItemTouchListener(specialsTouchListener);

        if (currentTouchListenerType != 1) {

            chapterTouchListener = new RecyclerTouchListener(getApplicationContext(), recyclerView,
                    new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {

                            Chapter chapter = chapterList.get(position);
                            if (accountType == 0 && chapter.isLock()) {
                                openUpgradeDialog(1, chapter.getId(), chapter.getTitle(), "1 TL");
                            } else {
                                openSelectedItem(1, chapter.getId(), chapter.getImageCount(), chapter.getFileLength());
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

    private void loadTestsToRecycler() {
        loadTestList();
        recyclerView.setAdapter(testAdapter);
        testAdapter.notifyDataSetChanged();
        if (currentTouchListenerType == 2)
            return;

        if (currentTouchListenerType == 1)
            recyclerView.removeOnItemTouchListener(chapterTouchListener);
        else if (currentTouchListenerType == 3)
            recyclerView.removeOnItemTouchListener(specialsTouchListener);

        if (currentTouchListenerType != 2) {

            testTouchListener = new RecyclerTouchListener(getApplicationContext(), recyclerView,
                    new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {

                            Test test = testList.get(position);
                            if (accountType <= 1) {
                                openUpgradeDialog(2, test.getId(), test.getTitle(), "2 TL");
                            } else {
                                openSelectedItem(2, test.getId(), test.getImageCount(), test.getFileLength());
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

    private void loadSpecialsToRecycler() {
        loadSpecialList();
        recyclerView.setAdapter(specialsAdapter);
        specialsAdapter.notifyDataSetChanged();
        if (currentTouchListenerType == 3)
            return;

        if (currentTouchListenerType == 1)
            recyclerView.removeOnItemTouchListener(chapterTouchListener);
        else if (currentTouchListenerType == 2)
            recyclerView.removeOnItemTouchListener(testTouchListener);

        if (currentTouchListenerType != 3) {

            specialsTouchListener = new RecyclerTouchListener(getApplicationContext(), recyclerView,
                    new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {

                            Specials specials = specialsList.get(position);
                            if (accountType <= 2) {
                                openUpgradeDialog(3, specials.getId(), specials.getTitle(), "3 TL");
                            } else {
                                openSelectedItem(2, specials.getId(), specials.getImageCount(), specials.getFileLength());
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

    private void openSelectedItem(int type, int id, int imgCount, int fileLength) {
        Log.d("openSelectedItem", "type: " + type + ", id: " + id + ", imgCount: " + imgCount + ", fileLength: " + fileLength);
        Intent intent = new Intent(MainActivity.this, ShowImages.class);
        intent.putExtra(EXTRA_MESSAGE + "_type", String.valueOf(type));
        intent.putExtra(EXTRA_MESSAGE + "_id", String.valueOf(id));
        intent.putExtra(EXTRA_MESSAGE + "_imageCount", String.valueOf(imgCount));
        intent.putExtra(EXTRA_MESSAGE + "_fileLength", String.valueOf(fileLength));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        dialogFragment.onActivityResult(requestCode, resultCode, data);
        dialogFragment.onDestroyView();
    }

    @Override
    public void onFinishBuying(int _accountType, int _type, int _id) {
        accountType = _accountType;
        Log.d("onFinishBuying", "accountType: " + accountType);
        saveSettings();
        chapterAdapter.setAccountType(accountType);
        testAdapter.setAccountType(accountType);
        specialsAdapter.setAccountType(accountType);
        if(_type == 1)
            loadChaptersToRecycler();
        else if(_type == 2)
            loadTestsToRecycler();
        else if(_type == 3)
            loadSpecialsToRecycler();
    }


    /*************************************************************************************
     * Shared Preferences Functions
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
            case R.id.option_güncelle:
                if(isOnline()) {
                    reloadInception();
                } else{
                    String msg = "No connection!";
                    showSnackBarMessage(msg);
                }
            default:
                return super.onOptionsItemSelected(item);
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
            readInceptionJson();
            loadChaptersToRecycler();
        }
    };

    public void download_inception_json() {
        Log.d("Functions", "download_inception_json()..");
        String URL = "https://www.dropbox.com/s/u6q6uno2ro50mv4/inception.json?dl=1";
        DownloaderAsync downloaderAsync = new DownloaderAsync();
        downloaderAsync.setContext(MainActivity.this);
        downloaderAsync.setListener(onTaskCompleted);
        downloaderAsync.setProcessMessage("Downloading inception json..");
        downloaderAsync.setParentFolderName(null);
        downloaderAsync.setFileName("inception");
        downloaderAsync.setFileExtension(".json");
        downloaderAsync.setFileLength(2607);
        if (isOnline()) {
            Log.d("isOnline", "Your are online. Now can start download");
            downloaderAsync.execute(URL);
        } else{
            Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSnackBarMessage(String msg){
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
        snackbar.show();
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
