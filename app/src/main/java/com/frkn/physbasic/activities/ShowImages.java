package com.frkn.physbasic.activities;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.frkn.physbasic.R;
import com.frkn.physbasic.functions.DownloaderAsync;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ShowImages extends AppCompatActivity {


    public final static String EXTRA_MESSAGE = "com.frkn.physbasic.MESSAGE";
    ImageSwitcher imageSwitcher = null;
    ImageButton prev, next;
    TextView txt;
    int currImage = 1;
    int animInDuration = 300;
    int animOutDuration = 100;
    String alpha = "1.0";

    int type, id, imageCount, fileLength;
    String typeAsString;
    String URL = null;


    private static final int SWIPE_MIN_DISTANCE = 120;
    //private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    final GestureDetector gdt = new GestureDetector(new GestureListener());

    Animation inRightToCenter;
    Animation outCenterToLeft;

    Animation inLeftToCenter;
    Animation outCenterToRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);

        type = Integer.parseInt(getIntent().getExtras().getString(EXTRA_MESSAGE + "_type"));
        id = Integer.parseInt(getIntent().getExtras().getString(EXTRA_MESSAGE + "_id"));
        imageCount = Integer.parseInt(getIntent().getExtras().getString(EXTRA_MESSAGE + "_imageCount"));
        fileLength = Integer.parseInt(getIntent().getExtras().getString(EXTRA_MESSAGE + "_fileLength"));

        Log.d("ShowImages", "type: " + type);
        Log.d("ShowImages", "id: " + id);
        Log.d("ShowImages", "imageCount: " + imageCount);
        Log.d("ShowImages", "fileLength: " + fileLength);

        switch (type) {
            case 1:
                typeAsString = "chapters";
                break;
            case 2:
                typeAsString = "tests";
                break;
            case 3:
                typeAsString = "specials";
                break;
            default:
                typeAsString = "chapters";
                break;
        }

        initializeImageSwitcher();
        if (openFolder()) {
            setInitialImage();
            updateUi();
            setButtonsClick();
        }
    }

    private void initializeImageSwitcher() {
        inRightToCenter = AnimationUtils.loadAnimation(this, R.anim.img_switch_in_rc);
        outCenterToLeft = AnimationUtils.loadAnimation(this, R.anim.img_switch_out_cl);

        inLeftToCenter = AnimationUtils.loadAnimation(this, R.anim.img_switch_in_lc);
        outCenterToRight = AnimationUtils.loadAnimation(this, R.anim.img_switch_out_cr);

        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        prev = (ImageButton) findViewById(R.id.previousImage);
        next = (ImageButton) findViewById(R.id.nextImage);
        txt = (TextView) findViewById(R.id.pageNumberText);
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new
                        ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                imageView.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(final View view, final MotionEvent event) {
                        // TODO Auto-generated method stub
                        gdt.onTouchEvent(event);
                        //Log.i("Hello my Log 1","How dfgfd are you");
                        return true;
                    }
                });

                return imageView;
            }
        });

        /*Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        out.setDuration(animOutDuration);
        in.setDuration(animInDuration);
        imageSwitcher.setInAnimation(out);
        imageSwitcher.setInAnimation(in);
        imageSwitcher.setAlpha(Float.parseFloat(alpha));*/


    }

    private void setButtonsClick() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                goPreviousImage();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                goNextImage();
            }
        });
    }

    private void goPreviousImage(){
        imageSwitcher.setInAnimation(inLeftToCenter);
        imageSwitcher.setOutAnimation(outCenterToRight);

        currImage--;
        updateUi();
        setCurrentImage();
    }

    private void goNextImage(){
        imageSwitcher.setInAnimation(inRightToCenter);
        imageSwitcher.setOutAnimation(outCenterToLeft);

        currImage++;
        updateUi();
        setCurrentImage();
    }

    private void updateUi() {
        prev.setEnabled(currImage > 1);
        next.setEnabled(currImage < imageCount);
        //this.setTitle(getString(R.string.app_name_with_index, currImage, pageCount));
    }

    private void setInitialImage() {
        setCurrentImage();
    }

    private Drawable getDrawableForIns(InputStream inputStream) {
        Bitmap thumbnail = null;
        try {
            thumbnail = BitmapFactory.decodeStream(inputStream);
        } catch (Exception ex) {
            Log.e("getThumbnail()", ex.getMessage());
            return null;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float scaledDensity = metrics.density;
        int width = thumbnail.getWidth();
        int height = thumbnail.getHeight();

        if (scaledDensity < 1) {

            width = (int) (width * scaledDensity);
            height = (int) (height * scaledDensity);
        } else {
            width = (int) (width + width * (scaledDensity - 1));
            height = (int) (height + height * (scaledDensity - 1));
        }

        Log.d("Density", "width: " + width);
        Log.d("Density", "height: " + height);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, width, height, true);
        Drawable d = new BitmapDrawable(getResources(), thumbnail);

        return d;

    }

    private void setCurrentImage() {
        try {
            //  /data/user/0/com.frkn.physbasic/files/chapter/1
            txt.setText(currImage + " / " + imageCount);
            InputStream is = new FileInputStream(new File(this.getFilesDir(), typeAsString + "/xx" + id + "/img" + currImage + ".jpg"));
            imageSwitcher.setImageDrawable(getDrawableForIns(is));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // set image to ImageView
    }

    private boolean openFolder() {
        Log.d("ShowImages", "openFolder()");
        File chaptersFolder = new File(this.getFilesDir(), typeAsString);
        File chapterIdFolder = new File(chaptersFolder, "xx" + id);
        if (!chapterIdFolder.exists()) {
            try {
                URL = MainActivity.inceptionJson.getJSONArray(typeAsString).getJSONObject(id - 1).getString("link");
                Log.d("ShowImages", "url: " + URL);
                download_one_chapter();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            Log.d("openFolder", "file already exist");
            return true;
        }
    }

    public void download_one_chapter() {
        Log.d("Functions", "download_one_chapter()..");
        DownloaderAsync downloaderAsync = new DownloaderAsync();
        downloaderAsync.setContext(ShowImages.this);
        downloaderAsync.setListener(onTaskCompleted);
        downloaderAsync.setProcessMessage("Downloading Chapter-" + id + "..");
        downloaderAsync.setParentFolderName(typeAsString);
        downloaderAsync.setFileName("xx" + id);
        downloaderAsync.setFileExtension(".zip");
        downloaderAsync.setFileLength(fileLength);
        downloaderAsync.execute(URL);
    }

    DownloaderAsync.DownloadListener onTaskCompleted = new DownloaderAsync.DownloadListener() {
        @Override
        public void onTaskCompleted(String response) {
            Log.d("ShowImages", "onTaskCompleted: " + response);
            setInitialImage();
            updateUi();
            setButtonsClick();
        }

        @Override
        public void onTaskFailed(String response) {
            Log.d("ShowImages", "onTaskFailed: " + response);
            finish();
        }
    };

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                if(currImage < imageCount) {
                    goNextImage();
                }
                return false; // Right to left
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                if(currImage > 1) {
                    goPreviousImage();
                }
                return false; // Left to right
            }

            /*if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                return false; // Bottom to top
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                return false; // Top to bottom
            }*/

            return false;
        }

    }
}
