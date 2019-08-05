package com.drumpads.drumpad.musicmaker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.drumpads.drumpad.musicmaker.util.IabHelper;
import com.drumpads.drumpad.musicmaker.util.IabResult;
import com.drumpads.drumpad.musicmaker.util.Inventory;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.push.YandexMetricaPush;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    public static IabHelper mHelper;
    public static final String ITEM_SKU = "android.test.purchased";

    private String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhDGthPGKEFOoYdtsYFhWJbd0HNbSPYLutWGm47/8qLls7p7Coj7AXrLxoss8XLrr2UDU8Em6O8KuoH2F4UQEqz+QLCdjwot8bx5guGqvOpsMAp/GlmbCiKWOLBabM/lZx27R7MOzdpDRjrTnJpgbzd/oUp+0e95Ac4dSV9aEPDKwngoLMdCErbn65qKEZQphlDRyaFOPpwUykAfKG7VtyyM1P5YlLb7+yns/Wjd9VAhfspjt7DnwRAfLAcd1q9feywwXj89HhsEL9cfrJsML3IUdDx3bNDq8XG0aBPAi6f+2uLBOv1xqwW/v+5Dx1LkyjZvXFISsKzHUClmohfy0fwIDAQAB";

    @Override
    public void onCreate() {
        super.onCreate();

        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder("5e8dbb2e-6f87-453d-9c0b-0f9cd0289f69").build();
        // Инициализация AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Отслеживание активности пользователей.
        YandexMetrica.enableActivityAutoTracking(this);

        YandexMetricaPush.init(getApplicationContext());

        mHelper = new IabHelper(getApplicationContext(), getResources().getString(R.string.base64EncodedPublicKey));
        mHelper.enableDebugLogging(true, "App");
        mHelper.startSetup(
                new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {
                        try {
                            if (!result.isSuccess()) {
                                Log.d("App", "In-app Billing setup failed: " + result);
                            } else {
                                Log.d("App", "In-app Billing is set up OK");
                            }

                            List<String> st = new ArrayList<String>();
                            st.add(ITEM_SKU);
                            mHelper.queryInventoryAsync(true, st, mGotInventoryListener);
                        } catch (Exception e) {}
                    }
                });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (inventory.hasPurchase(ITEM_SKU)) {
                consumeItem();
            } else {
                unConsumeItem();
            }

            if (mHelper == null) return;

            if (result.isFailure()) {
                return;
            }
        }
    };

    public void consumeItem() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
        editor.putBoolean(getResources().getString(R.string.isBillingSuccess), true);
        editor.apply();
    }

    public void unConsumeItem() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
        editor.putBoolean(getResources().getString(R.string.isBillingSuccess), false);
        editor.apply();
    }
}