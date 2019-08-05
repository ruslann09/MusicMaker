package com.drumpads.drumpad.musicmaker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drumpads.drumpad.musicmaker.util.IabHelper;
import com.drumpads.drumpad.musicmaker.util.IabResult;
import com.drumpads.drumpad.musicmaker.util.Inventory;
import com.drumpads.drumpad.musicmaker.util.Purchase;
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
import java.util.HashMap;
import java.util.List;

import static com.drumpads.drumpad.musicmaker.App.ITEM_SKU;
import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.check;
import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.checkPosition;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isAdPresent;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isExitFromApp;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecordsListView recordsListView;

    public static boolean isLoaded;

    private int REQUEST_CODE_PERMISSION_ACCESS_EXTERNAL_STORAGE = 1002;

    private LinearLayout[] btns;

    private LinearLayout pressingPanel;
    private ListView recordsList;

    public static int PERIOD_TIME_BETWEEN_CYCLES = 500;
    private long lastTime;
    private int currentPosition = -1;

    private ImageView[] unpressedCycles;
    private Thread cyclingThread;

    private HashMap<Integer, String> cyclingSounds;

    private SoundPool soundPool;
    private ImageView record_btn, cycle_start_arrow, cycle_stop_arrow, construct_edit, firstPlayImage;
    private LinearLayout change_playground;
    private LinearLayout lessons;

    private boolean isSecondPlaygroundActive;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private MediaPlayer[] mediaPlayers;

    private boolean isRecordingStart, isCyclingStarted, isCyclingPanelShowed;

    private AudioRecording audioRecording;
    private long CURRENT_RECORDING_TIMESTAMP;

    private LinearLayout recordsBtn, constructBtn, speakerBtn, premium, rateUs, share, privacy;
    private ImageView backBtn;

    private SharedPreferences sharedPreferences;

    private String[] soundsSrc;

    private CustomDrawerLayout drawer;

    private int step = 0;
    private Preference test;
    SharedPreferences sPref;
    EditText etText;
    public static boolean play_check = false;
    public static boolean lessons_check = false;
    private int pos;

    String TAG = "SoundsLoaderPallete";

    IabHelper mHelper;
    String base64EncodedPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        base64EncodedPublicKey = getResources().getString(R.string.base64EncodedPublicKey);

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(
                new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result)
                    {
                        if (!result.isSuccess()) {
                            Log.d(TAG, "In-app Billing setup failed: " + result);
                        } else {
                            Log.d(TAG, "In-app Billing is set up OK");
                            mHelper.enableDebugLogging(true, TAG);
                        }
                    }
                });

//        File file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/manual.txt");
//
//        Manual manual = new Manual();
//        manual.add(new ManualRecord("Closer", "The Chainsmokers", 0, 0, "http://mmaker.club/catalog/pack1/icon.jpg",
//                "http://mmaker.club/catalog/pack1/presound.wav", "http://mmaker.club/catalog/pack1/colors.txt",
//                "http://mmaker.club/catalog/pack1/sounds"));
//        manual.add(new ManualRecord("Dum Dee Dum", "Keys N Krates", 0, 0, "http://mmaker.club/catalog/pack2/icon.jpg",
//                "http://mmaker.club/catalog/pack2/presound.wav", "http://mmaker.club/catalog/pack2/colors.txt",
//                "http://mmaker.club/catalog/pack2/sounds"));
//        manual.add(new ManualRecord("Undone", "Desmeon", 0, 0, "http://mmaker.club/catalog/pack3/icon.jpg",
//                "http://mmaker.club/catalog/pack3/presound.wav", "http://mmaker.club/catalog/pack3/colors.txt",
//                "http://mmaker.club/catalog/pack3/sounds"));
//        manual.add(new ManualRecord("State Of Mind", "Teminite", 0, 0, "http://mmaker.club/catalog/pack4/icon.jpg",
//                "http://mmaker.club/catalog/pack4/presound.wav", "http://mmaker.club/catalog/pack4/colors.txt",
//                "http://mmaker.club/catalog/pack4/sounds"));
//        manual.add(new ManualRecord("Freaks", "Timmy Trampet & Savage", 0, 1, "http://mmaker.club/catalog/pack5/icon.jpg",
//                "http://mmaker.club/catalog/pack5/presound.wav", "http://mmaker.club/catalog/pack5/colors.txt",
//                "http://mmaker.club/catalog/pack5/sounds"));
//        manual.add(new ManualRecord("Bonfire", "Knife Party", 0, 0, "http://mmaker.club/catalog/pack6/icon.jpg",
//                "http://mmaker.club/catalog/pack6/presound.wav", "http://mmaker.club/catalog/pack6/colors.txt",
//                "http://mmaker.club/catalog/pack6/sounds"));
//        manual.add(new ManualRecord("Energy Drink", "Virtual Riot", 0, 0, "http://mmaker.club/catalog/pack7/icon.jpg",
//                "http://mmaker.club/catalog/pack7/presound.wav", "http://mmaker.club/catalog/pack7/colors.txt",
//                "http://mmaker.club/catalog/pack7/sounds"));
//        manual.add(new ManualRecord("Cloud 9", "Tobu & Itro", 0, 0, "http://mmaker.club/catalog/pack8/icon.jpg",
//                "http://mmaker.club/catalog/pack8/presound.wav", "http://mmaker.club/catalog/pack8/colors.txt",
//                "http://mmaker.club/catalog/pack8/sounds"));
//        manual.add(new ManualRecord("Razor Sharp", "Pegboard Nerds & Tristam", 0, 0, "http://mmaker.club/catalog/pack9/icon.jpg",
//                "http://mmaker.club/catalog/pack9/presound.wav", "http://mmaker.club/catalog/pack9/colors.txt",
//                "http://mmaker.club/catalog/pack9/sounds"));
//        manual.add(new ManualRecord("Love Us", "DatPhoria & Spag Heddy", 0, 0, "http://mmaker.club/catalog/pack10/icon.jpg",
//                "http://mmaker.club/catalog/pack10/presound.wav", "http://mmaker.club/catalog/pack10/colors.txt",
//                "http://mmaker.club/catalog/pack10/sounds"));
//        manual.add(new ManualRecord("Like i do", "David Guetta X Martin Garris X Broox", 0, 1, "http://mmaker.club/catalog/pack11/icon.jpg",
//                "http://mmaker.club/catalog/pack11/presound.wav", "http://mmaker.club/catalog/pack11/colors.txt",
//                "http://mmaker.club/catalog/pack11/sounds"));
//        manual.add(new ManualRecord("In The Name Of Love", "Martin Garrix feat. Bebe Rexha", 0, 0, "http://mmaker.club/catalog/pack12/icon.jpg",
//                "http://mmaker.club/catalog/pack12/presound.wav", "http://mmaker.club/catalog/pack12/colors.txt",
//                "http://mmaker.club/catalog/pack12/sounds"));
//        manual.add(new ManualRecord("Stressed Out (Tomsize Remix)", "Twenty One Pilots", 0, 1, "http://mmaker.club/catalog/pack13/icon.jpg",
//                "http://mmaker.club/catalog/pack13/presound.wav", "http://mmaker.club/catalog/pack13/colors.txt",
//                "http://mmaker.club/catalog/pack13/sounds"));
//        manual.add(new ManualRecord("Hope", "Tobu", 0, 0, "http://mmaker.club/catalog/pack14/icon.jpg",
//                "http://mmaker.club/catalog/pack14/presound.wav", "http://mmaker.club/catalog/pack14/colors.txt",
//                "http://mmaker.club/catalog/pack14/sounds"));
//        manual.add(new ManualRecord("I Want You To Know", "Zedd", 0, 0, "http://mmaker.club/catalog/pack15/icon.jpg",
//                "http://mmaker.club/catalog/pack15/presound.wav", "http://mmaker.club/catalog/pack15/colors.txt",
//                "http://mmaker.club/catalog/pack15/sounds"));
//        manual.add(new ManualRecord("Faded", "Alan Walker", 0, 1, "http://mmaker.club/catalog/pack16/icon.jpg",
//                "http://mmaker.club/catalog/pack16/presound.wav", "http://mmaker.club/catalog/pack16/colors.txt",
//                "http://mmaker.club/catalog/pack16/sounds"));
//        manual.add(new ManualRecord("Poison", "Martin Garrix", 0, 0, "http://mmaker.club/catalog/pack17/icon.jpg",
//                "http://mmaker.club/catalog/pack17/presound.wav", "http://mmaker.club/catalog/pack17/colors.txt",
//                "http://mmaker.club/catalog/pack17/sounds"));
//        manual.add(new ManualRecord("Beautiful Now (KDrew Remix)", "Zedd", 0, 0, "http://mmaker.club/catalog/pack18/icon.jpg",
//                "http://mmaker.club/catalog/pack18/presound.wav", "http://mmaker.club/catalog/pack18/colors.txt",
//                "http://mmaker.club/catalog/pack18/sounds"));
//        manual.add(new ManualRecord("Stay", "Zedd", 0, 1, "http://mmaker.club/catalog/pack19/icon.jpg",
//                "http://mmaker.club/catalog/pack19/presound.wav", "http://mmaker.club/catalog/pack19/colors.txt",
//                "http://mmaker.club/catalog/pack19/sounds"));
//
//        try {
//            GsonBuilder builder = new GsonBuilder();
//            Gson gson = builder.create();
//            String output = gson.toJson(manual);
//
//            FileOutputStream f = new FileOutputStream(file);
//
//            PrintWriter pw = new PrintWriter(f);
//
//            pw.println(output);
//
//            pw.flush();
//
//            pw.close();
//
//            f.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        sharedPreferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);

        if (!sharedPreferences.contains(getResources().getString(R.string.default_music_mode))) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getResources().getString(R.string.default_music_mode), getResources().getString(R.string.default_music_mode));
            editor.apply();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Red planet PACK");
        setSupportActionBar(toolbar);

        drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.setActivity(MainActivity.this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        pressingPanel = (LinearLayout) findViewById(R.id.pressing_panel);

        firstPlayImage = (ImageView) findViewById(R.id.imageView14);
        record_btn = (ImageView) findViewById(R.id.record_music);
        cycle_start_arrow = (ImageView) findViewById(R.id.cycle_start_arrow);
        cycle_stop_arrow = (ImageView) findViewById(R.id.cycle_stop_arrow);
        construct_edit = (ImageView) findViewById(R.id.construct_window);
        change_playground = (LinearLayout) findViewById(R.id.change_playground);
        lessons = (LinearLayout) findViewById(R.id.lessons);
        speakerBtn = (LinearLayout) findViewById(R.id.soundpacks_btn);
        constructBtn = (LinearLayout) findViewById(R.id.constructor_btn);
        recordsBtn = (LinearLayout) findViewById(R.id.records_btn);

        rateUs = (LinearLayout) findViewById(R.id.rate_us_btn);
        share = (LinearLayout) findViewById(R.id.share);
        privacy = (LinearLayout) findViewById(R.id.privacy_btn);
        firstPlayImage.setVisibility(View.GONE);

        premium = (LinearLayout) findViewById(R.id.premium);

        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.GONE);

        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                mHelper.launchSubscriptionPurchaseFlow(MainActivity.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
            }
        });

//        loadCheck();
//        if(check){
//            firstPlayImage.setVisibility(View.VISIBLE);
//            firstPlayImage.setImageResource(R.drawable.help_step1);
//            firstPlayImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(step == 0){
//                        firstPlayImage.setImageResource(R.drawable.help_step2);
//                    } else if(step == 1){
//                        firstPlayImage.setImageResource(R.drawable.help_step3);
//                    }else firstPlayImage.setVisibility(View.GONE);
//                    step++;
//                }
//            });
//        }
//        check = false;
//        saveCheck();

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                String appId = getPackageName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id=" + appId);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Make awesome music with Music Maker app");
                startActivity(Intent.createChooser(shareIntent, "Select the transfer app"));
            }
        });

        lessons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                if(pos == 0) checkPosition = true; else checkPosition = false;
                //startActivity(new Intent(getApplicationContext(), PlayGroundActivity.class));
                startActivity(new Intent(getApplicationContext(), TutorialsListActivity.class));
                finish();
            }
        });

        rateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                openAppRating(getApplicationContext());
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                Uri policy = Uri.parse("https://docs.google.com/document/d/1zZxIAtxv0Nfq2qirgIOpI_kgO2x1WCXgbN-xysRgoxA/edit?usp=sharing");
                Intent policy_link = new Intent (Intent.ACTION_VIEW, policy);
                startActivity (policy_link);
            }
        });

        constructBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(getString(R.string.default_music_mode));

                editor.apply();

                Intent intent = new Intent (getApplicationContext(), CustomConstructActivity.class);
                intent.putExtra(getString(R.string.construct_mode), getString(R.string.new_soundpack));
                startActivity(intent);
                finish();
            }
        });

        recordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                ((LinearLayout) findViewById(R.id.menu)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.records)).setVisibility(View.VISIBLE);

                ((ImageView) findViewById(R.id.back_btn_records)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((LinearLayout) findViewById(R.id.menu)).setVisibility(View.VISIBLE);
                        ((LinearLayout) findViewById(R.id.records)).setVisibility(View.GONE);
                    }
                });
            }
        });

        cyclingSounds = new HashMap<Integer, String>();

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

        change_playground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSecondPlaygroundActive) {
                    isSecondPlaygroundActive = true;

                    ((LinearLayout) findViewById(R.id.first_playground)).setVisibility(View.GONE);
                    ((LinearLayout) findViewById(R.id.second_playground)).setVisibility(View.VISIBLE);

                    ((TextView) findViewById(R.id.playground_mode)).setText("B");
                } else {
                    isSecondPlaygroundActive = false;

                    ((LinearLayout) findViewById(R.id.first_playground)).setVisibility(View.VISIBLE);
                    ((LinearLayout) findViewById(R.id.second_playground)).setVisibility(View.GONE);

                    ((TextView) findViewById(R.id.playground_mode)).setText("A");
                }
            }
        });

        construct_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                Intent intent = new Intent (getApplicationContext(), CustomConstructActivity.class);
                intent.putExtra(getString(R.string.construct_mode), getString(R.string.new_soundpack));
                startActivity(intent);
                finish();
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

        speakerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                Intent intent = new Intent(getApplicationContext(), SoundsLoaderPallete.class);
                startActivity(intent);
                finish();
            }
        });

        btns = new LinearLayout[24];

        setAllSoundsUp("original");

        for (int i = 0; i < 24; i++) {
            btns[i] = (LinearLayout) findViewById(getResId("btn_" + (i + 1), "id", this));
            final int finalI = i;

            btns[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            btns[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN)) {
                        try {
//                            try {
//                                if (mediaPlayers[finalI] != null && mediaPlayers[finalI].isPlaying()) {
//                                    mediaPlayers[finalI].seekTo(0);
//                                }
//
//                                mediaPlayers[finalI].start();
//
//                                if (!mediaPlayers[finalI].isPlaying())
//                                    recreate();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }

                            if (mediaPlayers[finalI] != null) {
                                mediaPlayers[finalI].release();
                                mediaPlayers[finalI] = null;
                            }

                            mediaPlayers[finalI] = new MediaPlayer();
                            mediaPlayers[finalI].setDataSource(soundsSrc[finalI]);
                            mediaPlayers[finalI].prepare();
                            mediaPlayers[finalI].start();

                            mediaPlayers[finalI].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mediaPlayers[finalI].release();
                                    mediaPlayers[finalI] = null;
                                }
                            });

                            if (isRecordingStart)
                                audioRecording.addSound(soundsSrc[finalI], System.currentTimeMillis() - CURRENT_RECORDING_TIMESTAMP);

                            if (isCyclingStarted && isCyclingPanelShowed) {
                                cyclingSounds.put(currentPosition, soundsSrc[finalI]);
                                unpressedCycles[currentPosition].setImageResource(R.drawable.cycling_pressed_btn);
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                        }
                    }

                    return false;
                }
            });
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_ACCESS_EXTERNAL_STORAGE);
        } else {
            createRecordsListView();
        }

        makeBtnsBackground();

        backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        loadCheck();
        if(!play_check && !lessons_check) onCreateDialog();
        saveCheck();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode,resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener= new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            if (result.isFailure()) {
                if (result.getMessage().equals("IAB returned null purchaseData or dataSignature (response: -1008:Unknown error)") || result.getMessage().contains("purchaseData") || result.getMessage().contains("dataSignature"))
                    consumeItem();

                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }
        }
    };

    public void consumeItem() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.isBillingSuccess), true);
        editor.apply();

        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.GONE);

        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (result.isFailure()) {
                if (result.getMessage().equals("IAB returned null purchaseData or dataSignature (response: -1008:Unknown error)") || result.getMessage().contains("purchaseData") || result.getMessage().contains("dataSignature"))
                    consumeItem();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.isBillingSuccess), true);
                editor.apply();

                if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
                    premium.setVisibility(View.GONE);

                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {
                    if (result.isSuccess()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getResources().getString(R.string.isBillingSuccess), true);
                        editor.apply();

                        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
                            premium.setVisibility(View.GONE);
                    } else {
                        return;
                    }
                }
            };

    @SuppressLint("ResourceAsColor")
    public void onCreateDialog() {
        play_check = true;
        final ImageView imageView14 = (ImageView)findViewById(R.id.imageView14);
        imageView14.setVisibility(View.VISIBLE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.help_try_lessons_dialog, null);

        layout.setMinimumHeight(2000);
        layout.setMinimumWidth(2500);
        builder.setView(layout);

        final AlertDialog a = builder.create();
        a.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        a.show();

        a.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                imageView14.setVisibility(View.GONE);
            }
        });

        RelativeLayout imageView18 = (RelativeLayout)layout.findViewById(R.id.try_lesson);
        imageView18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView14.setVisibility(View.GONE);
                a.cancel();
            }
        });
    }

    public void saveCheck() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("check", check);
        ed.putBoolean("lessons_check", lessons_check);
        ed.putBoolean("play_check", play_check);
        ed.apply();
    }

    public void loadCheck() {
        sPref = getPreferences(MODE_PRIVATE);
        check = sPref.getBoolean("check", check);
        lessons_check = sPref.getBoolean("lessons_check", lessons_check);
        play_check = sPref.getBoolean("play_check", play_check);
    }

    public int getResId(String ResName, String className, Context ctx) {
        try {
            return ctx.getResources().getIdentifier(ResName, className, ctx.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void openAppRating(Context context) {
        isAdPresent = true;

        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));

        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );

                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                break;

            }
        }
    }

    private void makeBtnsBackground () {
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
        record_btn.setImageDrawable(getResources().getDrawable(R.drawable.pause));
    }

    public void recordStop() {
        record_btn.setImageDrawable(getResources().getDrawable(R.drawable.record_dot));

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = li.inflate(R.layout.name_prompt, null);

        final AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MainActivity.this);
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
                audioRecording.setRecordName(Environment.getExternalStorageDirectory() + "/"
                        + getResources().getString(R.string.app_name) + "/records/" + userInput.getText().toString() + ".txt");

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    createRecordsListView();

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                String soundFile = gson.toJson(audioRecording);

                audioRecording.loadSoundFile(soundFile);

                createRecordsListView();

                alertDialog.dismiss();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            createRecordsListView();
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

                                if (cyclingSounds.containsKey(currentPosition)) {
//                                    soundPool.play(cyclingSounds.get(currentPosition), 1.0f, 1.0f, 0, 0, 1.0f);

                                    MediaPlayer mediaPlayer = new MediaPlayer();
                                    try {
                                        mediaPlayer.setDataSource(cyclingSounds.get(currentPosition));
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            createRecordsListView();
    }

    private void createRecordsListView () {
        recordsList = (ListView) findViewById(R.id.records_list);

        recordsListView = new RecordsListView(getApplicationContext(), getRecords());
        recordsList.setAdapter(recordsListView);

        File outFileDirectory = new File(Environment.getExternalStorageDirectory() + "/"
                + getResources().getString(R.string.app_name) + "/original/");

        if (!outFileDirectory.exists())
            makeMainSoundSourceDir (getApplicationContext());

        soundPool = new SoundPool(15, AudioManager.STREAM_MUSIC, 1);

//        setAllSoundsUp("original");
    }

    public void setAllSoundsUp (String folderName) {
        mediaPlayers = new MediaPlayer[24];
        soundsSrc = new String[24];

//        for (int i = 0; i < mediaPlayers.length; i++)
//            mediaPlayers[i] = new MediaPlayer();

        try {
//
            if (!sharedPreferences.contains(getString(R.string.default_music_mode)) || sharedPreferences.getString(getString(R.string.default_music_mode), "NULL").equals(getString(R.string.default_music_mode))) {
                for (int i = 1; i < 25; i++) {
    //                sounds[i] = soundPool.load(Environment.getExternalStorageDirectory() + "/"
    //                        + getResources().getString(R.string.app_name) + "/" + folderName + "/" + (i + 1) + ".wav", 1);
//                        mediaPlayers[i - 1].setDataSource(Environment.getExternalStorageDirectory() + "/"
//                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav");
                        soundsSrc[i - 1] = Environment.getExternalStorageDirectory() + "/"
                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav";
                }
            } else
                for (int i = 1; i < 25; i++) {
                    if (new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                            "none") + (i) + ".m4a").exists()) {
//                        sounds[i] = soundPool.load(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
//                                "none") + (i + 1) + ".m4a", 1);
//                        mediaPlayers[i - 1].setDataSource(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
//                                "none") + (i) + ".m4a");
                        soundsSrc[i - 1] = sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                                "none") + (i) + ".m4a";
                    } else if (sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                            "none").contains("-extended")) {
//                        sounds[i] = soundPool.load(Environment.getExternalStorageDirectory() + "/"
//                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + (i + 1) + ".wav", 1);
//                        mediaPlayers[i - 1].setDataSource(Environment.getExternalStorageDirectory() + "/"
//                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav");
                        soundsSrc[i - 1] = Environment.getExternalStorageDirectory() + "/"
                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav";
                    } else if (new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                            "none") + i + ".wav").exists()) {
//                        sounds[i] = soundPool.load(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
//                                "none") + (i + 1) + ".wav", 1);
//                        mediaPlayers[i - 1].setDataSource(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
//                                "none") + i + ".wav");
                        soundsSrc[i - 1] = sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                                "none") + i + ".wav";
                    }
                }

            for (int i = 0; i < mediaPlayers.length; i++)
                try {
                    mediaPlayers[i].prepare();
                } catch (Exception e) {
                }
        } catch (Exception e) {

        }
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

        public View getView(final int position, View convertView, ViewGroup parent) {
            pos = position;
            try {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.record_init_row, null, true);
                }

                ImageView play = convertView.findViewById(R.id.play_music);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            File file = new File(getRecords().get(position).getPath());


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
                            audioRecording = gson.fromJson(text.toString(), AudioRecording.class);

                            audioRecording.playSound(MainActivity.this);
                        } catch (Exception e) {

                        }
                    }
                });

                ImageView share = convertView.findViewById(R.id.share_btn);
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isAdPresent = true;

                        Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);

                        intentShareFile.setType("text/*");
                        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + getRecords().get(position).getPath()));

                        intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                                "Sharing File...");
                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                        startActivity(Intent.createChooser(intentShareFile, "Select the transfer app"));
                    }
                });

                TextView recordName = (TextView) convertView.findViewById(R.id.record_name);
                recordName.setText(arrayMyMatches.get(position).getName().substring(0, arrayMyMatches.get(position).getName().length() - 4));

                ((ImageView) convertView.findViewById(R.id.delete_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recordsListView.remove(position);
                        createRecordsListView();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            } finally {
                return convertView;
            }
        }
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
            if (record.getPath().contains(".txt")) {
                records.add(new InitRecordItem(record.getName()));
                records.get(records.size() - 1).setPath(record.getPath());
            }
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
            return;
        } else {
            isAdPresent = false;

            super.onBackPressed();

            soundPool.release();

            startActivity(new Intent(getApplicationContext(), SoundsLoaderPallete.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
