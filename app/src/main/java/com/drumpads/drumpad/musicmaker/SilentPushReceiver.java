package com.drumpads.drumpad.musicmaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yandex.metrica.push.YandexMetricaPush;

public class SilentPushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Извлечение данных из вашего push-уведомления.
        String payload = intent.getStringExtra(YandexMetricaPush.EXTRA_PAYLOAD);
    }
}
