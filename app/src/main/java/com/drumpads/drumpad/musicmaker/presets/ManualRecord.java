package com.drumpads.drumpad.musicmaker.presets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drumpads.drumpad.musicmaker.R;
import com.drumpads.drumpad.musicmaker.SplashScreenActivity;
import com.drumpads.drumpad.musicmaker.TutorialsListActivity;
import com.yandex.metrica.YandexMetrica;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.lastDownloadedPack;

public class ManualRecord implements Serializable {
    private String nameUrl, artistUrl;
    private int date, position;
    private String iconUrl, recordUrl, colorsUrl;
    private String mainSourceDir, workingPath;
    private int count = 1;
    private boolean premium;

    public ManualRecord() {
    }

    public ManualRecord(String name, String artist, int date, int position, String iconUrl,
                        String recordUrl, String colorsUrl, String mainSourceDir) {
        this.nameUrl = name;
        this.artistUrl = artist;
        this.date = date;
        this.position = position;
        this.iconUrl = iconUrl;
        this.recordUrl = recordUrl;
        this.colorsUrl = colorsUrl;
        this.mainSourceDir = mainSourceDir;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public String getName() {
        return nameUrl;
    }

    public String getArtist() {
        return artistUrl;
    }

    public int getDate() {
        return date;
    }

    public int getPosition() {
        return position;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public void setName(String name) {
        this.nameUrl = name;
    }

    public void setArtist(String artist) {
        this.artistUrl = artist;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
    }

    public String getColorsUrl() {
        return colorsUrl;
    }

    public void setColorsUrl(String colorsUrl) {
        this.colorsUrl = colorsUrl;
    }

    public String getMainSourceDir() {
        return mainSourceDir;
    }

    public void setMainSourceDir(String mainSourceDir) {
        this.mainSourceDir = mainSourceDir;
    }

    public void createSoundPack (Activity activity, ProgressBar progressBar, TextView textView) {
//        new LoadSoundPack(1, activity, getRecordUrl(),
//        new File(Environment.getExternalStorageDirectory() + "/"
//                + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + manualRecord.getName() + "/presound.wav")).start();

        new LoadSoundPack(0, activity, getMainSourceDir() + "/tutorial.txt",
                new File(Environment.getExternalStorageDirectory() + "/"
                        + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + getName() + "/tutorial.txt"), progressBar, textView).start();

        new LoadSoundPack(0, activity, getMainSourceDir() + "/tutorialInits.txt",
                new File(Environment.getExternalStorageDirectory() + "/"
                        + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + getName() + "/tutorialInits.txt"), progressBar, textView).start();

        for (int i = 1; i < 26; i++) {
            new LoadSoundPack(i + 1, activity, getMainSourceDir() + "/" + i + ".wav",
                    new File(Environment.getExternalStorageDirectory() + "/"
                            + activity.getResources().getString(R.string.app_name) + "/soundpacks/" + getName() + "/" + i + ".wav"), progressBar, textView).start();
        }

        YandexMetrica.reportEvent(getName() + "downloaded");
    }

    private void onCompleteDownloading(final int pos, final Activity activity, boolean success, final ProgressBar progressBar, final TextView textView) {
        if (success) {
            count++;

            final int progress = (int) (((count * 1.0f) / 26) * 100);

            if(progress == 103) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(100);
                        textView.setText(String.valueOf(100) + "%");
                    }
                });
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                        textView.setText(String.valueOf(progress) + "%");
                    }
                });
            }

            if (count >= 27) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SplashScreenActivity.isSoundPackLoaded = true;

                        if (SplashScreenActivity.isBannerWatched && SplashScreenActivity.isFailedToLoadRewarded) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.findViewById(R.id.soundpack_loader).setVisibility(View.GONE);
                                }
                            });

                            SharedPreferences.Editor editor = activity.getSharedPreferences(activity.getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
                            editor.putString(activity.getString(R.string.default_music_mode), lastDownloadedPack);

                            editor.apply();

                            activity.startActivity(new Intent(activity.getApplicationContext(), TutorialsListActivity.class));

                            if (!SplashScreenActivity.isFailedToLoadRewarded) {
                                SplashScreenActivity.isBannerWatched = false;
                                SplashScreenActivity.isFailedToLoadRewarded = false;
                            }
                        }
                    }
                });
            }
        }
    }

    private class LoadSoundPack extends Thread {
        private final String src;
        private final File dest;
        private final Activity activity;
        private int count;
        private ProgressBar progressBar;
        private TextView textView;

        LoadSoundPack(int count, Activity activity, String src, File dest, ProgressBar progressBar, TextView textView) {
            this.src = src;
            this.dest = dest;
            this.activity = activity;
            this.count = count;
            this.progressBar = progressBar;
            this.textView = textView;
        }

        @Override
        public void run() {
            try {
                FileUtils.copyURLToFile(new URL(src), dest);
                onCompleteDownloading(count, activity, true, progressBar, textView);
            } catch (IOException e) {
                e.printStackTrace();
                onCompleteDownloading(count, activity, false, progressBar, textView);
            }
        }
    }

    public String getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(String workingPath) {
        this.workingPath = workingPath;
    }
}
