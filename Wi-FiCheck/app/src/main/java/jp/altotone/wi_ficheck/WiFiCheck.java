package jp.altotone.wi_ficheck;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WiFiCheckConfigureActivity WiFiCheckConfigureActivity}
 */
public class WiFiCheck extends AppWidgetProvider {

    private boolean isRunningService(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServiceInfo = activityManager.getRunningServices(Integer.MAX_VALUE);
        boolean result = false;
        for (ActivityManager.RunningServiceInfo curr : listServiceInfo) {
            // クラス名を比較
            if (curr.service.getClassName().equals(WidgetService.class.getName())) {
                // 実行中のサービスと一致
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d("life", "onUpdate");

        if (!isRunningService(context)) {
            Intent intent = new Intent(context, WidgetService.class);
            context.startService(intent);
        }

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d("life", "onDeleted");
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WiFiCheckConfigureActivity.deleteIdPref(context, appWidgetId);
        }

        //idが1つも登録されていない場合は、service停止
        Map<String, ?> ids = WiFiCheckConfigureActivity.loadIdsPref(context);
        if (ids == null || ids.size() == 0) {
            if (isRunningService(context)) {
                Intent intent = new Intent(context, WidgetService.class);
                context.stopService(intent);
            }
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

        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        StringBuilder sb = new StringBuilder();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            //Wi-Fi情報をセット
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo w_info = wifiManager.getConnectionInfo();

            sb.append("Wi-Fi : ");
            sb.append(w_info.getSSID());
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE){
            sb.append("Mobile");
        } else {
            sb.append("Unknown");
        }

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

            Map<String, ?> appWidgetIds = WiFiCheckConfigureActivity.loadIdsPref(context);

            if (appWidgetIds != null) {
                Set entries = appWidgetIds.entrySet();

                for (Object entry1 : entries) {
                    Map.Entry entry = (Map.Entry) entry1;
                    try {
                        int id = Integer.parseInt(entry.getValue().toString());
                        updateAppWidget(context, AppWidgetManager.getInstance(context), id);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
}

