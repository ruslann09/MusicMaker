package com.drumpads.drumpad.musicmaker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class CustomConstructActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout greenBtn, purpleBtn, blueBtn, orangeBtn, fileChooser;

    private LinearLayout btn_1, btn_2, btn_3,
            btn_4, btn_5, btn_6,
            btn_7, btn_8, btn_9,
            btn_10, btn_11, btn_12;

    private int currentElementId;

    private String fileName, recordFileName;

    private boolean isRecordingStart;
    private ImageView record_btn;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;

    private int REQUEST_CODE_PERMISSION_ACCESS_AUDIO_RECORDING = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_construct);

        record_btn = (ImageView) findViewById(R.id.record_music);

        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecordingStart) {
                    isRecordingStart = true;

                    Toast.makeText(CustomConstructActivity.this, "Record started", Toast.LENGTH_SHORT).show();

                    if (currentElementId != 0)
                        recordFileName = String.valueOf(currentElementId);

                    recordStart();
                } else {
                    isRecordingStart = false;

                    Toast.makeText(CustomConstructActivity.this, "Record stopped", Toast.LENGTH_SHORT).show();

                    recordStop();
                }
            }
        });

        greenBtn = (LinearLayout) findViewById(R.id.btn_green);
        purpleBtn = (LinearLayout) findViewById(R.id.btn_purple);
        blueBtn = (LinearLayout) findViewById(R.id.btn_blue);
        orangeBtn = (LinearLayout) findViewById(R.id.btn_orange);
        fileChooser = (LinearLayout) findViewById(R.id.file_chooser);

        fileChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processFile();
            }
        });

        greenBtn.setOnClickListener(this);
        purpleBtn.setOnClickListener(this);
        blueBtn.setOnClickListener(this);
        orangeBtn.setOnClickListener(this);

        btn_1 = (LinearLayout) findViewById(R.id.btn_1);
        btn_2 = (LinearLayout) findViewById(R.id.btn_2);
        btn_3 = (LinearLayout) findViewById(R.id.btn_3);
        btn_4 = (LinearLayout) findViewById(R.id.btn_4);
        btn_5 = (LinearLayout) findViewById(R.id.btn_5);
        btn_6 = (LinearLayout) findViewById(R.id.btn_6);
        btn_7 = (LinearLayout) findViewById(R.id.btn_7);
        btn_8 = (LinearLayout) findViewById(R.id.btn_8);
        btn_9 = (LinearLayout) findViewById(R.id.btn_9);
        btn_10 = (LinearLayout) findViewById(R.id.btn_10);
        btn_11 = (LinearLayout) findViewById(R.id.btn_11);
        btn_12 = (LinearLayout) findViewById(R.id.btn_12);

        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_1;
            }
        });

        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_2;
            }
        });

        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_3;
            }
        });

        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_4;
            }
        });

        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_5;
            }
        });

        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_6;
            }
        });

        btn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_7;
            }
        });

        btn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_8;
            }
        });

        btn_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_9;
            }
        });

        btn_10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_10;
            }
        });

        btn_11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_11;
            }
        });

        btn_12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentElementId = R.id.btn_12;
            }
        });
    }

    public void recordStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_PERMISSION_ACCESS_AUDIO_RECORDING);
            return;
        }

        if (currentElementId != 0) {
            File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name));

            if (!outFileDirectory.exists())
                outFileDirectory.mkdirs();

            String recordFileName = (Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name) + "/"
                    + "records" + "/" + this.recordFileName + ".3gpp");

            File outFile = new File(recordFileName);

            if (outFile.exists()) {
                outFile.delete();
            }

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);

            try {
                releaseRecorder();

                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setOutputFile(recordFileName);
                mediaRecorder.setAudioEncodingBitRate(256000);
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setAudioChannels(1);
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            SharedPreferences preferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(String.valueOf(currentElementId) + "_sound", recordFileName);

            editor.apply();

            currentElementId = 0;
        }
    }

    public void recordStop() {
        try {
            if (mediaRecorder != null)
                mediaRecorder.stop();
        } catch (Exception e) {

        }
    }

    public void playStart(String fileName) {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onClick(View view) {
        String color = getString(R.string.green_color);

        switch (view.getId()) {
            case R.id.btn_green:
                color = getString(R.string.green_color);
                break;
            case R.id.btn_purple:
                color = getString(R.string.purple);
                break;
            case R.id.btn_blue:
                color = getString(R.string.blue);
                break;
            case R.id.btn_orange:
                color = getString(R.string.orange);
                break;
        }

        if (currentElementId != 0) {
            LinearLayout currentLinear = (LinearLayout) findViewById(currentElementId);

            int colorInt = 0;

            if (color.equals(getString(R.string.green_color))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.green_btn));

                colorInt = R.drawable.green_btn;
            } else if (color.equals(getString(R.string.purple))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.pink_btn));

                colorInt = R.drawable.pink_btn;
            } else if (color.equals(getString(R.string.blue))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.blue_btn));

                colorInt = R.drawable.blue_btn;
            } else if (color.equals(getString(R.string.orange))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.yellow_btn));

                colorInt = R.drawable.yellow_btn;
            }

            SharedPreferences preferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(String.valueOf(currentElementId) + "_color", colorInt);
            editor.apply();

            currentElementId = 0;
        }
    }

    private void processFile(){
        FileChooser fileChooser = new FileChooser(CustomConstructActivity.this);

        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                // ....do something with the file
                fileName = file.getAbsolutePath();

                if (!fileName.contains(Environment.getExternalStorageDirectory() + "/"
                        + getResources().getString(R.string.app_name) + "/original/"))
                    copyFileOrDirectory(fileName, Environment.getExternalStorageDirectory() + "/"
                            + getResources().getString(R.string.app_name) + "/original/");

                if (currentElementId != 0) {
                    SharedPreferences preferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(String.valueOf(currentElementId) + "_sound", Environment.getExternalStorageDirectory() + "/"
                            + getResources().getString(R.string.app_name) + "/original/" + fileName.substring(fileName.lastIndexOf("/")+1));

                    editor.apply();

                    currentElementId = 0;
                }
            }
        });

        fileChooser.showDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION_ACCESS_AUDIO_RECORDING)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                recordStart();
    }

    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
