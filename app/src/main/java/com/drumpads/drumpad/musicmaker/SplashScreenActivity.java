package com.drumpads.drumpad.musicmaker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.deleteDirectory;
import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.lastDownloadedPack;

public class SplashScreenActivity extends AppCompatActivity implements RewardedVideoAdListener {
    //    private InterstitialAd mInterstitialAd;
    public static String TAG = "Cash";

    private boolean adIsLoaded = false, startAdStarted = false;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    Timer interstitialAsLoad, rewardedVideoLoaderTimer;
    boolean isAdmob;

    public static InterstitialAd mInterstitialAd, splashInterstitial;
    public static RewardedVideoAd mRewardedVideoAd;

    public static int specInterval = 1000;

    public static boolean isSoundPackLoaded, isPackedOpenning, isTryOpenning,
            isManualLoaded, isBannerWatched, isFailedToLoadRewarded, justTakingRewardedWatch, isPurchaseRequired;

    public static boolean isExitFromApp, isAdPresent;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(getResources().getString(R.string.time_from_openning), System.currentTimeMillis());
        editor.apply();

        MobileAds.initialize(this, "ca-app-pub-1333730078271260~5165984082");

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        AdRequest adRequest = new AdRequest.Builder().build();

        mRewardedVideoAd.loadAd("ca-app-pub-1333730078271260/4045717375", adRequest);

        mInterstitialAd = new InterstitialAd(SplashScreenActivity.this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1333730078271260/5624169146");
        AdRequest adRequestInter = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequestInter);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int i) {
            }
        });

        rewardedVideoLoaderTimer = new Timer();

        rewardedVideoLoaderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (!mRewardedVideoAd.isLoaded()) {
                            specInterval = 4000;
                            AdRequest adRequest = new AdRequest.Builder().build();
                            mRewardedVideoAd.loadAd("ca-app-pub-1333730078271260/4045717375", adRequest);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    SplashScreenActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!mRewardedVideoAd.isLoaded())
                                                specInterval = 1500;
                                        }
                                    });
                                }
                            }, 4000);
                        }
                    }
                });
            }
        }, 0, specInterval);

        splashInterstitial = new InterstitialAd(SplashScreenActivity.this);
        splashInterstitial.setAdUnitId("ca-app-pub-1333730078271260/5665489603");
        AdRequest splashAdRequestInter = new AdRequest.Builder().build();

        splashInterstitial.loadAd(splashAdRequestInter);

        splashInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int i) {
            }
        });

        setContentView(R.layout.splash_screen);

        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            StartAnimations();

        } else {
            requestAndExternalPermission();
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {
        isAdPresent = true;
    }

    @Override
    public void onRewardedVideoAdClosed() {
        justTakingRewardedWatch = false;

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        AdRequest adRequest = new AdRequest.Builder().build();
        mRewardedVideoAd.loadAd("ca-app-pub-1333730078271260/4045717375",
                adRequest);

        if (isBannerWatched) {
            if (isSoundPackLoaded) {
                if (new File(lastDownloadedPack).exists()) {
                    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
                    editor.putString(getString(R.string.default_music_mode), lastDownloadedPack);
                    editor.apply();

                    startActivity(new Intent(getApplicationContext(), TutorialsListActivity.class));
                } else
                    try {
                        SoundsLoaderPallete.activity.recreate();
                    } catch (Exception e) {

                    }

                    isSoundPackLoaded = false;
            } else if (isPackedOpenning) {
                isPackedOpenning = false;

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else if (isTryOpenning) {
                isTryOpenning = false;

                startActivity(new Intent(getApplicationContext(), TutorialsListActivity.class));
            }

            isBannerWatched = false;
        } else if (isSoundPackLoaded) {
            isSoundPackLoaded = false;

            try {
                deleteDirectory(new File(lastDownloadedPack), true);
            } catch (Exception e) {}
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        isBannerWatched = true;
        isFailedToLoadRewarded = true;

        final AdRequest adRequest = new AdRequest.Builder().build();

        if (!mInterstitialAd.isLoaded())
            mInterstitialAd.loadAd(adRequest);

        if (justTakingRewardedWatch) {
            justTakingRewardedWatch = false;

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                    mInterstitialAd.show();

                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                        }
                    });
                    mInterstitialAd.loadAd(adRequest);
                }
            });
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        isBannerWatched = true;
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.splash);
        iv.clearAnimation();
        iv.startAnimation(anim);

        //wait load admob
        waitInterstitialAsLoad();
    }

    private void waitInterstitialAsLoad() {
        interstitialAsLoad = new Timer();
        final int[] timer = {0};

//        if (!startAdStarted) {
//            startAdStarted = true;
//            showStartAppStartingAds();
//        }

        interstitialAsLoad.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if ((splashInterstitial!=null && splashInterstitial.isLoaded())) {
                            if (!adIsLoaded) {
                                adIsLoaded = true;

                                startMenuActivity();

                                if (splashInterstitial != null && splashInterstitial.isLoaded() && !getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE).getBoolean(getString(R.string.isBillingSuccess), false)) {
                                    isAdPresent = true;

                                    splashInterstitial.show();
                                }

                                interstitialAsLoad.cancel();

//                                SplashScreenActivity.this.finish();
                            }
                        } else if (timer[0] >= 16) {
                            startMenuActivity();

//                            if (!startAdStarted) {
//                                startAdStarted = true;
//                                showStartAppStartingAds();
//                            }

                            if (!adIsLoaded && !startAdStarted) {
                                adIsLoaded = true;
                                startAdStarted = true;
                            }

                            interstitialAsLoad.cancel();

//                            SplashScreenActivity.this.finish();
                        }
                        timer[0]++;
                    }
                });
            }
        }, 0, 500);
    }

    public void startMenuActivity () {
        Intent intent = new Intent(SplashScreenActivity.this, SoundsLoaderPallete.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        Log.d(TAG, "run: isadmob: " + isAdmob);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            StartAnimations();

            return;
        }
    }

    private void requestAndExternalPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String [] permission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if(!hasPermissions(this, permission)){
            ActivityCompat.requestPermissions(this, permission, 101);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPermissions(thisActivity, permission)){
                    ActivityCompat.requestPermissions(thisActivity, permission, 101);
                }
            }
        };
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
