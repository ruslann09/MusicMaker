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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drumpads.drumpad.musicmaker.presets.Tutorial;
import com.drumpads.drumpad.musicmaker.presets.TutorialInfo;
import com.drumpads.drumpad.musicmaker.presets.TutorialsInit;
import com.drumpads.drumpad.musicmaker.util.IabHelper;
import com.drumpads.drumpad.musicmaker.util.IabResult;
import com.drumpads.drumpad.musicmaker.util.Inventory;
import com.drumpads.drumpad.musicmaker.util.Purchase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.drumpads.drumpad.musicmaker.App.ITEM_SKU;
import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.checkPosition;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isAdPresent;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isExitFromApp;

//import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.check;


public class PlayGroundActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecordsListView recordsListView;

    private int REQUEST_CODE_PERMISSION_ACCESS_EXTERNAL_STORAGE = 1002;

    private LinearLayout[] btns;
    private ListView recordsList;

    private SoundPool soundPool;
    private ImageView play_btn, retry_btn, exit_btn;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private MediaPlayer[] mediaPlayers;

    private AudioRecording audioRecording;

    private LinearLayout recordsBtn, constructBtn, speakerBtn, premium, rateUs, privacy, share;
    private ImageView backBtn;

    private SharedPreferences sharedPreferences;

    private String[] soundsSrc;

    private ImageView[] blocks_x_1, blocks_x_2, blocks_x_3;

    private TextView playGroundMode;

    private LinearLayout a, b;

    public boolean isNeedRecording;

    public static InterstitialAd mInterstitialAd;

    public static boolean buttonBack = true;
    public static boolean interstitialFailedToLoad = false;
    public static boolean play = false;
    SharedPreferences sPref;

    String TAG = "SoundsLoaderPallete";

    IabHelper mHelper;
    String base64EncodedPublicKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_ground);

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

        AdLoader.Builder AdBuilder;
        AdLoader adLoader;

        AdBuilder = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110");

//        AdBuilder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
//            @Override
//            public void onContentAdLoaded(NativeContentAd ad) {
//                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.ad_content);
//                NativeContentAdView adView = (NativeContentAdView) getLayoutInflater()
//                        .inflate(R.layout.ad_content, null);
//                populateContentAdView(ad, adView);
//                frameLayout.removeAllViews();
//                frameLayout.addView(adView);
//            }
//        });

        adLoader = AdBuilder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());

        mInterstitialAd = new InterstitialAd(PlayGroundActivity.this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1333730078271260/4667290632");
        AdRequest adRequestInter = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequestInter);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);

                interstitialFailedToLoad = true;
                Toast.makeText(PlayGroundActivity.this, "Failed: " + i, Toast.LENGTH_SHORT).show();
            }
        });

        a = (LinearLayout) findViewById(R.id.first_playground);
        b = (LinearLayout) findViewById(R.id.second_playground);

        playGroundMode = (TextView) findViewById(R.id.playground_mode);
        playGroundMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playGroundMode.getText().toString().equals("A")) {
                    a.setVisibility(View.GONE);
                    b.setVisibility(View.VISIBLE);

                    playGroundMode.setText("B");
                } else {
                    a.setVisibility(View.VISIBLE);
                    b.setVisibility(View.GONE);

                    playGroundMode.setText("A");
                }
            }
        });

        File file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/tutorial.txt");

        Tutorial tutorial = new Tutorial();

//        ArrayList<TutorialRecord> tutorials = new ArrayList<>();
//
//        tutorials.add(new TutorialRecord(new int[]{22}, new int[]{19}, new int[]{13}, 930, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{19}, new int[]{13}, new int[]{16}, 1880, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{13}, new int[]{16}, new int[]{23}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{16}, new int[]{23}, new int[]{20}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{23}, new int[]{20}, new int[]{14}, 930, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{20}, new int[]{14}, new int[]{17}, 1880, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{14}, new int[]{17}, new int[]{15}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{17}, new int[]{15}, new int[]{18}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{15}, new int[]{18}, new int[]{21}, 1870, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{18}, new int[]{21}, new int[]{24}, 1880, "", ""));
//
//        tutorials.add(new TutorialRecord(new int[]{21}, new int[]{24}, new int[]{7}, 1870, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{24}, new int[]{7, 10}, new int[]{8}, 1880, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{7, 10}, new int[]{8}, new int[]{5, 10}, 350, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{5, 10}, new int[]{4}, 110, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{5, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{8}, new int[]{9, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{9, 10}, new int[]{4}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{9, 10}, new int[]{4}, new int[]{1, 10}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{3}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{3}, new int[]{6, 10}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{3}, new int[]{6, 10}, new int[]{10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{6, 10}, new int[]{10}, new int[]{7, 10}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{10}, new int[]{7, 10}, new int[]{8}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{7, 10}, new int[]{8}, new int[]{5, 10}, 350, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{5, 10}, new int[]{4}, 110, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{5, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{4}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{8}, new int[]{9, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{9, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{9, 10}, new int[]{8}, new int[]{10, 12}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{10, 12}, new int[]{11}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{10, 12}, new int[]{11}, new int[]{2, 10}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{11}, new int[]{2, 10}, new int[]{10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{2, 10}, new int[]{10}, new int[]{7, 10}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{10}, new int[]{7, 10}, new int[]{8}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{7, 10}, new int[]{8}, new int[]{5, 10}, 350, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{5, 10}, new int[]{4}, 110, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{5, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{8}, new int[]{9, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{9, 10}, new int[]{4}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{9, 10}, new int[]{4}, new int[]{1, 10}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{3}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{3}, new int[]{6, 10}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{3}, new int[]{6, 10}, new int[]{10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{6, 10}, new int[]{10}, new int[]{7, 10}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{10}, new int[]{7, 10}, new int[]{8}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{7, 10}, new int[]{8}, new int[]{5, 10}, 350, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{5, 10}, new int[]{4}, 110, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{5, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{4}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{4}, new int[]{1, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{4}, new int[]{1, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{1, 10}, new int[]{8}, new int[]{9, 10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{9, 10}, new int[]{8}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{9, 10}, new int[]{8}, new int[]{10, 12}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{8}, new int[]{10, 12}, new int[]{11}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{10, 12}, new int[]{11}, new int[]{2, 10}, 230, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{11}, new int[]{2, 10}, new int[]{10}, 240, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{2, 10}, new int[]{10}, new int[]{}, 470, "", ""));
//        tutorials.add(new TutorialRecord(new int[]{10}, new int[]{}, new int[]{}, 0, "", ""));
//
//        tutorial.setTutorials(tutorials);

        try {
            FileOutputStream f = new FileOutputStream(file);

            PrintWriter pw = new PrintWriter(f);

            pw.println("{\"currentItem\":0,\"isStarted\":false,\"lastPresseds\":[],\"startTime\":0,\"timeToPress\":0,\"tutorials\":[\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[23],\"prevNext\":[18],\"soundPath\":\"\",\"step\":[14],\"timeToPress\":4480},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[18],\"prevNext\":[17],\"soundPath\":\"\",\"step\":[23],\"timeToPress\":4490},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[17],\"prevNext\":[16],\"soundPath\":\"\",\"step\":[18],\"timeToPress\":1120},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[16],\"prevNext\":[19],\"soundPath\":\"\",\"step\":[17],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[19],\"prevNext\":[20],\"soundPath\":\"\",\"step\":[16],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[20],\"prevNext\":[21],\"soundPath\":\"\",\"step\":[19],\"timeToPress\":1120},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[21],\"prevNext\":[13],\"soundPath\":\"\",\"step\":[20],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[13],\"prevNext\":[15],\"soundPath\":\"\",\"step\":[21],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[15],\"prevNext\":[22],\"soundPath\":\"\",\"step\":[13],\"timeToPress\":1120},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[22],\"prevNext\":[24],\"soundPath\":\"\",\"step\":[15],\"timeToPress\":1130},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[24],\"prevNext\":[1,10],\"soundPath\":\"\",\"step\":[22],\"timeToPress\":1120},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[1,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[24],\"timeToPress\":1120},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[4,10],\"soundPath\":\"\",\"step\":[1,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[4,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[5,10],\"soundPath\":\"\",\"step\":[4,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[5,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[10],\"soundPath\":\"\",\"step\":[5,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[1,10],\"soundPath\":\"\",\"step\":[10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[1,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":570},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[4,10],\"soundPath\":\"\",\"step\":[1,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[4,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[8,10],\"soundPath\":\"\",\"step\":[4,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[8,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[10],\"soundPath\":\"\",\"step\":[8,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[1,10],\"soundPath\":\"\",\"step\":[10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[1,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[4,10],\"soundPath\":\"\",\"step\":[1,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[4,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[5,10],\"soundPath\":\"\",\"step\":[4,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[5,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[10],\"soundPath\":\"\",\"step\":[5,10],\"timeToPress\":570},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[1,10],\"soundPath\":\"\",\"step\":[10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[1,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[4,10],\"soundPath\":\"\",\"step\":[1,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[4,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[2,10],\"soundPath\":\"\",\"step\":[4,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[2,10],\"prevNext\":[3,10],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[3,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[2,10],\"timeToPress\":2240},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[6,10],\"soundPath\":\"\",\"step\":[3,10],\"timeToPress\":559},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[6,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[7,10],\"soundPath\":\"\",\"step\":[6,10],\"timeToPress\":570},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[7,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[10],\"soundPath\":\"\",\"step\":[7,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[3,10],\"soundPath\":\"\",\"step\":[10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[3,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[6,10],\"soundPath\":\"\",\"step\":[3,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[6,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[9,10],\"soundPath\":\"\",\"step\":[6,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[9,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[10],\"soundPath\":\"\",\"step\":[9,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[3,10],\"soundPath\":\"\",\"step\":[10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[3,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":570},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[6,10],\"soundPath\":\"\",\"step\":[3,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[6,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[7,10],\"soundPath\":\"\",\"step\":[6,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[7,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[10],\"soundPath\":\"\",\"step\":[7,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[3,10],\"soundPath\":\"\",\"step\":[10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[3,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[6,10],\"soundPath\":\"\",\"step\":[3,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[6,10],\"prevNext\":[12],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[12],\"prevNext\":[10,11],\"soundPath\":\"\",\"step\":[6,10],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[10,11],\"prevNext\":[],\"soundPath\":\"\",\"step\":[12],\"timeToPress\":560},\n" +
                    "{\"color\":\"\",\"factTime\":0,\"prev\":[],\"prevNext\":[],\"soundPath\":\"\",\"step\":[10,11],\"timeToPress\":560}]}");

            pw.flush();

            pw.close();

            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sharedPreferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);

        premium = (LinearLayout) findViewById(R.id.premium);

        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.GONE);

        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                mHelper.launchSubscriptionPurchaseFlow(PlayGroundActivity.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Red planet PACK");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        play_btn = (ImageView) findViewById(R.id.play_btn);
        retry_btn = (ImageView) findViewById(R.id.retry_btn);
        exit_btn = (ImageView) findViewById(R.id.exit_btn);

        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                recreate();
            }
        });

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                startActivity(new Intent(getApplicationContext(), TutorialsListActivity.class));

                finish();
            }
        });

        speakerBtn = (LinearLayout) findViewById(R.id.soundpacks_btn);
        constructBtn = (LinearLayout) findViewById(R.id.constructor_btn);
        recordsBtn = (LinearLayout) findViewById(R.id.records_btn);

        rateUs = (LinearLayout) findViewById(R.id.rate_us_btn);
        privacy = (LinearLayout) findViewById(R.id.privacy_btn);
        share = (LinearLayout) findViewById(R.id.share);

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

                Intent intent = new Intent (getApplicationContext(), CustomConstructActivity.class);
                intent.putExtra(getString(R.string.construct_mode), getString(R.string.new_soundpack));
                startActivity(intent);
                finish();
            }
        });

        recordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        File file_gson = new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "original") + "tutorial.txt");

        if (!sharedPreferences.contains(getResources().getString(R.string.default_music_mode)) || sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "NULL").equals(getResources().getString(R.string.default_music_mode)))
            file_gson = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/tutorial.txt");

        if (file_gson.exists()) {
            try {
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file_gson));
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
                tutorial = gson.fromJson(text.toString(), Tutorial.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TutorialsInit tutorialsInit = null;

        File file_gson_inits = new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode), Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/") + "tutorialInits.txt");

        if (file_gson_inits.exists() || sharedPreferences.getString(getResources().getString(R.string.default_music_mode), getResources().getString(R.string.default_music_mode)).equals(getResources().getString(R.string.default_music_mode))) {
            if (!file_gson_inits.exists() || sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "NONE").equals(getResources().getString(R.string.default_music_mode)))
                file_gson_inits = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/tutorialInits.txt");

            if (file_gson_inits.exists()) {
                try {
                    StringBuilder text = new StringBuilder();

                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file_gson_inits));
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
                    tutorialsInit = gson.fromJson(text.toString(), TutorialsInit.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            isNeedRecording = true;
        } else {
            ArrayList<TutorialInfo> tutorialInfos = new ArrayList<>();
            tutorialInfos.add(new TutorialInfo(0, 0, 0, tutorial.getTutorials().size() - 1));
            tutorialsInit = new TutorialsInit(tutorialInfos);

        }

        int pos = getIntent().getIntExtra("pos", 0);

        tutorial.setPosOfTutorialInfo(pos);
        tutorial.setTutorialInfo(tutorialsInit);

        makeBtnsBlocks();

        tutorial.getNextStep(-1, blocks_x_1, blocks_x_2, blocks_x_3, a, b, this, false);

        final Tutorial finalTutorial = tutorial;

        for (int i = 0; i < 24; i++) {
            btns[i] = (LinearLayout) findViewById(getResId("btn_" + (i + 1), "id", this));
            final int finalI = i;

            btns[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            final int finalI1 = i;
            btns[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (!finalTutorial.isPlaingBySelf)
                            finalTutorial.getNextStep(finalI1 + 1, blocks_x_1, blocks_x_2, blocks_x_3, a, b, PlayGroundActivity.this, false);

                        try {
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
                        } catch (Exception e) {
                            Toast.makeText(PlayGroundActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                        }
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        finalTutorial.lastPresseds.clear();

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
                isAdPresent = true;

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        setAllSoundsUp("original");

        final Tutorial finalTutorial1 = tutorial;
        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalTutorial1.playBySelf(PlayGroundActivity.this, blocks_x_1, blocks_x_2, blocks_x_3, a, b, soundsSrc);
            }
        });

        loadCheck();
        if(!play && checkPosition) onCreateDialog();
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
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getResources().getString(R.string.isBillingSuccess), true);
            editor.commit();

            if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
                premium.setVisibility(View.GONE);
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

    private void populateContentAdView(NativeContentAd nativeContentAd,
                                       NativeContentAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
        adView.setImageView(adView.findViewById(R.id.contentad_image));
        adView.setBodyView(adView.findViewById(R.id.contentad_body));
        adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
        adView.setLogoView(adView.findViewById(R.id.contentad_logo));
        adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));

        // Some assets are guaranteed to be in every NativeContentAd.
        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if (images.size() > 0) {
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }

        // Some aren't guaranteed, however, and should be checked.
        NativeAd.Image logoImage = nativeContentAd.getLogo();

        if (logoImage == null) {
            adView.getLogoView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
            adView.getLogoView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeContentAd);
    }

    public void saveCheck() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        //ed.putBoolean("check", check);
        ed.putBoolean("play", play);
        ed.apply();
    }

    public void loadCheck() {
        sPref = getPreferences(MODE_PRIVATE);
        //check = sPref.getBoolean("check", check);
        play = sPref.getBoolean("play", play);
    }

    @SuppressLint("ResourceAsColor")
    public void onCreateDialog() {
        checkPosition = false;
        play = true;
        final ImageView imageView28 = (ImageView)findViewById(R.id.imageView28);
        imageView28.setVisibility(View.VISIBLE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(PlayGroundActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = PlayGroundActivity.this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.help_play_dialog, null);

        layout.setMinimumHeight(2000);
        layout.setMinimumWidth(2500);
        builder.setView(layout);

        final AlertDialog a = builder.create();
        a.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        a.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                imageView28.setVisibility(View.GONE);
            }
        });

//        WindowManager.LayoutParams lp = a.getWindow().getAttributes();
//        lp.dimAmount = 1f;
//        a.getWindow().setAttributes(lp);
        //a.getWindow().setDimAmount(0.6f);
        a.show();

        RelativeLayout play_dialog = (RelativeLayout) layout.findViewById(R.id.play_dialog);
        play_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView28.setVisibility(View.GONE);
                a.cancel();
            }
        });
    }

    public static void openAppRating(Context context) {
        isAdPresent = true;

        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }
    }

    private void makeBtnsBlocks () {
        blocks_x_1 = new ImageView[24];
        blocks_x_2 = new ImageView[24];
        blocks_x_3 = new ImageView[24];

        for (int i = 0; i < 24; i++) {
            blocks_x_1[i] = (ImageView) findViewById(getResId("block_" + (i + 1) + "_1", "id", this));
        }

        for (int i = 0; i < 24; i++) {
            blocks_x_2[i] = (ImageView) findViewById(getResId("block_" + (i + 1) + "_2", "id", this));
        }

        for (int i = 0; i < 24; i++) {
            blocks_x_3[i] = (ImageView) findViewById(getResId("block_" + (i + 1) + "_3", "id", this));
        }
    }

    public int getResId(String ResName, String className, Context ctx) {
        try {
            return ctx.getResources().getIdentifier(ResName, className, ctx.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
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
    }

    public void setAllSoundsUp (String folderName) {
        mediaPlayers = new MediaPlayer[24];
        soundsSrc = new String[24];

        for (int i = 0; i < mediaPlayers.length; i++)
            mediaPlayers[i] = new MediaPlayer();

        try {
//
            if (!sharedPreferences.contains(getString(R.string.default_music_mode)) || sharedPreferences.getString(getString(R.string.default_music_mode), "NULL").equals(getString(R.string.default_music_mode))) {
                for (int i = 1; i < 25; i++) {
                    //                sounds[i] = soundPool.load(Environment.getExternalStorageDirectory() + "/"
                    //                        + getResources().getString(R.string.app_name) + "/" + folderName + "/" + (i + 1) + ".wav", 1);
                    mediaPlayers[i - 1].setDataSource(Environment.getExternalStorageDirectory() + "/"
                            + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav");
                    soundsSrc[i - 1] = Environment.getExternalStorageDirectory() + "/"
                            + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav";
                }
            } else
                for (int i = 1; i < 25; i++) {
                    if (new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                            "none") + (i) + ".m4a").exists()) {
//                        sounds[i] = soundPool.load(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
//                                "none") + (i + 1) + ".m4a", 1);
                        mediaPlayers[i - 1].setDataSource(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                                "none") + (i) + ".m4a");
                        soundsSrc[i - 1] = sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                                "none") + (i) + ".m4a";
                    } else if (sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                            "none").contains("-extended")) {
//                        sounds[i] = soundPool.load(Environment.getExternalStorageDirectory() + "/"
//                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + (i + 1) + ".wav", 1);
                        mediaPlayers[i - 1].setDataSource(Environment.getExternalStorageDirectory() + "/"
                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav");
                        soundsSrc[i - 1] = Environment.getExternalStorageDirectory() + "/"
                                + getResources().getString(R.string.app_name) + "/" + folderName + "/" + i + ".wav";
                    } else if (new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                            "none") + i + ".wav").exists()) {
//                        sounds[i] = soundPool.load(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
//                                "none") + (i + 1) + ".wav", 1);
                        mediaPlayers[i - 1].setDataSource(sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                                "none") + i + ".wav");
                        soundsSrc[i - 1] = sharedPreferences.getString(getResources().getString(R.string.default_music_mode),
                                "none") + i + ".wav";
                    }
                }

            for (int i = 0; i < mediaPlayers.length; i++)
                try {
                    mediaPlayers[i].prepare();
                } catch (Exception e) {}
        } catch (IOException e) {

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
                    Toast.makeText(PlayGroundActivity.this, "Deleted successfuly", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(PlayGroundActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
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
                    convertView.setOnClickListener(new View.OnClickListener() {
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

                                audioRecording.playSound(PlayGroundActivity.this);
                            } catch (Exception e) {

                            }
                        }
                    });
                }

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

        try {
            for (int i = 0; i < mediaPlayers.length; i++)
                mediaPlayers[i].release();
        } catch (Exception e) {}

        releasePlayer();
        releaseRecorder();
    }

    @Override
    public void onBackPressed() {
        if(buttonBack){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return;
            }

            soundPool.release();

            isAdPresent = true;
            startActivity(new Intent(getApplicationContext(), TutorialsListActivity.class));

            finish();
        } else buttonBack = false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
