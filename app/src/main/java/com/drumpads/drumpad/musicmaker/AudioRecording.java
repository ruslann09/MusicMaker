package com.drumpads.drumpad.musicmaker;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

public class AudioRecording implements Serializable {
    private ArrayList<SoundInit> recordMap;
    private String recordName = "";

    public AudioRecording () {
        recordMap = new ArrayList<>();
    }

    public void setRecordName (String recordName) {
        this.recordName = recordName;
    }

    public void addSound (String src, Long time) {
        recordMap.add(new SoundInit(src, time));
    }

    public void loadSoundFile (String output) {
        File file = new File(recordName);

        try {
            FileOutputStream f = new FileOutputStream(file);

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
    }

    public ArrayList<SoundInit> getRecordMap () {
        return recordMap;
    }

    public void playSound (final Activity activity) {
        for (final SoundInit sound : recordMap) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(sound.getTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MediaPlayer mediaPlayer = new MediaPlayer();
                            try {
                                mediaPlayer.setDataSource(sound.getSrc());
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        mediaPlayer.release();
                                        mediaPlayer = null;
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }
    }
}
