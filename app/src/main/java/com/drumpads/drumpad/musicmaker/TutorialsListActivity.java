package com.drumpads.drumpad.musicmaker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.drumpads.drumpad.musicmaker.presets.TutorialInfo;
import com.drumpads.drumpad.musicmaker.presets.TutorialsInit;
import com.drumpads.drumpad.musicmaker.util.IabHelper;
import com.drumpads.drumpad.musicmaker.util.IabResult;
import com.drumpads.drumpad.musicmaker.util.Inventory;
import com.drumpads.drumpad.musicmaker.util.Purchase;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.metrica.YandexMetrica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.drumpads.drumpad.musicmaker.MainActivity.lessons_check;
import static com.drumpads.drumpad.musicmaker.MainActivity.play_check;
import static com.drumpads.drumpad.musicmaker.App.ITEM_SKU;
import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.check;
import static com.drumpads.drumpad.musicmaker.SoundsLoaderPallete.openAppRating;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isAdPresent;
import static com.drumpads.drumpad.musicmaker.SplashScreenActivity.isExitFromApp;

public class TutorialsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listView;
    private RecordsListView recordsListView;

    private TutorialsInit tutorial;

    private SharedPreferences sharedPreferences;

    private LinearLayout recordsBtn, constructBtn, speakerBtn, premium, rateUs, privacy, share;

    private Button next;
    SharedPreferences sPref;

    private AdLoader.Builder builder;
    private AdLoader adLoader;

    String TAG = "SoundsLoaderPallete";

    IabHelper mHelper;
    String base64EncodedPublicKey;

    private boolean isFBRequestPerforms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials_list);

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

        builder = new AdLoader.Builder(this, "ca-app-pub-1333730078271260/5260935938");

        if (!getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE).getBoolean(getString(R.string.isBillingSuccess), false))
            builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                @Override
                public void onContentAdLoaded(NativeContentAd ad) {
                    FrameLayout frameLayout =
                            (FrameLayout) findViewById(R.id.adLinear);
                    NativeContentAdView adView = (NativeContentAdView) getLayoutInflater()
                            .inflate(R.layout.ad_content, null);
                    populateContentAdView(ad, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            });

        adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());

        sharedPreferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);

        String name[] = sharedPreferences.getString(getResources().getString(R.string.default_music_mode), getResources().getString(R.string.default_music_mode)).split("/");

        setTitle(name[name.length - 1]);

        next = (Button) findViewById(R.id.next);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        speakerBtn = (LinearLayout) findViewById(R.id.soundpacks_btn);
        constructBtn = (LinearLayout) findViewById(R.id.constructor_btn);
        recordsBtn = (LinearLayout) findViewById(R.id.records_btn);

        rateUs = (LinearLayout) findViewById(R.id.rate_us_btn);
        privacy = (LinearLayout) findViewById(R.id.privacy_btn);
        share = (LinearLayout) findViewById(R.id.share);
//        rate_btn = (TextView) findViewById(R.id.rate);
//        not_now_btn = (TextView) findViewById(R.id.not_now);

        premium = (LinearLayout) findViewById(R.id.premium);

        if (sharedPreferences.getBoolean(getString(R.string.isBillingSuccess), false))
            premium.setVisibility(View.GONE);

        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                mHelper.launchSubscriptionPurchaseFlow(TutorialsListActivity.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchaseadfreetoken");
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

        speakerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdPresent = true;

                Intent intent = new Intent(getApplicationContext(), SoundsLoaderPallete.class);
                startActivity(intent);
                finish();
            }
        });

        File manualRecordFile = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + "tutorialInits.txt");

        if (!manualRecordFile.exists()) {
            try {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                FileOutputStream f = new FileOutputStream(manualRecordFile);

                PrintWriter pw = new PrintWriter(f);

                pw.println("{\"tutorialsInfo\":[{\"best\":0,\"endPos\":11,\"last\":0,\"startPos\":0},{\"best\":0,\"endPos\":48,\"last\":0,\"startPos\":12},{\"best\":0,\"endPos\":69,\"last\":0,\"startPos\":49},{\"best\":0,\"endPos\":69,\"last\":0,\"startPos\":0}]}\n");

                pw.flush();

                pw.close();

                f.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File file_gson = new File(sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "NONE") + "tutorialInits.txt");

        if (file_gson.exists() || sharedPreferences.getString(getResources().getString(R.string.default_music_mode), getResources().getString(R.string.default_music_mode)).equals(getResources().getString(R.string.default_music_mode))) {
            if (!file_gson.exists() || sharedPreferences.getString(getResources().getString(R.string.default_music_mode), "NONE").equals(getResources().getString(R.string.default_music_mode)))
                file_gson = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/tutorialInits.txt");

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

                    }

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    tutorial = gson.fromJson(text.toString(), TutorialsInit.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            ArrayList<TutorialInfo> tutorialInfos = new ArrayList<>();
            tutorialInfos.add(new TutorialInfo(0, 0, 0, 0));
            tutorial = new TutorialsInit(tutorialInfos);
        }

        int rating = 0;

        for (TutorialInfo tutorialInfo : tutorial.getTutorialsInfo())
            rating += tutorialInfo.getBest();

        rating /= tutorial.getTutorialsInfo().size();

        if (rating >= 80)
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onShowRateUs();
//                    startActivity(new Intent(getApplicationContext(), SoundsLoaderPallete.class));
//
//                    finish();
                }
            });
        else
            next.setVisibility(View.GONE);

        listView = (ListView) findViewById(R.id.listView);
        recordsListView = new RecordsListView(getApplicationContext(), getTutorials());
        listView.setAdapter(recordsListView);

        loadCheck();
        if(check) onCreateDialog();
        saveCheck();

//        if (System.currentTimeMillis() - sharedPreferences.getLong(getResources().getString(R.string.time_from_openning), System.currentTimeMillis()) > 180000 && !sharedPreferences.getBoolean("isFBRated", false))
//            showRateUsFB(TutorialsListActivity.this);
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

                ShareDialog.show(TutorialsListActivity.this, content);

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
    public void onShowRateUs() {
        isAdPresent = true;

        final AlertDialog.Builder builder = new AlertDialog.Builder(TutorialsListActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = TutorialsListActivity.this.getLayoutInflater();
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
                isAdPresent = true;

                openAppRating(getApplicationContext());
//                Uri address = Uri.parse("https://www.youtube.com");
//                Intent openlink = new Intent(Intent.ACTION_VIEW, address);
//                startActivity(openlink);
                a.cancel();
            }
        });

        not_now_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdPresent = true;

                a.cancel();
                startActivity(new Intent(getApplicationContext(), SoundsLoaderPallete.class));
                finish();
            }
        });
    }

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
        ed.putBoolean("check", check);
        ed.putBoolean("lessons_check", lessons_check);
        ed.putBoolean("play_check", play_check);
        ed.apply();
    }

    public void loadCheck() {
        sPref = getPreferences(MODE_PRIVATE);
        check = sPref.getBoolean("check", check);
        lessons_check = sPref.getBoolean("lessons_check", lessons_check);
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

    @SuppressLint("ResourceAsColor")
    public void onCreateDialog() {
        lessons_check = true;
        check = false;
        play_check = true;

        final ImageView imageView26 = (ImageView)findViewById(R.id.imageView26);
        imageView26.setVisibility(View.VISIBLE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(TutorialsListActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = TutorialsListActivity.this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.help_lessons_dialog, null);

        layout.setMinimumHeight(2000);
        layout.setMinimumWidth(2500);
        builder.setView(layout);

        final AlertDialog a = builder.create();
        a.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        WindowManager.LayoutParams lp = a.getWindow().getAttributes();
//        lp.dimAmount = 1f;
//        a.getWindow().setAttributes(lp);
        //a.getWindow().setDimAmount(0.6f);
        a.show();

        a.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                imageView26.setVisibility(View.GONE);
            }
        });

        ImageView imageView18 = (ImageView)layout.findViewById(R.id.imageView18);
        imageView18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView26.setVisibility(View.GONE);
                a.cancel();
            }
        });
    }

//    @SuppressLint("ValidFragment")
//    public class CustomDialogFragment extends DialogFragment {
//        /** The system calls this to get the DialogFragment's layout, regardless
//         of whether it's being displayed as a dialog or an embedded fragment. */
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            // Inflate the layout to use as dialog or embedded fragment
//            return inflater.inflate(R.layout.help_lessons_dialog, container, false);
//        }
//
//        /** The system calls this only when creating the layout in a dialog. */
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            // The only reason you might override this method when using onCreateView() is
//            // to modify any dialog characteristics. For example, the dialog includes a
//            // title by default, but your custom layout might not need it. So here you can
//            // remove the dialog title, but you must call the superclass to get the Dialog.
//            Dialog dialog = super.onCreateDialog(savedInstanceState);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            return dialog;
//        }
//    }
//
//    @SuppressLint("ResourceType")
//    public void showDialog() {
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        CustomDialogFragment newFragment = new CustomDialogFragment();
//
//        // The device is smaller, so show the fragment fullscreen
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        // For a little polish, specify a transition animation
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        // To make it fullscreen, use the 'content' root view as the container
//        // for the fragment, which is always the root view for the activity
//        transaction.add(R.layout.help_lessons_dialog, newFragment)
//                .addToBackStack(null).commit();
//
//    }
//
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(TutorialsListActivity.this);
//        // Get the layout inflater
//        LayoutInflater inflater = TutorialsListActivity.this.getLayoutInflater();
//
//        // Inflate and set the layout for the dialog
//        // Pass null as the parent view because its going in the dialog layout
//        builder.setView(inflater.inflate(R.layout.rate_us_dialog, null));
//                // Add action buttons
////                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
////                    @Override
////                    public void onClick(DialogInterface dialog, int id) {
////                        // sign in the user ...
////                    }
////                })
////                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
////                    public void onClick(DialogInterface dialog, int id) {
////                        LoginDialogFragment.this.getDialog().cancel();
////                    }
////                });
//
//        return builder.create();
//    }

    private ArrayList<TutorialInfo> getTutorials () {
        ArrayList<TutorialInfo> tutorialsInits = tutorial.getTutorialsInfo();

        return tutorialsInits;
    }

    class RecordsListView extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private ArrayList<TutorialInfo> arrayMyMatches;

        public RecordsListView (Context ctx, ArrayList<TutorialInfo> arr) {
            mLayoutInflater = LayoutInflater.from(ctx);
            setArrayMyData(arr);
        }

        public void setArrayMyData(ArrayList<TutorialInfo> arrayMyData) {
            this.arrayMyMatches = arrayMyData;
        }
        public int getCount () {
            return arrayMyMatches.size();
        }

        @Override
        public TutorialInfo getItem (int position) {
            TutorialInfo app = arrayMyMatches.get(position);

            return app;
        }

        public long getItemId (int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.tutorials_list_init_row, null);
                }

                TextView name = convertView.findViewById(R.id.name);
                ImageView star_1 = convertView.findViewById(R.id.star_1);
                ImageView star_2 = convertView.findViewById(R.id.star_2);
                ImageView star_3 = convertView.findViewById(R.id.star_3);

                star_1.setVisibility(View.VISIBLE);
                star_2.setVisibility(View.VISIBLE);
                star_3.setVisibility(View.VISIBLE);

                TextView best = (TextView) convertView.findViewById(R.id.best);
                TextView last = (TextView) convertView.findViewById(R.id.last);

                Button play = (Button) convertView.findViewById(R.id.play);

                int result = arrayMyMatches.get(position).getBest();

                name.setText(getResources().getString(R.string.lesson) + " #" + (position + 1));
                best.setText(getResources().getString(R.string.best_score) + ": " + result);
                last.setText(getResources().getString(R.string.last_score) + ": " + arrayMyMatches.get(position).getLast());

                if (result >= 80 && result <= 85) {
                    star_3.setImageDrawable(getResources().getDrawable(R.drawable.half_star));
                } else if (result >= 60 && result < 80) {
                    star_3.setVisibility(View.GONE);
                } else if (result >= 48 && result < 60) {
                    star_3.setVisibility(View.GONE);
                    star_2.setImageDrawable(getResources().getDrawable(R.drawable.half_star));
                } else if (result >= 30 && result < 48) {
                    star_3.setVisibility(View.GONE);
                    star_2.setVisibility(View.GONE);
                } else if (result >= 10 && result < 30) {
                    star_3.setVisibility(View.GONE);
                    star_2.setVisibility(View.GONE);
                    star_1.setImageDrawable(getResources().getDrawable(R.drawable.half_star));
                } else if (result < 10) {
                    star_3.setVisibility(View.GONE);
                    star_2.setVisibility(View.GONE);
                    star_1.setVisibility(View.GONE);
                }

                play.setEnabled(false);

                if (position > 0 && arrayMyMatches.get(position - 1).getBest() < 80) {}
                else {
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isAdPresent = true;

                            Intent intent = new Intent(getApplicationContext(), PlayGroundActivity.class);
                            intent.putExtra("pos", position);
                            startActivity(intent);

                            YandexMetrica.reportEvent("play lesson");

                            finish();
                        }
                    });

                    play.setEnabled(true);

                    ((LinearLayout) convertView.findViewById(R.id.inective)).setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            } finally {
                return convertView;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        isAdPresent = true;

        startActivity(new Intent(getApplicationContext(), SoundsLoaderPallete.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }
}
