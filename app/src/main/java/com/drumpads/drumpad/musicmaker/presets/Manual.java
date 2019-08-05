package com.drumpads.drumpad.musicmaker.presets;

import android.app.Activity;
import android.os.Environment;

import com.drumpads.drumpad.musicmaker.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

public class Manual implements Serializable {
    private ArrayList<ManualRecord> packs;
    private long date;
    private int count;
    public boolean isManualLoaded;

    public Manual() {
        packs = new ArrayList<>();
    }

    public void add (ManualRecord manualRecord) {
        packs.add (manualRecord);
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void createSoundPacks (final Activity activity) {
        try {
            for (ManualRecord manualRecord : packs) {
                File file = new File(Environment.getExternalStorageDirectory() + "/" + activity.getString(R.string.app_name) + "/soundpacks/" + manualRecord.getName());

                if (!file.exists())
                    file.mkdirs();

                File manualRecordFile = new File(Environment.getExternalStorageDirectory() + "/" + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + manualRecord.getName() + "/manualRecord.txt");

                manualRecord.setWorkingPath(Environment.getExternalStorageDirectory() + "/"
                        + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + manualRecord.getName() + "/");

                try {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    String output = gson.toJson(manualRecord);

                    FileOutputStream f = new FileOutputStream(manualRecordFile);

                    PrintWriter pw = new PrintWriter(f);

                    pw.println(output);

                    pw.flush();

                    pw.close();

                    f.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File manualRecordPremium = new File(Environment.getExternalStorageDirectory() + "/" + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + manualRecord.getName() + "/manualRecordPremium.txt");

                if (manualRecord.getPosition() == 1)
                    try {
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        String output = gson.toJson("");

                        FileOutputStream f = new FileOutputStream(manualRecordPremium);

                        PrintWriter pw = new PrintWriter(f);

                        pw.println(output);

                        pw.flush();

                        pw.close();

                        f.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                new LoadFile(count++, activity, manualRecord.getIconUrl(),
                        new File(Environment.getExternalStorageDirectory() + "/"
                                + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + manualRecord.getName() + "/icon.jpg")).start();
            }
        } catch (Exception e) {

        }
    }

    private void onDownloadComplete(int pos, final Activity activity, boolean success) {
        if (pos >= packs.size() - 1)
            isManualLoaded = true;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.recreate();
                        }
                    });
                } catch (Exception e) {

                }
            }
        });
    }

    private class LoadFile extends Thread {
        private final String src;
        private final File dest;
        private final Activity activity;
        private int count;

        LoadFile(int count, Activity activity, String src, File dest) {
            this.src = src;
            this.dest = dest;
            this.activity = activity;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                FileUtils.copyURLToFile(new URL(src), dest);
                onDownloadComplete(count, activity, true);
            } catch (IOException e) {
                e.printStackTrace();
                onDownloadComplete(count, activity, false);
            }
        }
    }
}
