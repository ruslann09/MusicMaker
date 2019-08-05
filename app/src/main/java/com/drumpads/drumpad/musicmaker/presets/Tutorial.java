package com.drumpads.drumpad.musicmaker.presets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drumpads.drumpad.musicmaker.PlayGroundActivity;
import com.drumpads.drumpad.musicmaker.R;
import com.drumpads.drumpad.musicmaker.SoundsLoaderPallete;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.metrica.YandexMetrica;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

import static com.drumpads.drumpad.musicmaker.PlayGroundActivity.buttonBack;
import static com.drumpads.drumpad.musicmaker.PlayGroundActivity.interstitialFailedToLoad;
import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.openAppRating;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isAdPresent;
import static com.facebook.FacebookSdk.getApplicationContext;

public class Tutorial implements Serializable {
    private ArrayList<TutorialRecord> tutorials;
    private int currentItem, firstItem,  lastItem;
    private long startTime, timeToPress;
    public boolean isStarted, isPlaingBySelf;
    public ArrayList<Integer> lastPresseds;
    private TutorialsInit tutorialsInit;
    private int posOfTutorialInfo;

    public ArrayList<TutorialRecord> getTutorials() {
        return tutorials;
    }

    public void setTutorials(ArrayList<TutorialRecord> tutorials) {
        this.tutorials = tutorials;
    }

    public int getPosOfTutorialInfo() {
        return posOfTutorialInfo;
    }

    public void setPosOfTutorialInfo(int posOfTutorialInfo) {
        this.posOfTutorialInfo = posOfTutorialInfo;
    }

    public TutorialsInit getTutorialInfo() {
        return tutorialsInit;
    }

    public void setTutorialInfo(TutorialsInit tutorialInfo) {
        this.tutorialsInit = tutorialInfo;

        currentItem = tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getStartPos();
        lastItem = tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getEndPos();
        firstItem = tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getStartPos();
    }

    public Tutorial () {
        tutorials = new ArrayList<>();
        lastPresseds = new ArrayList<>();
    }

    public void playBySelf (final Activity activity, final ImageView[] blocks_x_1, final ImageView[] blocks_x_2, final ImageView[] blocks_x_3, final LinearLayout a, final LinearLayout b, final String[] src) {
        isPlaingBySelf = true;

        long timeLast = 0, timeCurrent = 0;

        for (int i = firstItem; i <= lastItem; i++) {
            timeCurrent += tutorials.get(i).getTimeToPress() - 25;

            final long finalTime = timeLast;
            final int finalI = i;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (int step : tutorials.get(finalI).getStep()) {
                                lastPresseds.add(step);

                                try {
                                    getNextStep(step, blocks_x_1, blocks_x_2, blocks_x_3, a, b, activity, false);

                                    MediaPlayer mediaPlayer = new MediaPlayer();
                                    mediaPlayer.setDataSource(src[step - 1]);
                                    mediaPlayer.prepare();
                                    mediaPlayer.start();

                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                            mediaPlayer.release();
                                            mediaPlayer = null;
                                        }
                                    });
                                } catch (Exception e) {

                                }
                            }
                        }
                    }, finalTime);
                }
            });

            timeLast = timeCurrent;
            lastPresseds.clear();
        }
    }

    public void getNextStep (int id, final ImageView[] blocks_x_1, final ImageView[] blocks_x_2, final ImageView[] blocks_x_3, LinearLayout a, LinearLayout b, final Activity context, boolean isMainThread) {
        boolean isFalling = false, isNextRequire = false;
        lastPresseds.add(id);

        if (isStarted && currentItem > tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getStartPos() + 1 && currentItem <= tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getEndPos() + 1 && (System.currentTimeMillis() - startTime) >= tutorials.get(currentItem - 2).getTimeToPress() - 299)
            isNextRequire = true;
        else if (currentItem < tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getStartPos() + 2 && (System.currentTimeMillis() - startTime) >= 600)
            isNextRequire = true;
        else if (!isStarted)
            isNextRequire = true;

        if (isNextRequire) {
            isFalling = true;

            if (isStarted && currentItem <= tutorials.size() && currentItem > tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getStartPos())
                for (int num : tutorials.get(currentItem - 1).getStep()) {
                    if (!lastPresseds.contains(num))
                        isFalling = false;
                }
        }

        if (currentItem <= lastItem && isNextRequire) {
            if (currentItem > tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getStartPos() && isFalling) {

                try {
                    if (tutorials.get(currentItem).getStep()[0] > 12) {
                        a.setVisibility(View.GONE);
                        b.setVisibility(View.VISIBLE);
                    } else {
                        a.setVisibility(View.VISIBLE);
                        b.setVisibility(View.GONE);
                    }
                } catch (Exception e) {

                }

                tutorials.get(currentItem - 1).setFactTime(System.currentTimeMillis() - timeToPress + tutorials.get(currentItem - 1).getTimeToPress());

                for (int i = 0; i < blocks_x_1.length; i++) {
                    blocks_x_1[i].setVisibility(View.INVISIBLE);
                    blocks_x_2[i].setVisibility(View.INVISIBLE);
                    blocks_x_3[i].setVisibility(View.INVISIBLE);
                }

                if (tutorials.get(currentItem - 1).getStep().length > 0) {
                    for (final int step : tutorials.get(currentItem - 1).getStep())
                        blocks_x_1[step - 1].setVisibility(View.INVISIBLE);
                }

                if (tutorials.get(currentItem - 1).getPrev().length > 0) {
                    for (final int step : tutorials.get(currentItem - 1).getPrev()) {
                        blocks_x_2[step - 1].setVisibility(View.VISIBLE);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        blocks_x_2[step - 1].setVisibility(View.INVISIBLE);
                                    }
                                }, tutorials.get(currentItem - 1).getTimeToPress() - 300);
                            }
                        });
                    }
                }

                if (tutorials.get(currentItem - 1).getPrevNext().length > 0) {
                    for (final int step : tutorials.get(currentItem - 1).getPrevNext()) {
                        blocks_x_3[step - 1].setVisibility(View.VISIBLE);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        blocks_x_3[step - 1].setVisibility(View.INVISIBLE);
                                    }
                                }, tutorials.get(currentItem - 1).getTimeToPress() - 300);
                            }
                        });
                    }
                }

                if (tutorials.get(currentItem).getStep().length > 0) {
                    for (final int step : tutorials.get(currentItem).getStep()) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        blocks_x_1[step - 1].setVisibility(View.VISIBLE);

                                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.change_size_big);

                                        blocks_x_1[step - 1].startAnimation(animation);

                                        timeToPress = System.currentTimeMillis() + 300;
                                    }
                                }, tutorials.get(currentItem - 1).getTimeToPress() - 300);
                            }
                        });
                    }
                }

                if (tutorials.get(currentItem).getPrev().length > 0) {
                    if (tutorials.get(currentItem).getPrev().length > 0) {
                        for (final int step : tutorials.get(currentItem).getPrev()) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            blocks_x_2[step - 1].setVisibility(View.VISIBLE);

                                            Animation animation = AnimationUtils.loadAnimation(context, R.anim.change_size_middle);

                                            blocks_x_2[step - 1].startAnimation(animation);
                                        }
                                    }, tutorials.get(currentItem - 1).getTimeToPress() - 300);
                                }
                            });
                        }
                    }

                    if (tutorials.get(currentItem).getPrevNext().length > 0) {
                        for (final int step : tutorials.get(currentItem).getPrevNext()) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            blocks_x_3[step - 1].setVisibility(View.VISIBLE);

                                            Animation animation = AnimationUtils.loadAnimation(context, R.anim.change_size_low);

                                            blocks_x_3[step - 1].startAnimation(animation);
                                        }
                                    }, tutorials.get(currentItem - 1).getTimeToPress() - 300);
                                }
                            });
                        }
                    }
                }

                startTime = System.currentTimeMillis();
            } else if (!isStarted) {
                startTime = System.currentTimeMillis();

                try {
                    if (tutorials.get(currentItem + 1).getStep()[0] > 12) {
                        a.setVisibility(View.GONE);
                        b.setVisibility(View.VISIBLE);
                    } else {
                        a.setVisibility(View.VISIBLE);
                        b.setVisibility(View.GONE);
                    }
                } catch (Exception e) {

                }

                if (tutorials.get(currentItem).getStep().length > 0) {
                    for (int step : tutorials.get(currentItem).getStep()) {
                        blocks_x_1[step - 1].setVisibility(View.VISIBLE);

                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.change_size_big);

                        blocks_x_1[step - 1].startAnimation(animation);

                        timeToPress = System.currentTimeMillis();
                    }
                }

                if (tutorials.get(currentItem).getPrev().length > 0) {
                    for (int step : tutorials.get(currentItem).getPrev()) {
                        blocks_x_2[step - 1].setVisibility(View.VISIBLE);

                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.change_size_middle);

                        blocks_x_2[step - 1].startAnimation(animation);
                    }
                }

                if (tutorials.get(currentItem).getPrevNext().length > 0) {
                    for (int step : tutorials.get(currentItem).getPrevNext()) {
                        blocks_x_3[step - 1].setVisibility(View.VISIBLE);

                        Animation animation = AnimationUtils.loadAnimation(context, R.anim.change_size_low);

                        blocks_x_3[step - 1].startAnimation(animation);
                    }
                }

                isStarted = true;
                isFalling = true;
            }
        } else if (currentItem >= lastItem+1 && isFalling) {
            finishTutorial(context);
        }

        if (isFalling)
            currentItem++;
    }

    private void showRateUs(final Activity context){
        isAdPresent = true;

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.rate_us_dialog, null);
        ImageView rate_btn = (ImageView)layout.findViewById(R.id.yes_btn);
        ImageView not_now_btn = (ImageView)layout.findViewById(R.id.cancel_btn);

        layout.setMinimumHeight(1400);
        builder.setView(layout);

        final AlertDialog a = builder.create();
        a.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        a.show();

        rate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppRating(getApplicationContext());
                a.cancel();

                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
                editor.putBoolean("isRatedSuccessfuly", true);
                editor.apply();
            }
        });

        not_now_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a.cancel();
                context.startActivity(new Intent(context, SoundsLoaderPallete.class));

                context.finish();
            }
        });
    }

    private void finishTutorial (final Activity context){
        if (isPlaingBySelf)
            context.recreate();
        else {
            float result = 0;

            for (int i = firstItem; i <= lastItem; i++) {
                long wrongsTime = tutorials.get(i).getFactTime() - tutorials.get(i).getTimeToPress();

                if (wrongsTime <= 200)
                    result++;
            }

            context.findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            if (result >= 1)
                result = (result / (lastItem - firstItem)) * 100;
            else
                result = 0;

            if (isPlaingBySelf)
                result = 100;

            tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).setLast((int) Math.round(result));

            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);

            try {
                if (tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getBest() < result) {
                    tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).setBest((int) Math.round(result));

                    ((TextView) context.findViewById(R.id.high_score)).setText(context.getResources().getString(R.string.best_score) + ": " + (int) Math.round(result));
                } else
                    ((TextView) context.findViewById(R.id.high_score)).setText(context.getResources().getString(R.string.best_score) + ": "  + ((int) tutorialsInit.getTutorialsInfo().get(posOfTutorialInfo).getBest()));
            } catch (Exception e) {
                Toast.makeText(context, e.toString() + "", Toast.LENGTH_SHORT).show();
            }

            float starsCount = 0;

            if (result >= 85 && result <= 100) {
                starsCount = 3;
            } else if (result >= 80 && result <= 85) {
                ((ImageView) context.findViewById(R.id.star_3)).setImageDrawable(context.getResources().getDrawable(R.drawable.half_star));

                starsCount = 2.5f;
            } else if (result >= 60 && result < 80) {
                ((ImageView) context.findViewById(R.id.star_3)).setVisibility(View.INVISIBLE);

                starsCount = 2f;
            } else if (result >= 48 && result < 60) {
                ((ImageView) context.findViewById(R.id.star_3)).setVisibility(View.INVISIBLE);
                ((ImageView) context.findViewById(R.id.star_2)).setImageDrawable(context.getResources().getDrawable(R.drawable.half_star));

                starsCount = 1.5f;
            } else if (result >= 30 && result < 48) {
                ((ImageView) context.findViewById(R.id.star_3)).setVisibility(View.INVISIBLE);
                ((ImageView) context.findViewById(R.id.star_2)).setVisibility(View.INVISIBLE);

                starsCount = 1f;
            } else if (result >= 10 && result < 30) {
                ((ImageView) context.findViewById(R.id.star_3)).setVisibility(View.INVISIBLE);
                ((ImageView) context.findViewById(R.id.star_2)).setVisibility(View.INVISIBLE);
                ((ImageView) context.findViewById(R.id.star_1)).setImageDrawable(context.getResources().getDrawable(R.drawable.half_star));

                starsCount = 0.5f;
            }

            ((ImageView) context.findViewById(R.id.star_3)).setVisibility(View.INVISIBLE);
            ((ImageView) context.findViewById(R.id.star_2)).setVisibility(View.INVISIBLE);
            ((ImageView) context.findViewById(R.id.star_1)).setVisibility(View.INVISIBLE);

            if (starsCount >= 3) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_1)).setVisibility(View.VISIBLE);
                    }
                }, 500);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_2)).setVisibility(View.VISIBLE);
                    }
                }, 1000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_3)).setVisibility(View.VISIBLE);
                    }
                }, 1500);
            } else if (starsCount >= 2.5) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_1)).setVisibility(View.VISIBLE);
                    }
                }, 500);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_2)).setVisibility(View.VISIBLE);
                    }
                }, 1000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_3)).setVisibility(View.VISIBLE);
                    }
                }, 1500);
            } else if (starsCount >= 2) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_1)).setVisibility(View.VISIBLE);
                    }
                }, 500);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_2)).setVisibility(View.VISIBLE);
                    }
                }, 1000);
            } else if (starsCount >= 1.5) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_1)).setVisibility(View.VISIBLE);
                    }
                }, 500);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_2)).setVisibility(View.VISIBLE);
                    }
                }, 1000);
            } else if (starsCount >= 1) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_1)).setVisibility(View.VISIBLE);
                    }
                }, 500);
            } else if (starsCount >= 0.5) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) context.findViewById(R.id.star_1)).setVisibility(View.VISIBLE);
                    }
                }, 500);
            }

            final int[] currentRes = {0};
            final long timeToWait = 1000 / (int) Math.round(result);

            final float finalResult = result;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (currentRes[0] < finalResult) {
                        try {
                            Thread.sleep(timeToWait);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) context.findViewById(R.id.result)).setText(context.getResources().getString(R.string.score) + ": " +  (int) Math.round(currentRes[0]));
                            }
                        });

                        currentRes[0]++;
                    }
                }
            }).start();

            context.findViewById(R.id.next_level_layout).setVisibility(View.GONE);

            buttonBack = false;

            isAdPresent = true;

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(((PlayGroundActivity) context).mInterstitialAd.isLoaded()){
                                if (!context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE).getBoolean(context.getString(R.string.isBillingSuccess), false)) {
                                    isAdPresent = true;

                                    ((PlayGroundActivity) context).mInterstitialAd.show();
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        context.findViewById(R.id.next_level_layout).setVisibility(View.VISIBLE);
                                        ((LinearLayout) context.findViewById(R.id.get_more)).setVisibility(View.VISIBLE);

                                        if (finalResult >= 80) {
                                            ((LinearLayout) context.findViewById(R.id.get_more)).setVisibility(View.GONE);
                                            ImageView nextLevel = (ImageView) context.findViewById(R.id.next_level);
                                            ((LinearLayout) context.findViewById(R.id.next_level_layout_level)).setVisibility(View.VISIBLE);

                                            nextLevel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if (posOfTutorialInfo < tutorialsInit.getTutorialsInfo().size() - 1) {
                                                        isAdPresent = true;

                                                        Intent intent = new Intent(context, PlayGroundActivity.class);
                                                        intent.putExtra("pos", posOfTutorialInfo + 1);
                                                        context.startActivity(intent);

                                                        context.finish();
                                                    } else {
                                                        try {
                                                            if (!getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.app_preferences), Context.MODE_PRIVATE).getBoolean("isRatedSuccessfuly", false)) {
                                                                showRateUs(context);
                                                            } else {
                                                                isAdPresent = true;

                                                                context.startActivity(new Intent(context, SoundsLoaderPallete.class));

                                                                context.finish();
                                                            }
                                                        } catch (Exception e) {

                                                        }
                                                    }

                                                    YandexMetrica.reportEvent("next lesson");
                                                }
                                            });
                                        }
                                        buttonBack = true;
                                    }
                                }, 500);
                            }
                            else if(((PlayGroundActivity) context).mInterstitialAd.isLoading() || interstitialFailedToLoad){
                                isAdPresent = true;

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE).getBoolean(context.getString(R.string.isBillingSuccess), false)) {
                                            isAdPresent = true;

                                            ((PlayGroundActivity) context).mInterstitialAd.show();
                                        }

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                context.findViewById(R.id.next_level_layout).setVisibility(View.VISIBLE);
                                                ((LinearLayout) context.findViewById(R.id.get_more)).setVisibility(View.VISIBLE);

                                                if (finalResult >= 80) {
                                                    ((LinearLayout) context.findViewById(R.id.get_more)).setVisibility(View.GONE);
                                                    ImageView nextLevel = (ImageView) context.findViewById(R.id.next_level);
                                                    ((LinearLayout) context.findViewById(R.id.next_level_layout_level)).setVisibility(View.VISIBLE);

                                                    nextLevel.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            if (posOfTutorialInfo < tutorialsInit.getTutorialsInfo().size() - 1) {
                                                                isAdPresent = true;

                                                                Intent intent = new Intent(context, PlayGroundActivity.class);
                                                                intent.putExtra("pos", posOfTutorialInfo + 1);
                                                                context.startActivity(intent);

                                                                context.finish();
                                                            } else {
                                                                try {
                                                                    if (!getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.app_preferences), Context.MODE_PRIVATE).getBoolean("isRatedSuccessfuly", false)) {
                                                                        showRateUs(context);
                                                                    } else {
                                                                        isAdPresent = true;

                                                                        context.startActivity(new Intent(context, SoundsLoaderPallete.class));

                                                                        context.finish();
                                                                    }
                                                                } catch (Exception e) {

                                                                }
                                                            }

                                                            YandexMetrica.reportEvent("next lesson");
                                                        }
                                                    });
                                                }
                                                buttonBack = true;
                                            }
                                        }, 500);
                                    }
                                }, 5000);
                            }
                        }
                    }, 4000);
                }
            });

            context.findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    YandexMetrica.reportEvent("play again");

                    context.recreate();
                }
            });

            context.findViewById(R.id.finish).setVisibility(View.VISIBLE);

            try {
                if (!isPlaingBySelf && ((PlayGroundActivity) context).isNeedRecording) {
                    File manualRecordFile = new File(sharedPreferences.getString(context.getResources().getString(R.string.default_music_mode), Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.app_name) + "/") + "tutorialInits.txt");

                    if (!manualRecordFile.exists() || sharedPreferences.getString(context.getResources().getString(R.string.default_music_mode), "NONE").equals(context.getResources().getString(R.string.default_music_mode)))
                        manualRecordFile = new File(Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.app_name) + "/tutorialInits.txt");

                    try {
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        String output = gson.toJson(tutorialsInit);

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
                }
            } catch (Exception e) {

            }
        }
    }
}
