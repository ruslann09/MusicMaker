package com.drumpads.drumpad.musicmaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecordsListView recordsListView;

    private int REQUEST_CODE_PERMISSION_ACCESS_AUDIO_RECORDING = 1001;
    private int REQUEST_CODE_PERMISSION_ACCESS_EXTERNAL_STORAGE = 1002;

    private LinearLayout[] btns;

    private LinearLayout adBlock, pressingPanel;
    private ListView recordsList;

    public static int PERIOD_TIME_BETWEEN_CYCLES = 500;
    private long lastTime;
    private int currentPosition = -1;

    private ImageView[] unpressedCycles;
    private Thread cyclingThread;

    private HashMap<Integer, Integer> cyclingSounds;

    private int[] sounds;

    private SoundPool soundPool;
    private ImageView record_btn, cycle_start_arrow, cycle_stop_arrow, newRecords, constructWindow;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private boolean isRecordingStart, isCyclingStarted, isCyclingPanelShowed;

    private AudioRecording audioRecording;
    private long CURRENT_RECORDING_TIMESTAMP;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        adBlock = (LinearLayout) findViewById(R.id.ad_block);
        pressingPanel = (LinearLayout) findViewById(R.id.pressing_panel);

        record_btn = (ImageView) findViewById(R.id.record_music);
        cycle_start_arrow = (ImageView) findViewById(R.id.cycle_start_arrow);
        cycle_stop_arrow = (ImageView) findViewById(R.id.cycle_stop_arrow);
        newRecords = (ImageView) findViewById(R.id.new_records_btn);
        constructWindow = (ImageView) findViewById(R.id.construct_window);

        constructWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (getApplicationContext(), CustomConstructActivity.class));
                finish();
            }
        });

        cyclingSounds = new HashMap<Integer, Integer>();

        cycle_start_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isCyclingPanelShowed) {
                    pressingPanel.setVisibility(View.VISIBLE);

                    isCyclingPanelShowed = true;
                } else {
                    pressingPanel.setVisibility(View.GONE);

                    isCyclingPanelShowed = false;
                }

                if (!isCyclingStarted) {
                    isCyclingStarted = true;

                    cyclingStart();
                }
            }
        });

        cycle_stop_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCyclingStarted) {
                    isCyclingStarted = false;

                    pressingPanel.setVisibility(View.GONE);

                    cyclingSounds.clear();

                    cyclingStop();

                    Toast.makeText(MainActivity.this, "Cycling mode disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecordingStart) {
                    isRecordingStart = true;

                    CURRENT_RECORDING_TIMESTAMP = System.currentTimeMillis();

                    audioRecording = new AudioRecording();

                    recordStart();
                } else {
                    isRecordingStart = false;

                    recordStop();
                }
            }
        });

        newRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SoundsLoaderPallete.class));
            }
        });

        btns = new LinearLayout[12];

        btns[0] = (LinearLayout) findViewById(R.id.btn_1);
        btns[1] = (LinearLayout) findViewById(R.id.btn_2);
        btns[2] = (LinearLayout) findViewById(R.id.btn_3);
        btns[3] = (LinearLayout) findViewById(R.id.btn_4);
        btns[4] = (LinearLayout) findViewById(R.id.btn_5);
        btns[5] = (LinearLayout) findViewById(R.id.btn_6);
        btns[6] = (LinearLayout) findViewById(R.id.btn_7);
        btns[7] = (LinearLayout) findViewById(R.id.btn_8);
        btns[8] = (LinearLayout) findViewById(R.id.btn_9);
        btns[9] = (LinearLayout) findViewById(R.id.btn_10);
        btns[10] = (LinearLayout) findViewById(R.id.btn_11);
        btns[11] = (LinearLayout) findViewById(R.id.btn_12);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_ACCESS_EXTERNAL_STORAGE);
        } else {
            createRecordsListView();
        }

        makeBtnsBackground();

        btns[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    soundPool.play(sounds[0], 1.0f, 1.0f, 0, 0, 1.0f);

                    if (isRecordingStart)
                        audioRecording.addSound(0, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                    if (isCyclingStarted && isCyclingPanelShowed) {
                        cyclingSounds.put(currentPosition, sounds[0]);
                        unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btns[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[1], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(1, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[1]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[2], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(2, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[2]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[3], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(3, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[3]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[4], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(4, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[4]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[5], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(5, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[5]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[6], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(6, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[6]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[7].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[7], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(7, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[7]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[8].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[8], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(8, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[8]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[9].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[9], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(9, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[9]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[10].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[10], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(10, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[10]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });

        btns[11].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(sounds[11], 1.0f, 1.0f, 0, 0, 1.0f);

                if (isRecordingStart)
                    audioRecording.addSound(11, System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                if (isCyclingStarted && isCyclingPanelShowed) {
                    cyclingSounds.put(currentPosition, sounds[11]);
                    unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                }
            }
        });
    }

    private void makeBtnsBackground () {
        for (int i = 0; i < btns.length; i++) {
            if (preferences.contains(String.valueOf(btns[i].getId()) + "_color"))
                btns[i].setBackground(getResources().getDrawable(preferences.getInt(String.valueOf(btns[i].getId()) + "_color", R.drawable.blue_btn)));
        }
    }

    public void recordStart() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAPTURE_AUDIO_OUTPUT) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
//                    REQUEST_CODE_PERMISSION_ACCESS_AUDIO_RECORDING);
//            return;
//        }
//
//        File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
//                + getResources().getString(R.string.app_name));
//
//        if (!outFileDirectory.exists())
//            outFileDirectory.mkdirs();
//
//        fileName = (Environment.getExternalStorageDirectory() + "/"
//                + getResources().getString(R.string.app_name) + "/"
//                + "testing" + ".3gpp");
//
//        File outFile = new File(fileName);
//
//        if (outFile.exists()) {
//            outFile.delete();
//        }
//
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMode(AudioManager.MODE_IN_CALL);
//        audioManager.setSpeakerphoneOn(true);
//
//        try {
//            releaseRecorder();
//
//            mediaRecorder = new MediaRecorder();
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//            mediaRecorder.setOutputFile(fileName);
//            mediaRecorder.setAudioEncodingBitRate(256000);
//            mediaRecorder.setAudioSamplingRate(44100);
//            mediaRecorder.setAudioChannels(1);
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void recordStop() {
//        try {
//            if (mediaRecorder != null)
//                mediaRecorder.stop();
//        } catch (Exception e) {
//
//        }
//
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = li.inflate(R.layout.name_prompt, null);

        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        mDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.sound_name_edit);

        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
//                                        + getResources().getString(R.string.app_name));
//
//                                if (!outFileDirectory.exists())
//                                    outFileDirectory.mkdirs();
//
//                                fileName = (Environment.getExternalStorageDirectory() + "/"
//                                        + getResources().getString(R.string.app_name) + "/"
//                                        + "testing" + ".3gpp");
//
//                                File outFile = new File(fileName);
//
//                                if (outFile.exists()) {
//                                    outFile.renameTo(new File(fileName = (Environment.getExternalStorageDirectory() + "/"
//                                            + getResources().getString(R.string.app_name) + "/"
//                                            + userInput.getText().toString() + ".3gpp")));
//                                }

                                audioRecording.setRecordName(Environment.getExternalStorageDirectory() + "/"
                                        + getResources().getString(R.string.app_name) + "/records/" + userInput.getText().toString() + ".txt");

//                                audioRecording.loadSoundFile(getApplicationContext());

                                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                                    createRecordsListView();

                                GsonBuilder builder = new GsonBuilder();
                                Gson gson = builder.create();
                                String soundFile = gson.toJson(audioRecording);

                                audioRecording.loadSoundFile(soundFile);

                                createRecordsListView();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = mDialogBuilder.create();

        alertDialog.show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            createRecordsListView();
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

    private void cyclingStart () {
        registerAllCycles();

        cyclingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isCyclingStarted) {
                    if (System.currentTimeMillis() - lastTime >= PERIOD_TIME_BETWEEN_CYCLES) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentPosition++;

                                if (currentPosition >= 32) {
                                    currentPosition = 0;

                                    if (cyclingSounds.containsKey(31))
                                        unpressedCycles[31].setImageResource(R.drawable.cycling_pressed_btn);
                                    else
                                        unpressedCycles[31].setImageResource(R.drawable.cycling_unpressed_btn);
                                }

                                unpressedCycles[currentPosition].setImageResource(R.drawable.control_main_cycle);

                                if (cyclingSounds.containsKey(currentPosition))
                                    soundPool.play(cyclingSounds.get(currentPosition), 1.0f, 1.0f, 0, 0, 1.0f);

                                if (currentPosition > 0 && cyclingSounds.containsKey(currentPosition - 1))
                                    unpressedCycles[currentPosition - 1].setImageResource(R.drawable.cycling_pressed_btn);
                                else if (currentPosition > 0)
                                    unpressedCycles[currentPosition - 1].setImageResource(R.drawable.cycling_unpressed_btn);
                            }
                        });

                        lastTime = System.currentTimeMillis();
                    }
                }
            }
        });
        cyclingThread.start();
    }

    private void cyclingStop () {
        unRegisterAllCycles();
        currentPosition = -1;
    }

    private void registerAllCycles () {
        unpressedCycles = new ImageView[32];

        unpressedCycles[0] = (ImageView) findViewById(R.id.unpressed_1);
        unpressedCycles[1] = (ImageView) findViewById(R.id.unpressed_2);
        unpressedCycles[2] = (ImageView) findViewById(R.id.unpressed_3);
        unpressedCycles[3] = (ImageView) findViewById(R.id.unpressed_4);
        unpressedCycles[4] = (ImageView) findViewById(R.id.unpressed_5);
        unpressedCycles[5] = (ImageView) findViewById(R.id.unpressed_6);
        unpressedCycles[6] = (ImageView) findViewById(R.id.unpressed_7);
        unpressedCycles[7] = (ImageView) findViewById(R.id.unpressed_8);
        unpressedCycles[8] = (ImageView) findViewById(R.id.unpressed_9);
        unpressedCycles[9] = (ImageView) findViewById(R.id.unpressed_10);
        unpressedCycles[10] = (ImageView) findViewById(R.id.unpressed_11);
        unpressedCycles[11] = (ImageView) findViewById(R.id.unpressed_12);
        unpressedCycles[12] = (ImageView) findViewById(R.id.unpressed_13);
        unpressedCycles[13] = (ImageView) findViewById(R.id.unpressed_14);
        unpressedCycles[14] = (ImageView) findViewById(R.id.unpressed_15);
        unpressedCycles[15] = (ImageView) findViewById(R.id.unpressed_16);
        unpressedCycles[16] = (ImageView) findViewById(R.id.unpressed_17);
        unpressedCycles[17] = (ImageView) findViewById(R.id.unpressed_18);
        unpressedCycles[18] = (ImageView) findViewById(R.id.unpressed_19);
        unpressedCycles[19] = (ImageView) findViewById(R.id.unpressed_20);
        unpressedCycles[20] = (ImageView) findViewById(R.id.unpressed_21);
        unpressedCycles[21] = (ImageView) findViewById(R.id.unpressed_22);
        unpressedCycles[22] = (ImageView) findViewById(R.id.unpressed_23);
        unpressedCycles[23] = (ImageView) findViewById(R.id.unpressed_24);
        unpressedCycles[24] = (ImageView) findViewById(R.id.unpressed_25);
        unpressedCycles[25] = (ImageView) findViewById(R.id.unpressed_26);
        unpressedCycles[26] = (ImageView) findViewById(R.id.unpressed_27);
        unpressedCycles[27] = (ImageView) findViewById(R.id.unpressed_28);
        unpressedCycles[28] = (ImageView) findViewById(R.id.unpressed_29);
        unpressedCycles[29] = (ImageView) findViewById(R.id.unpressed_30);
        unpressedCycles[30] = (ImageView) findViewById(R.id.unpressed_31);
        unpressedCycles[31] = (ImageView) findViewById(R.id.unpressed_32);

        for (int i = 0; i < unpressedCycles.length; i++) {
            final int finalI = i;
            unpressedCycles[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isCyclingStarted) {
                        unpressedCycles[finalI].setImageResource(R.drawable.cycling_unpressed_btn);

                        if (cyclingSounds.containsKey(finalI))
                            cyclingSounds.remove(finalI);
                    }
                }
            });
        }
    }

    private void unRegisterAllCycles () {
        for (int i = 0; i < unpressedCycles.length; i++)
            unpressedCycles[i].setImageResource(R.drawable.cycling_unpressed_btn);

        for (int i = 0; i < unpressedCycles.length; i++)
            unpressedCycles[i] = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        if (requestCode == REQUEST_CODE_PERMISSION_ACCESS_AUDIO_RECORDING)
////            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
////                    && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
////                recordStart();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            createRecordsListView();
    }

    private void createRecordsListView () {
        recordsList = (ListView) findViewById(R.id.records_list);

        recordsListView = new RecordsListView(getApplicationContext(), getRecords());
        recordsList.setAdapter(recordsListView);

        recordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                playStart(getRecords().get(i).getPath());

                File file = new File(getRecords().get(i).getPath());

                //Read text from file
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }


                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                audioRecording = gson.fromJson(text.toString(), AudioRecording.class);

                audioRecording.playSound(MainActivity.this, soundPool, sounds);
            }
        });

        registerForContextMenu(recordsList);

        File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
                + getResources().getString(R.string.app_name) + "/original/");

        if (!outFileDirectory.exists())
            makeMainSoundSourceDir (getApplicationContext());

        soundPool = new SoundPool(25, AudioManager.STREAM_MUSIC, 0);

        sounds = setAllSoundsUp("original");
    }

    public int[] setAllSoundsUp (String folderName) {
        int[] sounds = new int[12];

        for (int i = 0; i < 12; i++) {
            if (preferences.contains(String.valueOf(btns[i].getId()) + "_sound")) {
                sounds[i] = soundPool.load(preferences.getString(String.valueOf(btns[i].getId()) + "_sound", Environment.getExternalStorageDirectory() + "/"
                        + getResources().getString(R.string.app_name) + "/" + folderName + "/" + (i + 1) + ".wav"), 1);
            } else {
                sounds[i] = soundPool.load(Environment.getExternalStorageDirectory() + "/"
                        + getResources().getString(R.string.app_name) + "/" + folderName + "/" + (i + 1) + ".wav", 1);
            }
        }

        return sounds;
    }

    class RecordsListView extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private ArrayList<InitRecordItem> arrayMyMatches;

        public RecordsListView (Context ctx, ArrayList<InitRecordItem> arr) {
            mLayoutInflater = LayoutInflater.from(ctx);
            setArrayMyData(arr);
        }
        public ArrayList<InitRecordItem> getArrayMyData() {
            return arrayMyMatches;
        }

        public void setArrayMyData(ArrayList<InitRecordItem> arrayMyData) {
            this.arrayMyMatches = arrayMyData;
        }
        public int getCount () {
            return arrayMyMatches.size();
        }

        @Override
        public InitRecordItem getItem (int position) {
            InitRecordItem app = arrayMyMatches.get(position);

            return app;
        }

        public void remove(int position) {
            try {
                if (new File(getItem(position).getPath()).delete())
                    Toast.makeText(MainActivity.this, "Deleted successfuly", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
            }
        }

        public long getItemId (int position) {
            return position;
        }

        public void refuse (ArrayList<InitRecordItem> apps) {
            arrayMyMatches.clear ();
            arrayMyMatches.addAll (apps);
            notifyDataSetChanged();
        }

        //получение элемента ListView и его отправка в активность данных

        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.record_init_row, null, true);
                }

//                convertView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                    @Override
//                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//                        MenuInflater inflater = getMenuInflater();
//                        inflater.inflate(R.menu.delete_list_row, contextMenu);
//                    }
//                });

                TextView recordName = (TextView) convertView.findViewById(R.id.record_name);
                recordName.setText(arrayMyMatches.get(position).getName().substring(0, arrayMyMatches.get(position).getName().length() - 4));
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            } finally {
                return convertView;
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_list_row, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_item:
                try {
                    recordsListView.remove(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
                    createRecordsListView();
                } catch (Exception e) {
                    Toast.makeText(this, e.toString() + "", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onContextItemSelected(item);
    }

    public ArrayList<File> listFilesWithSubFolders(File dir) {
        ArrayList<File> files = new ArrayList<File>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                files.addAll(listFilesWithSubFolders(file));
            else
                files.add(file);
        }

        return files;
    }

    private ArrayList<InitRecordItem> getRecords () {
        ArrayList<InitRecordItem> records = new ArrayList<>();

        String directoryName = Environment.getExternalStorageDirectory() + "/"
                + getResources().getString(R.string.app_name) + "/records/";

        File outFileDirectory = new File(directoryName);

        if (!outFileDirectory.exists())
            outFileDirectory.mkdirs();

        for (File record: listFilesWithSubFolders(outFileDirectory)) {
            records.add(new InitRecordItem(record.getName()));
            records.get(records.size() - 1).setPath(record.getPath());
        }

        return records;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        releaseRecorder();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void makeMainSoundSourceDir (Context context) {
        File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
                + getResources().getString(R.string.app_name) + "/original/");

        if (!outFileDirectory.exists())
            outFileDirectory.mkdirs();

        AssetManager assetManager = context.getResources().getAssets();

        String[] files = null;

        try {
            files = assetManager.list(""); //ringtone is folder name
        } catch (Exception e) {
        }

        for (int i = 0; i < files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("" + files[i]);
                out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/"
                        + context.getResources().getString(R.string.app_name) + "/original/" + files[i]);

                byte[] buffer = new byte[65536 * 2];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
