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
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isAdPresent;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isExitFromApp;

public class CustomConstructActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout redBtn, purpleBtn, blueBtn, orangeBtn, fileChooser;

    private int currentElementId, lastPickedElement;

    private static final int REQUEST_CODE_EDIT = 1;

    private String fileName, recordFileName;

    private boolean isRecordingStart, isRecordingWindowOpen;
    private LinearLayout record_btn;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;

    private int REQUEST_CODE_PERMISSION_ACCESS_AUDIO_RECORDING = 1001;

    private SoundPool soundPool;
    private int[] sounds = new int[24];
    private LinearLayout[] btns;
    private LinearLayout[] btnsStrokes;
    private LinearLayout currentStrokeBtn;

    private ImageView start_recording;

    private SharedPreferences sharedPreferences;

    private LinearLayout savePack;

    private String mode, lastPack;
    private int[]  lastSounds = new int[24];

    private boolean isExtended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_construct);

        sharedPreferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);

        getSupportActionBar().hide();

        mode = getIntent().getStringExtra(getString(R.string.construct_mode));

        soundPool = new SoundPool(25, AudioManager.STREAM_MUSIC, 0);

        record_btn = (LinearLayout) findViewById(R.id.record_music);

        savePack = (LinearLayout) findViewById(R.id.save);
        savePack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePack();
            }
        });

        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecordingWindowOpen) {
                    isRecordingWindowOpen = true;

                    ((LinearLayout) findViewById(R.id.construct_func)).setVisibility(View.GONE);
                    ((LinearLayout) findViewById(R.id.microphone_recording)).setVisibility(View.VISIBLE);
                } else {
                    isRecordingWindowOpen = false;

                    ((LinearLayout) findViewById(R.id.construct_func)).setVisibility(View.VISIBLE);
                    ((LinearLayout) findViewById(R.id.microphone_recording)).setVisibility(View.GONE);
                }
            }
        });

        start_recording = (ImageView) findViewById(R.id.start_recording);
        start_recording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecordingStart) {
                    isRecordingStart = true;

                    start_recording.setImageDrawable(getResources().getDrawable(R.drawable.pause));

                    if (currentElementId != 0)
                        recordFileName = String.valueOf(currentElementId);

                    recordStart();
                } else {
                    isRecordingStart = false;

                    start_recording.setImageDrawable(getResources().getDrawable(R.drawable.red_dot));

                    recordStop();
                }
            }
        });

        redBtn = (LinearLayout) findViewById(R.id.btn_red);
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

        redBtn.setOnClickListener(this);
        purpleBtn.setOnClickListener(this);
        blueBtn.setOnClickListener(this);
        orangeBtn.setOnClickListener(this);

        btns = new LinearLayout[24];
        btnsStrokes = new LinearLayout[24];

        for (int i = 0; i < 24; i++) {
            btns[i] = (LinearLayout) findViewById(getResId("btn_" + (i + 1), "id", this));
            btnsStrokes[i] = (LinearLayout) findViewById(getResId("btn_" + (i + 1) + "_stroke", "id", this));
            final int finalI = i;
            btns[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentStrokeBtn.setBackgroundResource(android.R.color.transparent);
                    currentStrokeBtn = btnsStrokes[finalI];
                    currentStrokeBtn.setBackground(getResources().getDrawable(R.drawable.white_stroke));

                    lastPickedElement = getResId("btn_" + (finalI + 1), "id", getApplicationContext());
                    currentElementId = finalI + 1;
                }
            });
        }

        if (mode.equals(getString(R.string.new_soundpack))) {
            isExtended = sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "NONE")
                    .equals(getResources().getString(R.string.default_music_mode));

            lastPack = sharedPreferences.getString(getString(R.string.default_music_mode), getString(R.string.default_music_mode));

            File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name) + "/soundpacks/customPack/");

            if (!outFileDirectory.exists())
                makeMainSoundSourceDir (getApplicationContext());

            if(isExtended)
                sounds = setAllSoundsUp("original");

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.default_music_mode), Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name) + "/soundpacks/customPack/");

            editor.apply();

            makeBtnsBackground();
        } else {
            makeBtnsBackgroundExists();

            sounds = setAllSoundsUp("original");
        }

        currentStrokeBtn = btnsStrokes[0];
        currentElementId = 1;

        btns[0].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(50);

                                    soundPool.stop(lastSounds[0]);
                                    lastSounds[0] = soundPool.play(sounds[0], 1.0f, 1.0f, 0, 0, 1.0f);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (Exception e) {

                    }
                }

                return false;
            }
        });

        btns[1].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[1]);
                                lastSounds[1] = soundPool.play(sounds[1], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[2].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[2]);
                                lastSounds[2] = soundPool.play(sounds[2], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[3].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[3]);
                                lastSounds[3] = soundPool.play(sounds[3], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[4].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[4]);
                                lastSounds[4] = soundPool.play(sounds[4], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[5].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[5]);
                                lastSounds[5] = soundPool.play(sounds[5], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[6].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[6]);
                                lastSounds[6] = soundPool.play(sounds[6], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[7].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[7]);
                                lastSounds[7] = soundPool.play(sounds[7], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[8].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[8]);
                                lastSounds[8] = soundPool.play(sounds[8], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[9].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[9]);
                                lastSounds[9] = soundPool.play(sounds[9], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[10].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[10]);
                                lastSounds[10] = soundPool.play(sounds[10], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[11].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[11]);
                                lastSounds[11] = soundPool.play(sounds[11], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[12].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[12]);
                                lastSounds[12] = soundPool.play(sounds[12], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[13].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[13]);
                                lastSounds[13] = soundPool.play(sounds[13], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[14].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[14]);
                                lastSounds[14] = soundPool.play(sounds[14], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[15].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[15]);
                                lastSounds[15] = soundPool.play(sounds[15], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[16].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[16]);
                                lastSounds[16] = soundPool.play(sounds[16], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[17].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[17]);
                                lastSounds[17] = soundPool.play(sounds[17], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[18].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[18]);
                                lastSounds[18] = soundPool.play(sounds[18], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[19].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[19]);
                                lastSounds[19] = soundPool.play(sounds[19], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[20].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[20]);
                                lastSounds[20] = soundPool.play(sounds[20], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[21].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[21]);
                                lastSounds[21] = soundPool.play(sounds[21], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[22].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[22]);
                                lastSounds[22] = soundPool.play(sounds[22], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        btns[23].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);

                                soundPool.stop(lastSounds[23]);
                                lastSounds[23] = soundPool.play(sounds[23], 1.0f, 1.0f, 0, 0, 1.0f);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                return false;
            }
        });

        lastPickedElement = R.id.btn_1;

        ((ImageView) findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isExitFromApp && !isAdPresent) {
            startActivity(new Intent(getApplicationContext(), SplashScreenActivity.class));
            finish();
        } else {
            isExitFromApp = false;
            isAdPresent = false;
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        if (isExitFromApp)
            isExitFromApp = false;
        else
            isExitFromApp = true;
    }

    public int getResId(String ResName, String className, Context ctx) {
        try {
            return ctx.getResources().getIdentifier(ResName, className, ctx.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void makeBtnsBackgroundExists () {
        File file = new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "NONE") + "colors.txt");

        if (file.exists()) {
            try {
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                } catch (IOException e) {
                    //You'll need to add proper error handling here
                }

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Colors colors = gson.fromJson(text.toString(), Colors.class);

                colors.makeBtnBackground(getApplicationContext(), btns);
            } catch (Exception j) {

            }
        }
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

            this.recordFileName = recordFileName;

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
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setOutputFile(recordFileName);
                mediaRecorder.setAudioEncodingBitRate(1024000);
                mediaRecorder.setAudioSamplingRate(176400);
                mediaRecorder.setAudioChannels(2);
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void makeBtnsBackground () {
        if (isExtended) {
            btns[0].setTag(new Integer(3));
            btns[1].setTag(new Integer(2));
            btns[2].setTag(new Integer(1));
            btns[3].setTag(new Integer(1));
            btns[4].setTag(new Integer(3));
            btns[5].setTag(new Integer(3));
            btns[6].setTag(new Integer(2));
            btns[7].setTag(new Integer(4));
            btns[8].setTag(new Integer(1));
            btns[9].setTag(new Integer(2));
            btns[10].setTag(new Integer(2));
            btns[11].setTag(new Integer(2));

            btns[12].setTag(new Integer(3));
            btns[13].setTag(new Integer(2));
            btns[14].setTag(new Integer(1));
            btns[15].setTag(new Integer(1));
            btns[16].setTag(new Integer(3));
            btns[17].setTag(new Integer(3));
            btns[18].setTag(new Integer(2));
            btns[19].setTag(new Integer(4));
            btns[20].setTag(new Integer(1));
            btns[21].setTag(new Integer(2));
            btns[22].setTag(new Integer(2));
            btns[23].setTag(new Integer(2));
        } else
            for (int i = 0; i < btns.length; i++)
                btns[i].setTag(new Integer(5));

        Colors colors = new Colors(getApplicationContext(), btns);

        File file = new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "original") + "colors.txt");

        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String output = gson.toJson(colors);

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

        colors.makeBtnBackground(getApplicationContext(), btns);
    }

    public void recordStop() {
        isAdPresent = true;

        try {
            if (mediaRecorder != null)
                mediaRecorder.stop();

            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(recordFileName));
            intent.putExtra("was_get_content_intent", false);
            intent.putExtra("btn_id", String.valueOf(currentElementId));
            intent.setClass(getApplicationContext(), RingdroidEditActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT);

//            SharedPreferences preferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE);
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString(String.valueOf(currentElementId) + "_sound", recordFileName);
//
//            editor.apply();
        } catch (Exception e) {

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
        String color = getString(R.string.red);

        switch (view.getId()) {
            case R.id.btn_red:
                color = getString(R.string.red);
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

        if (lastPickedElement != 0) {
            LinearLayout currentLinear = (LinearLayout) findViewById(lastPickedElement);

            int colorInt = 0;

            if (color.equals(getString(R.string.red))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.red_btn));
                currentLinear.setTag(new Integer(1));

                colorInt = R.drawable.red_btn;
            } else if (color.equals(getString(R.string.purple))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.pink_btn));
                currentLinear.setTag(new Integer(2));

                colorInt = R.drawable.pink_btn;
            } else if (color.equals(getString(R.string.blue))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.blue_btn));
                currentLinear.setTag(new Integer(3));

                colorInt = R.drawable.blue_btn;
            } else if (color.equals(getString(R.string.orange))) {
                currentLinear.setBackground(getResources().getDrawable(R.drawable.yellow_btn));
                currentLinear.setTag(new Integer(4));

                colorInt = R.drawable.yellow_btn;
            }

            Colors colors = new Colors(getApplicationContext(), btns);

            File file = new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "original") + "colors.txt");

            try {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                String output = gson.toJson(colors);

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
    }

    public int[] setAllSoundsUp (String folderName) {
        int[] sounds = new int[24];

        if (!sharedPreferences.contains(getString(R.string.default_music_mode)) || sharedPreferences.getString(getString(R.string.default_music_mode), "NULL").equals(getString(R.string.default_music_mode))) {
            for (int i = 1; i < 25; i++) {
                sounds[i - 1] = soundPool.load(Environment.getExternalStorageDirectory() + "/"
                        + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav", 1);
            }
        } else
            for (int i = 1; i < 25; i++) {
                if (new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                        "none") + (i) + ".m4a").exists()) {
                    sounds[i - 1] = soundPool.load(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                            "none") + (i) + ".m4a", 1);
                } else if (lastPack.equals(getString(R.string.default_music_mode))) {
                    sounds[i - 1] = soundPool.load(Environment.getExternalStorageDirectory() + "/"
                            + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav", 1);
                } else if (new File(lastPack + i + ".wav").exists()) {
                    sounds[i - 1] = soundPool.load(lastPack + i + ".wav", 1);
                }
            }

        return sounds;
    }

    private void processFile(){
        isAdPresent = true;

        FileChooser fileChooser = new FileChooser(CustomConstructActivity.this);

        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                fileName = file.getAbsolutePath();

                Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(fileName));
                intent.putExtra("was_get_content_intent", false);
                intent.putExtra("btn_id", String.valueOf(currentElementId));
                intent.setClass(getApplicationContext(), RingdroidEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_EDIT);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sounds = setAllSoundsUp("original");
                }
            }, 150);
    }

    private void makeMainSoundSourceDir (Context context) {
        File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
                + getResources().getString(R.string.app_name) + "/soundpacks/customPack/");

        if (!outFileDirectory.exists())
            outFileDirectory.mkdirs();
    }

    @Override
    public void onBackPressed() {
        if (isRecordingWindowOpen) {
            isRecordingWindowOpen = false;

            ((LinearLayout) findViewById(R.id.construct_func)).setVisibility(View.VISIBLE);
            ((LinearLayout) findViewById(R.id.microphone_recording)).setVisibility(View.GONE);
        } else
            savePack();
    }

    private void savePack () {
        isAdPresent = true;

        if (mode.equals(getString(R.string.new_soundpack))) {
            final AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(CustomConstructActivity.this);

            mDialogBuilder
                    .setTitle("Are you wanna save this soundpack?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View promptsView = li.inflate(R.layout.name_prompt, null);

                            final AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(CustomConstructActivity.this);
                            mDialogBuilder.setView(promptsView);

                            final EditText userInput = (EditText) promptsView.findViewById(R.id.sound_name_edit);

                            ImageView yes_btn = (ImageView) promptsView.findViewById(R.id.yes_btn);
                            ImageView cancel_btn = (ImageView) promptsView.findViewById(R.id.cancel_btn);

                            mDialogBuilder
                                    .setCancelable(false);

                            final AlertDialog alertDialog = mDialogBuilder.create();

                            yes_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    File pack = new File(Environment.getExternalStorageDirectory() + "/"
                                            + getResources().getString(R.string.app_name) + "/soundpacks/customPack/");

                                    if (userInput.getText().toString().length() < 1) {
                                        if (mode.equals(getString(R.string.new_soundpack))) {
                                            Toast.makeText(CustomConstructActivity.this, "The name is very short! Try again!", Toast.LENGTH_SHORT).show();

                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.remove(getString(R.string.default_music_mode));

                                            editor.apply();
                                        }
                                    } else {
                                        if (mode.equals(getString(R.string.new_soundpack))) {
                                            pack.renameTo(new File(Environment.getExternalStorageDirectory() + "/"
                                                    + getResources().getString(R.string.app_name) + "/soundpacks/" + userInput.getText().toString() + (isExtended ? "-extended/" : "/")));

                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(getString(R.string.default_music_mode), Environment.getExternalStorageDirectory() + "/"
                                                    + getResources().getString(R.string.app_name) + "/soundpacks/" + userInput.getText().toString() + (isExtended ? "-extended/" : "/"));

                                            editor.apply();
                                        }
                                    }

                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                            });

                            cancel_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (mode.equals(getString(R.string.new_soundpack))) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.remove(getString(R.string.default_music_mode));

                                        editor.apply();

                                        alertDialog.dismiss();
                                    }

                                    File file = new File(Environment.getExternalStorageDirectory() + "/"
                                            + getResources().getString(R.string.app_name) + "/soundpacks/customPack");
                                    deleteDirectory(file);
                                }
                            });

                            alertDialog.show();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mode.equals(getString(R.string.new_soundpack))) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(getString(R.string.default_music_mode), getString(R.string.default_music_mode));

                                editor.apply();
                            }

                            File file = new File(Environment.getExternalStorageDirectory()
                                    + "/" + getString(R.string.app_name) + "/soundpacks/customPack/");

                            deleteDirectory(file);

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    })
                    .setCancelable(false);

            final AlertDialog alertDialog = mDialogBuilder.create();

            alertDialog.show();
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    public static boolean deleteDirectory(File path) {
        isAdPresent = true;

        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
