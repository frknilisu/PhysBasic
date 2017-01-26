package com.frkn.physbasic.helper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by frkn on 25.01.2017.
 */

public class Downloader {

    public DownloadManager downloadManager;
    public long downloadReference;

    Context context;
    String downloadLink;
    BroadcastReceiver broadcastReceiver;
    String path, filename;

    public Downloader() {

    }

    public void startDownload(){
        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(downloadLink);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        //Set the title of this download, to be displayed in notifications (if enabled).
        request.setTitle("Downloading..");
        //Set a description of this download, to be displayed in notifications (if enabled)
        request.setDescription("Chapters => 1/10");
        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalPublicDir(path, filename);

        //Enqueue a new download and same the referenceId
        downloadReference = downloadManager.enqueue(request);
    }

    private final static int BUFFER_SIZE = 2048;
    private final static String ZIP_EXTENSION = ".zip";

    public boolean unzip(String srcZipFileName, String destDirectoryName) {
        try {
            BufferedInputStream bufIS = null;
            // create the destination directory structure (if needed)
            File destDirectory = new File(destDirectoryName);
            destDirectory.mkdirs();

            // open archive for reading
            File file = new File(srcZipFileName);
            ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);

            //for every zip archive entry do
            Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                System.out.println("\tExtracting entry: " + entry);

                //create destination file
                File destFile = new File(destDirectory, entry.getName());

                //create parent directories if needed
                File parentDestFile = destFile.getParentFile();
                parentDestFile.mkdirs();

                if (!entry.isDirectory()) {
                    bufIS = new BufferedInputStream(
                            zipFile.getInputStream(entry));
                    int currentByte;

                    // buffer for writing file
                    byte data[] = new byte[BUFFER_SIZE];

                    // write the current file to disk
                    FileOutputStream fOS = new FileOutputStream(destFile);
                    BufferedOutputStream bufOS = new BufferedOutputStream(fOS, BUFFER_SIZE);

                    while ((currentByte = bufIS.read(data, 0, BUFFER_SIZE)) != -1) {
                        bufOS.write(data, 0, currentByte);
                    }

                    // close BufferedOutputStream
                    bufOS.flush();
                    bufOS.close();

                    // recursively unzip files
                    if (entry.getName().toLowerCase().endsWith(ZIP_EXTENSION)) {
                        String zipFilePath = destDirectory.getPath() + File.separatorChar + entry.getName();

                        unzip(zipFilePath, zipFilePath.substring(0, zipFilePath.length() - ZIP_EXTENSION.length()));
                    }
                }
            }
            bufIS.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public void setBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
        this.broadcastReceiver = broadcastReceiver;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
