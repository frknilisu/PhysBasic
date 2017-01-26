package com.frkn.physbasic.helper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.frkn.physbasic.activities.MainActivity;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static com.frkn.physbasic.R.id.imageView;

/**
 * Created by frkn on 26.01.2017.
 */

public class DownloaderAsync extends AsyncTask<String, String, String> {

    ProgressDialog mProgressDialog;
    Context context;

    OnTaskCompleted listener;

    public DownloaderAsync(Context _context, OnTaskCompleted _listener) {
        this.context = _context;
        this.listener = _listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Create a progressdialog
        mProgressDialog = new ProgressDialog(context);
        // Set progressdialog title
        mProgressDialog.setTitle("DownloaderAsync");
        // Set progressdialog message
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(0);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        // Show progressdialog
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(String... fileUrl) {
        int count;
        try {
            URL url = new URL(fileUrl[0]);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(100);
            urlConnection.connect();
            // show progress bar 0-100%
            int fileLength = urlConnection.getContentLength();
            Log.d("doInBack", "fileLength: " + fileLength);
            InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
            OutputStream outputStream = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/2.zip");

            byte data[] = new byte[1024];
            long total = 0;
            while ((count = inputStream.read(data)) != -1) {
                total += count;
                Log.d("Total", "tot: " + total);
                publishProgress("" + (int) ((total) / (fileLength*24)));
                outputStream.write(data, 0, count);
            }
            // flushing output
            outputStream.flush();
            // closing streams
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return null;
    }

    protected void onProgressUpdate(String... progress) {
        // progress percentage
        Log.d("onProgressUpdate", progress[0]);
        mProgressDialog.setProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mProgressDialog.dismiss();
        Toast.makeText(context, "Download completed.", Toast.LENGTH_SHORT).show();
        listener.onTaskCompleted("");
    }

    @Override
    protected void onCancelled(String result) {
        super.onCancelled(result);
        listener.onTaskCompleted("Error: Download Failed. Please retry!");
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String response);
    }
}
