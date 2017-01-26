package com.frkn.physbasic.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.frkn.physbasic.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ShowImages extends AppCompatActivity {


    public final static String EXTRA_MESSAGE = "com.frkn.physbasic.MESSAGE";
    ImageSwitcher imageSwitcher = null;
    Button prev, next;
    int currImage = 1;
    int animInDuration = 1000;
    int animOutDuration = 700;
    String alpha = "1.0";

    int type, id, imgCount;

    String imagesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);

        type = Integer.parseInt(getIntent().getExtras().getString(EXTRA_MESSAGE + "_type"));
        id = Integer.parseInt(getIntent().getExtras().getString(EXTRA_MESSAGE + "_id"));
        imgCount = Integer.parseInt(getIntent().getExtras().getString(EXTRA_MESSAGE + "_length"));

        switch (type){
            case 1:
                imagesPath += "/PhysBasic/chapters/";
                break;
            case 2:
                imagesPath += "/PhysBasic/tests/";
                break;
            case 3:
                imagesPath += "/PhysBasic/specials/";
                break;
            default:
                imagesPath += "/PhysBasic/chapters/";
                break;
        }
        imagesPath += id + "/";

        initializeImageSwitcher();
        Log.d("imgCount", String.valueOf(imgCount));
        setInitialImage();
        updateUi();
        setButtonsClick();
    }

    private void initializeImageSwitcher() {
        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        prev = (Button) findViewById(R.id.previousImage);
        next = (Button) findViewById(R.id.nextImage);
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new
                        ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                return imageView;
            }
        });

        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        out.setDuration(animOutDuration);
        in.setDuration(animInDuration);
        imageSwitcher.setInAnimation(out);
        imageSwitcher.setInAnimation(in);
        //imageSwitcher.setAlpha(Float.parseFloat(alpha));
    }

    private void setButtonsClick() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                currImage--;
                updateUi();
                setCurrentImage();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                currImage++;
                updateUi();
                setCurrentImage();
            }
        });
    }

    private void updateUi() {
        prev.setEnabled(currImage > 1);
        next.setEnabled(currImage < imgCount);
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

        if(scaledDensity<1){

            width = (int) (width *scaledDensity);
            height = (int) (height *scaledDensity);
        }else{
            width = (int) (width +width *(scaledDensity-1));
            height = (int) (height +height *(scaledDensity-1));
        }

        Log.d("Density", "width: " + width);
        Log.d("Density", "height: " + height);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, width, height, true);
        Drawable d = new BitmapDrawable(getResources(),thumbnail);

        return d;

    }

    private void setCurrentImage() {
        try {
            InputStream is = new FileInputStream(new File(imagesPath + "img" + currImage + ".jpg"));
            imageSwitcher.setImageDrawable(getDrawableForIns(is));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // set image to ImageView
    }
}
