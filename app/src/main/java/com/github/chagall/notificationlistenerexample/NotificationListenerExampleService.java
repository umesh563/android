package com.github.chagall.notificationlistenerexample;

import android.app.Notification;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NotificationListenerExampleService extends NotificationListenerService implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */

    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int OTHER_NOTIFICATIONS_CODE = 4; // We ignore all notification with code == 4
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
       // if(!sbn.getPackageName().contains("whatsapp")) {
            String androidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> user = new HashMap<String, Object>();
            user.put("device_id", androidDeviceId);

            data.put("user", user);

            Map<String, Object> notification_1 = new HashMap<String, Object>();
            notification_1.put("id", sbn.getId());
            notification_1.put("key", sbn.getKey());
            notification_1.put("package_name", sbn.getPackageName());
            notification_1.put("group_key", sbn.getGroupKey());
            notification_1.put("post_time", sbn.getPostTime());
            notification_1.put("tag", sbn.getTag());

            Notification notification = sbn.getNotification();
            if (notification != null) {
                Bundle dataBundle = notification.extras;

                Map<String, Object> notifMap = new HashMap<>();
                Set<String> keys = dataBundle.keySet();
                for (String key : keys) {
                    try {
                        notifMap.put(key, dataBundle.get(key).toString());
                    } catch (Exception e) {
                        continue;
                    }
                }
                notification_1.put("notification", notifMap);
            }
            data.put("data", notification_1);

            ApiDataSource.dumpData(data)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Void -> {
                        Log.e("UMESH SUCCESS", data.toString());
                    }, Throwable -> {

                        Log.e("UMESH", "ERROR");
                        Throwable.printStackTrace();
                    }, () -> {
                    });
       // }

    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

            StatusBarNotification[] activeNotifications = this.getActiveNotifications();

            if(activeNotifications != null && activeNotifications.length > 0) {
                for (int i = 0; i < activeNotifications.length; i++) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
                        intent.putExtra("Notification Code", notificationCode);
                        sendBroadcast(intent);
                        break;
                    }
                }
            }
        }
    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
            return(InterceptedNotificationCode.FACEBOOK_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
            return(InterceptedNotificationCode.INSTAGRAM_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
            return(InterceptedNotificationCode.WHATSAPP_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }

}

