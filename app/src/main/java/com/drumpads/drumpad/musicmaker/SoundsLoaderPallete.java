package com.drumpads.drumpad.musicmaker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drumpads.drumpad.musicmaker.presets.Manual;
import com.drumpads.drumpad.musicmaker.presets.ManualRecord;
import com.drumpads.drumpad.musicmaker.util.IabHelper;
import com.drumpads.drumpad.musicmaker.util.IabResult;
import com.drumpads.drumpad.musicmaker.util.Purchase;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.metrica.YandexMetrica;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.drumpads.drumpad.musicmaker.App.ITEM_SKU;
import static com.drumpads.drumpad.musicmaker.App.mHelper;
import static com.drumpads.drumpad.musicmaker.MainActivity.isLoaded;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isAdPresent;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isBannerWatched;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isExitFromApp;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isManualLoaded;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isPackedOpenning;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isSoundPackLoaded;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isTryOpenning;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.justTakingRewardedWatch;

public class SoundsLoaderPallete extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView soundsLoaderListView;
    public static RecordsListView recordsListView;

    private RecordsListView1 recordsListView1;

    private ArrayList<SoundLoaderInitRow> records;

    private SharedPreferences sharedPreferences;

    private int REQUEST_CODE_PERMISSION_ACCESS_EXTERNAL_STORAGE = 1002;
    private LinearLayout recordsBtn, constructBtn, speakerBtn, premium, rateUs, privacy, share;
    private ListView recordsList;

    private AudioRecording audioRecording;

    private Manual manual;

    public static String lastDownloadedPack;

    private LinearLayout currentSoundpacks, soundpacksLoader;

    public static boolean check = true, checkStart = true;
    public static boolean checkPosition = false;

    public static Activity activity;

    String TAG = "SoundsLoaderPallete";

    String base64EncodedPublicKey;

    FirebaseDatabase database;
    DatabaseReference myRef;

    Timer recordListViewUpdater;

    long[] stagesLikes, stagesDownloads;
    boolean[] stagesDownloadsChanged;

    private boolean isFBRequestPerforms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        String packagename = getApplicationContext().getPackageName();
        String namep = packagename.replaceAll("\\.","");
        myRef = database.getReference(namep);

        sharedPreferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);

        base64EncodedPublicKey = getResources().getString(R.string.base64EncodedPublicKey);

        activity = this;

        setContentView(R.layout.activity_sounds_loader_pallete_drawer);

        getSupportActionBar().hide();

        loadCheck();

        if(checkStart)
            onCreatedDialog();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_ACCESS_EXTERNAL_STORAGE);

            return;
        }

        currentSoundpacks = (LinearLayout) findViewById(R.id.current_soundpacks);
        soundpacksLoader = (LinearLayout) findViewById(R.id.soundpack_loader);

        records = new ArrayList<>();

        soundsLoaderListView = (ListView) findViewById(R.id.sounds_loader_pallete);

        recordsListView = new RecordsListView(getApplicationContext(), getRecords());
        soundsLoaderListView.setAdapter(recordsListView);

        stagesLikes = new long[recordsListView.arrayMyMatches.size()];
        stagesDownloads = new long[recordsListView.arrayMyMatches.size()];
        stagesDownloadsChanged = new boolean[recordsListView.arrayMyMatches.size()];

        registerForContextMenu(soundsLoaderListView);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView menu_btn = (ImageView) findViewById(R.id.menu_btn);
        menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        speakerBtn = (LinearLayout) findViewById(R.id.soundpacks_btn);
        constructBtn = (LinearLayout) findViewById(R.id.constructor_btn);
        recordsBtn = (LinearLayout) findViewById(R.id.records_btn);
        rateUs = (LinearLayout) findViewById(R.id.rate_us_btn);
        privacy = (LinearLayout) findViewById(R.id.privacy_btn);
        share = (LinearLayout) findViewById(R.id.share);
        premium = (LinearLayout) findViewById(R.id.premium);

        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.GONE);

        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                mHelper.launchSubscriptionPurchaseFlow(SoundsLoaderPallete.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
            }
        });

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

        try {
            rateUs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isAdPresent = true;
                    openAppRating(getApplicationContext());
                }
            });
        } catch (Exception e) {

        }

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                Uri policy = Uri.parse("https://docs.google.com/document/d/1zZxIAtxv0Nfq2qirgIOpI_kgO2x1WCXgbN-xysRgoxA/edit?usp=sharing");
                Intent policy_link = new Intent (Intent.ACTION_VIEW, policy);
                startActivity (policy_link);
            }
        });

        speakerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                onBackPressed();
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

        ((ImageView) findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            createRecordsListView();

        recordListViewUpdater = new Timer();
        recordListViewUpdater.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        SoundsLoaderPallete.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recordsListView.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        }, 0, 700);

//        if (System.currentTimeMillis() - sharedPreferences.getLong(getResources().getString(R.string.time_from_openning), System.currentTimeMillis()) > 180000 && !sharedPreferences.getBoolean("isFBRated", false))
//            showRateUsFB(SoundsLoaderPallete.this);

        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.GONE);
        else
            premium.setVisibility(View.VISIBLE);
    }

    public void consumeItem() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
        editor.putBoolean(getResources().getString(R.string.isBillingSuccess), true);
        editor.apply();

        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.GONE);
    }

    public void unConsumeItem() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
        editor.putBoolean(getResources().getString(R.string.isBillingSuccess), false);
        editor.apply();

        if (!sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.VISIBLE);
    }

    private void showRateUsFB(final Activity context){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.sharing_rate_us, null);
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
                isFBRequestPerforms = true;

                String appId = getPackageName();

                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + appId))
                        .setContentTitle("Make awesome music with Music Maker app")
                        .build();

                ShareDialog.show(SoundsLoaderPallete.this, content);

                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
                editor.putBoolean("isFBRated", true);
                editor.apply();

                a.cancel();
            }
        });

        not_now_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a.cancel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFBRequestPerforms) {
            isAdPresent = true;
            recreate();
        }

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

    public void saveCheck() {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean("checkingStart", checkStart);
        ed.apply();
    }

    public void loadCheck() {
        checkStart = sharedPreferences.getBoolean("checkingStart", checkStart);
    }

    public void onCreatedDialog() {
        final RelativeLayout starter = (RelativeLayout) findViewById(R.id.starter);
        starter.setVisibility(View.VISIBLE);

        starter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starter.setVisibility(View.GONE);

                checkStart = false;

                saveCheck();
            }
        });
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
                if (result.getMessage().equals("IAB returned null purchaseData or dataSignature (response: -1008:Unknown error)") || result.getMessage().contains("purchaseData") || result.getMessage().contains("dataSignature") || result.getMessage().toLowerCase().contains("owned"))
                    consumeItem();

                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        recordListViewUpdater.cancel();
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
                break;

            }
        }
    }

    private ArrayList<SoundLoaderInitRow> getRecords () {
        final SoundLoaderInitRow soundLoaderInitRow = new SoundLoaderInitRow(getResources().getString(R.string.default_music_mode));
        soundLoaderInitRow.setExecutorName("TheFatRat");
        soundLoaderInitRow.setPremium(false);
        soundLoaderInitRow.setName("Monody");

        records.add(soundLoaderInitRow);
        records.get(0).setDefault(true);

        for (SoundLoaderInitRow row : getPacks())
            records.add(row);

        records.get(0).setDownloading(false);

        return records;
    }

    public void playStart(final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(fileName);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class RecordsListView extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private ArrayList<SoundLoaderInitRow> arrayMyMatches;

        public RecordsListView (Context ctx, ArrayList<SoundLoaderInitRow> arr) {
            mLayoutInflater = LayoutInflater.from(ctx);
            setArrayMyData(arr);
        }

        public void setArrayMyData(ArrayList<SoundLoaderInitRow> arrayMyData) {
            this.arrayMyMatches = arrayMyData;
        }
        public int getCount () {
            return arrayMyMatches.size();
        }

        @Override
        public SoundLoaderInitRow getItem (int position) {
            SoundLoaderInitRow app = arrayMyMatches.get(position);

            return app;
        }

        public void remove(int position) {
            arrayMyMatches.remove(position);
            SoundsLoaderPallete.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordsListView.notifyDataSetChanged();
                }
            });
        }

        public long getItemId (int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.sounds_loader_init_row, null);
                }

                LinearLayout icon = (LinearLayout) convertView.findViewById(R.id.icon);
                TextView name = (TextView) convertView.findViewById(R.id.name);
                TextView executorName = (TextView) convertView.findViewById(R.id.executor_name);
                ImageView playBtn = (ImageView) convertView.findViewById(R.id.play_btn);

                LinearLayout user_play_btn = (LinearLayout) convertView.findViewById(R.id.user_created_play_btn);
                LinearLayout user_try_btn = (LinearLayout) convertView.findViewById(R.id.user_try_btn);
                LinearLayout user_second_play_btn = (LinearLayout) convertView.findViewById(R.id.user_second_play_btn);
                LinearLayout user_download_btn = (LinearLayout) convertView.findViewById(R.id.user_download_btn);

                user_play_btn.setVisibility(View.GONE);
                user_try_btn.setVisibility(View.GONE);
                user_second_play_btn.setVisibility(View.GONE);
                user_download_btn.setVisibility(View.GONE);

                final LinearLayout liker = (LinearLayout) convertView.findViewById(R.id.liker);
                liker.setEnabled(true);
                final LinearLayout downloader = (LinearLayout) convertView.findViewById(R.id.downloader);
                final ImageView liker_img = (ImageView) convertView.findViewById(R.id.liker_img);
                final TextView liker_txt = (TextView) convertView.findViewById(R.id.liker_txt);
                final TextView downloads_txt = (TextView) convertView.findViewById(R.id.downloads);
                final ImageView premium_download = (ImageView) convertView.findViewById(R.id.premium_download);

                if (arrayMyMatches.get(position).isPremium())
                    premium_download.setVisibility(View.VISIBLE);
                else
                    premium_download.setVisibility(View.GONE);

                downloader.setVisibility(View.VISIBLE);
                liker.setVisibility(View.VISIBLE);

                if (arrayMyMatches.get(position).getName().equals(getResources().getString(R.string.default_music_mode)))
                    downloader.setVisibility(View.GONE);

                liker_img.setImageDrawable(getResources().getDrawable(R.drawable.unliked));

                if (sharedPreferences.getBoolean(arrayMyMatches.get(position).getReference(), false))
                    liker_img.setImageDrawable(getResources().getDrawable(R.drawable.liked));

                liker_txt.setText(stagesLikes[position] + "");
                downloads_txt.setText(stagesDownloads[position] + "");

                if (!sharedPreferences.getBoolean(getResources().getString(R.string.isTryAvalable), false)) {
                    user_second_play_btn.setEnabled(false);
                    user_second_play_btn.setBackground(getResources().getDrawable(R.drawable.bgn_sound_loader_bg_inactive));
                    user_second_play_btn.setAlpha(0.5f);

                    user_play_btn.setEnabled(false);
                    user_play_btn.setBackground(getResources().getDrawable(R.drawable.bgn_sound_loader_bg_inactive));
                    user_play_btn.setAlpha(0.5f);

                    user_try_btn.setEnabled(false);
                    user_try_btn.setBackground(getResources().getDrawable(R.drawable.bgn_sound_loader_bg_inactive));
                    user_try_btn.setAlpha(0.5f);

                    user_download_btn.setEnabled(false);
                    user_download_btn.setBackground(getResources().getDrawable(R.drawable.bgn_sound_loader_bg_inactive));
                    user_download_btn.setAlpha(0.5f);
                }

                final String variableName = arrayMyMatches.get(position).getName().replace(" ", "_");

                myRef.child(variableName + "_likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            stagesLikes[position] = Long.parseLong(dataSnapshot.getValue().toString());
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                myRef.child(variableName + "_downloads").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            stagesDownloads[position] = Long.parseLong(dataSnapshot.getValue().toString());

                            if (stagesDownloadsChanged[position])
                                stagesDownloads[position]--;
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if (sharedPreferences.getBoolean(arrayMyMatches.get(position).getReference(), false))
                    liker.setEnabled(false);

                liker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (!sharedPreferences.getBoolean(arrayMyMatches.get(position).getReference(), false))
                                YandexMetrica.reportEvent(arrayMyMatches.get(position).getName() + "_liked");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(arrayMyMatches.get(position).getReference(), !sharedPreferences.getBoolean(arrayMyMatches.get(position).getReference(), false));
                            editor.apply();

                            myRef.child(variableName + "_likes").setValue(stagesLikes[position] + 1);

                            stagesLikes[position]++;

                            SoundsLoaderPallete.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    recordsListView.notifyDataSetChanged();
                                }
                            });

                            liker.setEnabled(false);
                        } catch (Exception e) {
                            Toast.makeText(SoundsLoaderPallete.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                if (arrayMyMatches.get(position).getReference() == null)
                    playBtn.setBackgroundResource(android.R.color.transparent);

                try {
                    if (arrayMyMatches.get(position).getIcon() != null && new File(arrayMyMatches.get(position).getIcon()).exists())
                        icon.setBackground(Drawable.createFromPath(arrayMyMatches.get(position).getIcon()));
                    else
                        icon.setBackground(getResources().getDrawable(R.drawable.default_image));
                } catch (Exception e) {
                    Toast.makeText(SoundsLoaderPallete.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                }

                name.setText(arrayMyMatches.get(position).getName());
                executorName.setText(arrayMyMatches.get(position).getExecutorName());

                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playStart(arrayMyMatches.get(position).getReference());
                    }
                });

                ((LinearLayout) convertView.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteDirectory(new File (arrayMyMatches.get(position).getPath()), false);

                        recordsListView.remove(position);
                    }
                });

                ((LinearLayout) convertView.findViewById(R.id.delete)).setVisibility(View.GONE);

                if (arrayMyMatches.get(position).getName().equals(getResources().getString(R.string.default_music_mode))) {
                    user_try_btn.setEnabled(true);
                    user_try_btn.setBackground(getResources().getDrawable(R.drawable.btn_sound_loader_bg));
                    user_try_btn.setAlpha(1.0f);
                }

                if (arrayMyMatches.get(position).isDefault()) {
                    user_try_btn.setVisibility(View.VISIBLE);
                    user_try_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isAdPresent = true;

                            YandexMetrica.reportEvent("lessons");

                            SharedPreferences.Editor editor1 = sharedPreferences.edit();
                            editor1.putBoolean(getResources().getString(R.string.isTryAvalable), true);
                            editor1.apply();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.default_music_mode), arrayMyMatches.get(position).getPath());

                            if(position == 0) checkPosition = true; else checkPosition = false;

                            editor.apply();

                            isTryOpenning = true;

                            if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false) || arrayMyMatches.get(position).getName().equals(getResources().getString(R.string.default_music_mode))) {
                                soundpacksLoader.setVisibility(View.VISIBLE);
                                soundpacksLoader.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });

                                isAdPresent = true;

                                isBannerWatched = true;
                                SplashScreenActivity.isFailedToLoadRewarded = true;

                                startActivity(new Intent(getApplicationContext(), TutorialsListActivity.class));
                                finish();
                            } else {
                                final LinearLayout layout_root = (LinearLayout) findViewById(R.id.layout_root);
                                layout_root.setVisibility(View.VISIBLE);

                                ImageView yes_btn = (ImageView) findViewById(R.id.yes_btn);
                                ImageView premium = (ImageView) findViewById(R.id.premiums);
                                ImageView close = (ImageView) findViewById(R.id.close);

                                TextView root_title = (TextView) findViewById(R.id.root_title);
                                root_title.setText("Watch the video to play lessons or try 7 days premium for free");

                                yes_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            isAdPresent = true;
                                            isSoundPackLoaded = false;
                                            isBannerWatched = false;

                                            if (SplashScreenActivity.mRewardedVideoAd.isLoaded())
                                                SplashScreenActivity.mRewardedVideoAd.show();
                                            else if (SplashScreenActivity.mInterstitialAd.isLoaded())
                                                SplashScreenActivity.mInterstitialAd.show();
                                            else {
                                                isAdPresent = true;
                                                startActivity(new Intent(getApplicationContext(), TutorialsListActivity.class));
                                                finish();
                                            }
                                        } catch (Exception e) {
                                        }

                                        YandexMetrica.reportEvent("video watch");

                                        layout_root.setVisibility(View.GONE);
                                    }
                                });

                                premium.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        layout_root.setVisibility(View.GONE);

                                        YandexMetrica.reportEvent("premium click");

                                        final LinearLayout premium_layout = (LinearLayout) findViewById(R.id.premium_layout);
                                        premium_layout.setVisibility(View.VISIBLE);

                                        LinearLayout cancel_it = (LinearLayout) findViewById(R.id.cancel_it);
                                        LinearLayout try_it = (LinearLayout) findViewById(R.id.try_it);
                                        LinearLayout about_it = (LinearLayout) findViewById(R.id.about_it);

                                        cancel_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);
                                            }
                                        });

                                        try_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);

                                                YandexMetrica.reportEvent("try trial");

                                                mHelper.launchSubscriptionPurchaseFlow(SoundsLoaderPallete.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
                                            }
                                        });

                                        about_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);

                                                Uri policy = Uri.parse("https://docs.google.com/document/d/19kOgdWOFAi7jeFsULh0hdAhrjsbOdNeQAQ-AOtqg2Z0/edit?usp=sharing");
                                                Intent policy_link = new Intent (Intent.ACTION_VIEW, policy);
                                                startActivity (policy_link);
                                            }
                                        });
                                    }
                                });

                                close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        layout_root.setVisibility(View.GONE);

                                        YandexMetrica.reportEvent("no watch video");
                                    }
                                });
                            }
                        }
                    });

                    if (!sharedPreferences.getBoolean(getResources().getString(R.string.isTryAvalable), false)) {
                        user_second_play_btn.setEnabled(false);
                        user_second_play_btn.setBackground(getResources().getDrawable(R.drawable.bgn_sound_loader_bg_inactive));
                        user_second_play_btn.setAlpha(0.5f);
                    }

                    user_second_play_btn.setVisibility(View.VISIBLE);
                    user_second_play_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            isAdPresent = true;
                            isPackedOpenning = true;

                            YandexMetrica.reportEvent("play");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.default_music_mode), arrayMyMatches.get(position).getPath());

                            editor.apply();

                            if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false) || arrayMyMatches.get(position).getName().equals(getResources().getString(R.string.default_music_mode))) {
                                soundpacksLoader.setVisibility(View.VISIBLE);
                                soundpacksLoader.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });

                                isBannerWatched = true;
                                SplashScreenActivity.isFailedToLoadRewarded = true;

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                finish();
                            } else {
                                final LinearLayout layout_root = (LinearLayout) findViewById(R.id.layout_root);
                                layout_root.setVisibility(View.VISIBLE);

                                ImageView yes_btn = (ImageView) findViewById(R.id.yes_btn);
                                ImageView premium = (ImageView) findViewById(R.id.premiums);
                                ImageView close = (ImageView) findViewById(R.id.close);

                                TextView root_title = (TextView) findViewById(R.id.root_title);
                                root_title.setText("Watch the video to play lessons or try 7 days premium for free");

                                yes_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            isSoundPackLoaded = false;
                                            isBannerWatched = false;

                                            if (SplashScreenActivity.mRewardedVideoAd.isLoaded())
                                                SplashScreenActivity.mRewardedVideoAd.show();
                                            else if (SplashScreenActivity.mInterstitialAd.isLoaded())
                                                SplashScreenActivity.mInterstitialAd.show();
                                            else {
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                finish();
                                            }
                                        } catch (Exception e) {
                                        }

                                        YandexMetrica.reportEvent("video watch");

                                        layout_root.setVisibility(View.GONE);
                                    }
                                });

                                premium.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        layout_root.setVisibility(View.GONE);

                                        YandexMetrica.reportEvent("premium click");

                                        final LinearLayout premium_layout = (LinearLayout) findViewById(R.id.premium_layout);
                                        premium_layout.setVisibility(View.VISIBLE);

                                        LinearLayout cancel_it = (LinearLayout) findViewById(R.id.cancel_it);
                                        LinearLayout try_it = (LinearLayout) findViewById(R.id.try_it);
                                        LinearLayout about_it = (LinearLayout) findViewById(R.id.about_it);

                                        cancel_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);
                                            }
                                        });

                                        try_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);

                                                YandexMetrica.reportEvent("try trial");

                                                mHelper.launchSubscriptionPurchaseFlow(SoundsLoaderPallete.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
                                            }
                                        });

                                        about_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);

                                                Uri policy = Uri.parse("https://docs.google.com/document/d/19kOgdWOFAi7jeFsULh0hdAhrjsbOdNeQAQ-AOtqg2Z0/edit?usp=sharing");
                                                Intent policy_link = new Intent (Intent.ACTION_VIEW, policy);
                                                startActivity (policy_link);
                                            }
                                        });
                                    }
                                });

                                close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        layout_root.setVisibility(View.GONE);

                                        YandexMetrica.reportEvent("no watch video");
                                    }
                                });
                            }
                        }
                    });
                } else if (arrayMyMatches.get(position).isDownloading()) {
                    user_download_btn.setVisibility(View.VISIBLE);
                    user_download_btn.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(View view) {
                            isAdPresent = true;

                            YandexMetrica.reportEvent("download");

                            if (!stagesDownloadsChanged[position]) {
                                myRef.child(variableName + "_downloads").setValue(stagesDownloads[position] + 1);

                                stagesDownloadsChanged[position] = true;
                            }

                            if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false)) {
                                soundpacksLoader.setVisibility(View.VISIBLE);
                                soundpacksLoader.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });

                                isBannerWatched = true;
                                justTakingRewardedWatch = true;
                                SplashScreenActivity.isFailedToLoadRewarded = true;

                                File manualRecordFile = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/soundpacks/" + arrayMyMatches.get(position).getName() + "/manualRecord.txt");
                                lastDownloadedPack = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/soundpacks/" + arrayMyMatches.get(position).getName() + "/";

                                if (manualRecordFile.exists())
                                    try {
                                        StringBuilder text = new StringBuilder();

                                        try {
                                            BufferedReader br = new BufferedReader(new FileReader(manualRecordFile));
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
                                        final ManualRecord manualRecord = gson.fromJson(text.toString(), ManualRecord.class);

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!isSoundPackLoaded) {
                                                    reloadPage();
                                                }
                                            }
                                        }, 15000);

                                        manualRecord.createSoundPack(SoundsLoaderPallete.this, ((ProgressBar) findViewById(R.id.progressBar)), ((TextView) findViewById(R.id.current_progress)));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                            } else {
                                final LinearLayout layout_root = (LinearLayout) findViewById(R.id.layout_root);
                                layout_root.setVisibility(View.VISIBLE);

                                ImageView yes_btn = (ImageView) findViewById(R.id.yes_btn);
                                ImageView premium = (ImageView) findViewById(R.id.premiums);
                                ImageView close = (ImageView) findViewById(R.id.close);

                                TextView yes_text = (TextView) findViewById(R.id.yes_text);
                                TextView no_text = (TextView) findViewById(R.id.no_text);

                                ImageView watch_logo = (ImageView) findViewById(R.id.watch_logo);
                                ImageView premium_logo = (ImageView) findViewById(R.id.premium_logo);

                                yes_text.setText("Watch");
                                no_text.setText("Premium");

                                watch_logo.setVisibility(View.VISIBLE);
                                premium_logo.setVisibility(View.VISIBLE);

                                yes_btn.setImageDrawable(getResources().getDrawable(R.drawable.watch_video_rectangle));
                                premium.setImageDrawable(getResources().getDrawable(R.drawable.finish_btn));

                                if (arrayMyMatches.get(position).isPremium()) {
                                    yes_text.setText("Yes");
                                    no_text.setText("No");

                                    watch_logo.setVisibility(View.GONE);
                                    premium_logo.setVisibility(View.GONE);

                                    yes_btn.setImageDrawable(getResources().getDrawable(R.drawable.finish_btn));
                                    premium.setImageDrawable(getResources().getDrawable(R.drawable.watch_video_rectangle));

                                    try {
                                        TextView root_title = (TextView) findViewById(R.id.root_title);
                                        root_title.setText("This soundpack is available only for premium users. Do you want to try 7-days premium for free?");
                                    } catch (Exception e) {
                                        Toast.makeText(SoundsLoaderPallete.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                                    }

                                    yes_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            layout_root.setVisibility(View.GONE);

                                            YandexMetrica.reportEvent("premium click");

                                            final LinearLayout premium_layout = (LinearLayout) findViewById(R.id.premium_layout);
                                            premium_layout.setVisibility(View.VISIBLE);

                                            LinearLayout cancel_it = (LinearLayout) findViewById(R.id.cancel_it);
                                            LinearLayout try_it = (LinearLayout) findViewById(R.id.try_it);
                                            LinearLayout about_it = (LinearLayout) findViewById(R.id.about_it);

                                            cancel_it.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    premium_layout.setVisibility(View.GONE);
                                                }
                                            });

                                            try_it.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    premium_layout.setVisibility(View.GONE);

                                                    YandexMetrica.reportEvent("try trial");

                                                    mHelper.launchSubscriptionPurchaseFlow(SoundsLoaderPallete.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
                                                }
                                            });

                                            about_it.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    premium_layout.setVisibility(View.GONE);

                                                    Uri policy = Uri.parse("https://docs.google.com/document/d/19kOgdWOFAi7jeFsULh0hdAhrjsbOdNeQAQ-AOtqg2Z0/edit?usp=sharing");
                                                    Intent policy_link = new Intent (Intent.ACTION_VIEW, policy);
                                                    startActivity (policy_link);
                                                }
                                            });
                                        }
                                    });

                                    premium.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            layout_root.setVisibility(View.GONE);
                                        }
                                    });

                                    close.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            layout_root.setVisibility(View.GONE);

                                            YandexMetrica.reportEvent("no watch video");
                                        }
                                    });
                                } else {
                                    try {
                                        TextView root_title = (TextView) findViewById(R.id.root_title);
                                        root_title.setText("Watch the video to play soundpack or try 7 days premium for free");
                                    } catch (Exception e) {
                                        Toast.makeText(SoundsLoaderPallete.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                                    }

                                    yes_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            justTakingRewardedWatch = true;

                                            try {
                                                soundpacksLoader.setVisibility(View.VISIBLE);
                                                soundpacksLoader.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                    }
                                                });

                                                isSoundPackLoaded = false;
                                                justTakingRewardedWatch = true;

                                                if (SplashScreenActivity.mRewardedVideoAd.isLoaded()) {
                                                    SplashScreenActivity.mRewardedVideoAd.show();

                                                    isBannerWatched = false;
                                                } else if (SplashScreenActivity.mInterstitialAd.isLoaded())
                                                    SplashScreenActivity.mInterstitialAd.show();
                                                else {
                                                    isBannerWatched = true;
                                                    justTakingRewardedWatch = true;
                                                    SplashScreenActivity.isFailedToLoadRewarded = true;
                                                }
                                            } catch (Exception e) {
                                            }

                                            YandexMetrica.reportEvent("video watch");

                                            layout_root.setVisibility(View.GONE);

                                            File manualRecordFile = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/soundpacks/" + arrayMyMatches.get(position).getName() + "/manualRecord.txt");
                                            lastDownloadedPack = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/soundpacks/" + arrayMyMatches.get(position).getName() + "/";

                                            if (manualRecordFile.exists())
                                                try {
                                                    StringBuilder text = new StringBuilder();

                                                    try {
                                                        BufferedReader br = new BufferedReader(new FileReader(manualRecordFile));
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
                                                    final ManualRecord manualRecord = gson.fromJson(text.toString(), ManualRecord.class);

                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (!isSoundPackLoaded) {
                                                                reloadPage();
                                                            }
                                                        }
                                                    }, 15000);

                                                    manualRecord.createSoundPack(SoundsLoaderPallete.this, ((ProgressBar) findViewById(R.id.progressBar)), ((TextView) findViewById(R.id.current_progress)));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                        }
                                    });

                                    premium.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            layout_root.setVisibility(View.GONE);

                                            YandexMetrica.reportEvent("premium click");

                                            final LinearLayout premium_layout = (LinearLayout) findViewById(R.id.premium_layout);
                                            premium_layout.setVisibility(View.VISIBLE);

                                            LinearLayout cancel_it = (LinearLayout) findViewById(R.id.cancel_it);
                                            LinearLayout try_it = (LinearLayout) findViewById(R.id.try_it);
                                            LinearLayout about_it = (LinearLayout) findViewById(R.id.about_it);

                                            cancel_it.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    premium_layout.setVisibility(View.GONE);
                                                }
                                            });

                                            try_it.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    premium_layout.setVisibility(View.GONE);

                                                    YandexMetrica.reportEvent("try trial");

                                                    mHelper.launchSubscriptionPurchaseFlow(SoundsLoaderPallete.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
                                                }
                                            });

                                            about_it.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    premium_layout.setVisibility(View.GONE);

                                                    Uri policy = Uri.parse("https://docs.google.com/document/d/19kOgdWOFAi7jeFsULh0hdAhrjsbOdNeQAQ-AOtqg2Z0/edit?usp=sharing");
                                                    Intent policy_link = new Intent (Intent.ACTION_VIEW, policy);
                                                    startActivity (policy_link);
                                                }
                                            });
                                        }
                                    });

                                    close.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            layout_root.setVisibility(View.GONE);

                                            YandexMetrica.reportEvent("no watch video");
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    if (!sharedPreferences.getBoolean(getResources().getString(R.string.isTryAvalable), false)) {
                        user_play_btn.setEnabled(false);
                        user_play_btn.setBackground(getResources().getDrawable(R.drawable.bgn_sound_loader_bg_inactive));
                        user_play_btn.setAlpha(0.5f);
                    }

                    user_play_btn.setVisibility(View.VISIBLE);
                    user_play_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isAdPresent = true;

                            isPackedOpenning = true;

                            YandexMetrica.reportEvent("play");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.default_music_mode), arrayMyMatches.get(position).getPath());

                            editor.apply();

                            if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false) || arrayMyMatches.get(position).getName().equals(getResources().getString(R.string.default_music_mode))) {
                                soundpacksLoader.setVisibility(View.VISIBLE);
                                soundpacksLoader.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });

                                isBannerWatched = true;
                                SplashScreenActivity.isFailedToLoadRewarded = true;

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                finish();
                            } else {
                                final LinearLayout layout_root = (LinearLayout) findViewById(R.id.layout_root);
                                layout_root.setVisibility(View.VISIBLE);

                                ImageView yes_btn = (ImageView) findViewById(R.id.yes_btn);
                                ImageView premium = (ImageView) findViewById(R.id.premiums);
                                ImageView close = (ImageView) findViewById(R.id.close);

                                TextView root_title = (TextView) findViewById(R.id.root_title);
                                root_title.setText("Watch the video to play lessons or try 7 days premium for free");

                                yes_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            isSoundPackLoaded = false;
                                            isBannerWatched = false;

                                            if (SplashScreenActivity.mRewardedVideoAd.isLoaded())
                                                SplashScreenActivity.mRewardedVideoAd.show();
                                            else if (SplashScreenActivity.mInterstitialAd.isLoaded())
                                                SplashScreenActivity.mInterstitialAd.show();
                                            else {
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                finish();
                                            }
                                        } catch (Exception e) {
                                        }

                                        layout_root.setVisibility(View.GONE);

                                        YandexMetrica.reportEvent("video watch");
                                    }
                                });

                                premium.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        layout_root.setVisibility(View.GONE);

                                        final LinearLayout premium_layout = (LinearLayout) findViewById(R.id.premium_layout);
                                        premium_layout.setVisibility(View.VISIBLE);

                                        YandexMetrica.reportEvent("premium click");

                                        LinearLayout cancel_it = (LinearLayout) findViewById(R.id.cancel_it);
                                        LinearLayout try_it = (LinearLayout) findViewById(R.id.try_it);
                                        LinearLayout about_it = (LinearLayout) findViewById(R.id.about_it);

                                        cancel_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);
                                            }
                                        });

                                        try_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);

                                                YandexMetrica.reportEvent("try trial");

                                                mHelper.launchSubscriptionPurchaseFlow(SoundsLoaderPallete.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
                                            }
                                        });

                                        about_it.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                premium_layout.setVisibility(View.GONE);

                                                Uri policy = Uri.parse("https://docs.google.com/document/d/19kOgdWOFAi7jeFsULh0hdAhrjsbOdNeQAQ-AOtqg2Z0/edit?usp=sharing");
                                                Intent policy_link = new Intent (Intent.ACTION_VIEW, policy);
                                                startActivity (policy_link);
                                            }
                                        });
                                    }
                                });

                                close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        layout_root.setVisibility(View.GONE);

                                        YandexMetrica.reportEvent("no watch video");
                                    }
                                });
                            }
                        }
                    });

                    user_try_btn.setVisibility(View.VISIBLE);
                    user_try_btn.setEnabled(false);

                    ((LinearLayout) convertView.findViewById(R.id.delete)).setVisibility(View.VISIBLE);

                    downloader.setVisibility(View.GONE);
                    liker.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            } finally {
                return convertView;
            }
        }
    }

    private ArrayList<SoundLoaderInitRow> getPacks () {
        ArrayList<SoundLoaderInitRow> records = new ArrayList<>();

        String directoryName = Environment.getExternalStorageDirectory() + "/"
                + getResources().getString(R.string.app_name) + "/soundpacks/";

        File outFileDirectory = new File(directoryName);

        if (!outFileDirectory.exists())
            outFileDirectory.mkdirs();

        for (File record: listFilesWithSubFolders(outFileDirectory)) {
            if (record.getPath().contains("customPack"))
                continue;

            records.add(new SoundLoaderInitRow(record.getPath() + "/"));
            records.get(records.size() - 1).setName(record.getName());

            records.get(records.size() - 1).setIcon(Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name) + "/soundpacks/" + record.getPath().substring(record.getPath().lastIndexOf("/")) + "/icon.jpg");

            records.get(records.size() - 1).setReference(Environment.getExternalStorageDirectory() + "/"
                    + getResources().getString(R.string.app_name) + "/soundpacks/" + record.getPath().substring(record.getPath().lastIndexOf("/") + 1) + "/" + "presound.wav");

            if (new File(record.getPath() + "/manualRecordPremium.txt").exists() && !sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
                records.get(records.size() - 1).setPremium(true);

            if (new File(record.getPath() + "/manualRecord.txt").exists() && listFilesWithSubFolders(record).size() >= 27)
                records.get(records.size() - 1).setDefault(true);
            else
                records.get(records.size() - 1).setCustom(true);

            if (new File(record.getPath() + "/manualRecord.txt").exists() && listFilesWithSubFolders(record).size() <= 26)
                records.get(records.size() - 1).setDownloading(true);

            try {
                if (new File(record.getPath() + "/manualRecord.txt").exists()) {
                    File file = new File(record.getPath() + "/manualRecord.txt");

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
                            ManualRecord manualRecord = gson.fromJson(text.toString(), ManualRecord.class);
                            records.get(records.size() - 1).setExecutorName(manualRecord.getArtist());
                        } catch (Exception j) {

                        }
                    }
                }
            } catch (Exception e) {

            }
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (!isLoaded) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isManualLoaded || (manual != null && !manual.isManualLoaded)) {
                        reloadPage();
                    }
                }
            }, 15000);

            new LoadFile("http://mmaker.club/catalog/manual.txt",
                    new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/manual.txt")).start();
        }
        return records;
    }

    private void reloadPage () {
        isAdPresent = true;

//        try {
//            LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View promptsView = li.inflate(R.layout.reset_downloading_page, null);
//
//            final AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(SoundsLoaderPallete.this);
//            mDialogBuilder.setView(promptsView);
//
//            ImageView yes_btn = (ImageView) promptsView.findViewById(R.id.yes_btn);
//
//            mDialogBuilder
//                    .setCancelable(false);
//
//            final AlertDialog alertDialog = mDialogBuilder.create();
//
//            yes_btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    recreate();
//                }
//            });
//
//            alertDialog.show();
//        } catch (Exception e) {
//
//        }
    }

    private void onDownloadComplete(boolean success) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/manual.txt");

            if (file.exists())
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

                    isLoaded = true;

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    manual = gson.fromJson(text.toString(), Manual.class);
                    isManualLoaded = true;
                    manual.createSoundPacks(this);

                    ((ProgressBar) findViewById(R.id.progressBar)).setProgress(100);
                    ((TextView) findViewById(R.id.current_progress)).setText(100 + "%");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            soundpacksLoader.setVisibility(View.GONE);

                            ((ProgressBar) findViewById(R.id.progressBar)).setProgress(0);
                            ((TextView) findViewById(R.id.current_progress)).setText(0 + "%");
                        }
                    }, 300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        } catch (Exception e) {
        }
    }

    private class LoadFile extends Thread {
        private final String src;
        private final File dest;

        LoadFile(String src, File dest) {
            soundpacksLoader.setVisibility(View.VISIBLE);

            this.src = src;
            this.dest = dest;
        }

        @Override
        public void run() {
            isAdPresent = true;

            try {
                FileUtils.copyURLToFile(new URL(src), dest);
                onDownloadComplete(true);
            } catch (IOException e) {
                e.printStackTrace();
                onDownloadComplete(false);
            }
        }
    }

    public ArrayList<File> listFilesWithSubFolders(File dir) {
        ArrayList<File> files = new ArrayList<>();
        for (File file : dir.listFiles())
            files.add(file);

        return files;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            recreate();
        else
            finish();
    }

    @Override
    public void onBackPressed() {
        if (soundpacksLoader.getVisibility() == View.VISIBLE) {
            soundpacksLoader.setVisibility(View.GONE);

            return;
        }

        final LinearLayout premium_layout = (LinearLayout) findViewById(R.id.premium_layout);

        if (premium_layout.getVisibility() == View.VISIBLE) {
            premium_layout.setVisibility(View.GONE);

            return;
        }

        try {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return;
            } else {
                final AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(SoundsLoaderPallete.this);
                mDialogBuilder.setCancelable(true).setTitle("Are you sure you want to quite?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startHomeScreen();

                        recordListViewUpdater.cancel();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                mDialogBuilder.show();
            }
        }catch (Exception e) {}
    }

    public void startHomeScreen() {
            Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
            startHomescreen.addCategory(Intent.CATEGORY_HOME);
            startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startHomescreen);
    }

    private void createRecordsListView () {
        recordsList = (ListView) findViewById(R.id.records_list_origins);

        recordsListView1 = new RecordsListView1(getApplicationContext(), getRecords1());
        recordsList.setAdapter(recordsListView1);
    }

    public static boolean deleteDirectory(File path, boolean isLoaded) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i], isLoaded);
                } else {
                    if (isLoaded && !files[i].getPath().contains("manualRecord.txt"))
                        files[i].delete();
                    else if (!isLoaded)
                        files[i].delete();
                }
            }
        }

        if (!isLoaded)
            return (path.delete());

        return true;
    }

    class RecordsListView1 extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private ArrayList<InitRecordItem> arrayMyMatches;

        public RecordsListView1 (Context ctx, ArrayList<InitRecordItem> arr) {
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
                    Toast.makeText(SoundsLoaderPallete.this, "Deleted successfuly", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(SoundsLoaderPallete.this, e.toString() + "", Toast.LENGTH_SHORT).show();
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

                ImageView play = convertView.findViewById(R.id.play_music);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            File file = new File(getRecords1().get(position).getPath());

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

                            audioRecording.playSound(SoundsLoaderPallete.this);
                        } catch (Exception e) {

                        }
                    }
                });

                ImageView share = convertView.findViewById(R.id.share_btn);
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);

                        intentShareFile.setType("text/*");
                        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + getRecords1().get(position).getPath()));

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
                        recordsListView1.remove(position);
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

    private ArrayList<InitRecordItem> getRecords1 () {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
