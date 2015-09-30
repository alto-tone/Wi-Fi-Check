package jp.altotone.wi_ficheck;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WiFiCheckConfigureActivity WiFiCheckConfigureActivity}
 */
public class WiFiCheck extends AppWidgetProvider {

    private static int[] mAppWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d("life", "onUpdate");

        mAppWidgetIds = appWidgetIds;

//        final int N = appWidgetIds.length;
//        if (N == 1) {
//            //初めての追加のためReceiverを登録
//            IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//            context.getApplicationContext().registerReceiver(getIntentReceiver(), filter);
//        }

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

//        if (N == 1) {
//            //最後のWidgetが削除されたのでReceiverを解放
//            context.getApplicationContext().unregisterReceiver(getIntentReceiver());
//        }
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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d("life","updateAppWidget");

        CharSequence widgetText = WiFiCheckConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_check);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        //Wi-Fi情報をセット
        StringBuilder sb = new StringBuilder();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo w_info = wifiManager.getConnectionInfo();

//        Log.i("Sample", "SSID:" + w_info.getSSID());
        sb.append("SSID : ");
        sb.append(w_info.getSSID());

//        Log.i("Sample", "BSSID:" + w_info.getBSSID());
//        sb.append("\nBSSID:");
//        sb.append(w_info.getBSSID());
//
//        Log.i("Sample", "IP Address:" + w_info.getIpAddress());
//        int ip_addr_i = w_info.getIpAddress();
//        String ip_addr = ((ip_addr_i) & 0xFF) + "." + ((ip_addr_i >> 8) & 0xFF) + "." + ((ip_addr_i >> 16) & 0xFF) + "." + ((ip_addr_i >> 24) & 0xFF);
//        sb.append("\nIP Address:");
//        sb.append(ip_addr);
//
//        Log.i("Sample", "Mac Address:" + w_info.getMacAddress());
//        sb.append("\nMac Address:");
//        sb.append(w_info.getMacAddress());
//
//        Log.i("Sample", "Network ID:" + w_info.getNetworkId());
//        sb.append("\nNetwork ID:");
//        sb.append(w_info.getNetworkId());
//
//        Log.i("Sample", "Link Speed:" + w_info.getLinkSpeed());
//        sb.append("\nink Speed :");
//        sb.append(w_info.getLinkSpeed());

        views.setTextViewText(R.id.appwidget_text, sb.toString());

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


//    public static BroadcastReceiver getIntentReceiver() {
//        return mIntentReceiver;
//    }
//
//    private final static BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d("BroadcastReceiver", "ネットワーク変更！");
//
//            if (mAppWidgetIds != null) {
//                for (int appWidgetId : mAppWidgetIds) {
//                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
//                }
//            }
//        }
//    };
}

