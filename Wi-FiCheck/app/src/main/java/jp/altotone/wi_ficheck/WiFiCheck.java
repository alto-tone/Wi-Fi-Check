package jp.altotone.wi_ficheck;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WiFiCheckConfigureActivity WiFiCheckConfigureActivity}
 */
public class WiFiCheck extends AppWidgetProvider {

    private static int[] mAppWidgetIds;

    private Intent mServiceIntent;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d("life", "onUpdate");

        mServiceIntent = new Intent(context, WidgetService.class);
        context.startService(mServiceIntent);

        mAppWidgetIds = appWidgetIds;

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d("life", "onDeleted");
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int appWidgetId : appWidgetIds) {
            WiFiCheckConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d("life", "onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.d("life","onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d("life","updateAppWidget");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_check);

        //Wi-Fi情報をセット
        StringBuilder sb = new StringBuilder();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo w_info = wifiManager.getConnectionInfo();

        sb.append("SSID : ");
        sb.append(w_info.getSSID());

        views.setTextViewText(R.id.appwidget_text, sb.toString());

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static class WidgetService extends Service {
        @Override
        public IBinder onBind(Intent in) {
            Log.d("life","onBind");
            return null;
        }

        @Override
        public boolean onUnbind(Intent intent) {
            Log.d("life","onUnbind");
            return super.onUnbind(intent);
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            super.onTaskRemoved(rootIntent);
            Log.d("life", "onTaskRemoved");
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.d("life", "onStartCommand");

            IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(getIntentReceiver(), filter);

            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onDestroy() {
            Log.d("life", "onDestroy");

            super.onDestroy();
            unregisterReceiver(getIntentReceiver());
        }
    }


    public static BroadcastReceiver getIntentReceiver() {
        return mIntentReceiver;
    }

    private final static BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "ネットワーク変更！");

            if (mAppWidgetIds != null) {
                for (int appWidgetId : mAppWidgetIds) {
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
                }
            }
        }
    };
}

