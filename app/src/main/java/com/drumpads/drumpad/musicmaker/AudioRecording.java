package com.drumpads.drumpad.musicmaker;

import android.app.Activity;
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

    public void addSound (Integer id, Long time) {
        recordMap.add(new SoundInit(id, time));
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

    public void playSound (final Activity activity, final SoundPool soundPool, final int[] sounds) {
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
                            soundPool.play(sounds[sound.getId()], 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                    });
                }
            }).start();
        }
    }
}
