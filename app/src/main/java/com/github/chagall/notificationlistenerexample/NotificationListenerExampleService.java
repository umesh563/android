package com.github.chagall.notificationlistenerexample;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import io.reactivex.schedulers.Schedulers;

public class NotificationListenerExampleService extends NotificationListenerService implements LocationListener {

    private static Executor executor = new SerialExecutor("notification-upload");

    private static final String TAG = "NotificationService";

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

    private static final String validPackages[] = {
            "com.application.zomato",
            "com.application.zomato.ordering",
            "com.flipkart.android",
            "in.amazon.mShop.android.shopping",
            "com.shopping.limeroad",
            "in.swiggy.android",
            "com.freshmenu",
            "com.done.faasos",
            "com.snapdeal.main",
            "com.myntra.android",
            "com.ebay.mobile",
            "net.one97.paytm",
            "com.bigbasket.mobileapp",
            "com.grofers.customerapp",
            "com.goibibo",
            "com.makemytrip",
            "in.redbus.android",
            "com.oyo.consumer",
            "com.myntra.android",
            "com.india.foodpanda.android",
            "com.github.chagall.notificationlistenerexample"
    };

    List<String> validPackageList = Arrays.asList(validPackages);

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


    private List<Bitmap> retrieveBitmaps(StatusBarNotification sbn) {
        if ( sbn == null ) {
            return null;
        }

        Notification notification = sbn.getNotification();
        if ( notification.bigContentView == null ) {
            return null;
        }

        List<?> actions = Utility.getFieldValue(notification.bigContentView, "mActions");
        if ( actions == null ) {
            Log.e(TAG, "Actions is null");
            return null;
        }

        Log.i(TAG, "Actions list size = " + actions.size());

        List<Bitmap> images = new ArrayList<>();

        for ( Object action : actions ) {
            if ( action == null ) {
                continue;
            }

            if ( Utility.getClassFromName("android.widget.RemoteViews$BitmapReflectionAction")
                    .isInstance(action) ) {
                Bitmap bitmap = Utility.getFieldValue(action, "bitmap");
                images.add(bitmap);
            }
        }

        return images;
    }

    private Bitmap getNotificationRender(StatusBarNotification sbn) {
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setBackgroundColor(Color.BLUE);

        if ( sbn.getNotification().bigContentView != null ) {
            View notificationView = sbn.getNotification().bigContentView
                    .apply(getApplicationContext(), ll);
            notificationView.setBackgroundColor(Color.WHITE);
            ll.addView(notificationView);
        } else if ( sbn.getNotification().contentView != null ) {
            View notificationView = sbn.getNotification().contentView
                    .apply(getApplicationContext(), ll);
            notificationView.setBackgroundColor(Color.WHITE);
            ll.addView(notificationView);
        } else {
            Log.e(TAG, "Unable to draw - " + sbn);
        }

        return Utility.getBitmapFromView(ll, getApplicationContext());
    }

    private static String normalize(String name) {
        name = name.replaceAll("\\|", "_");
        return name;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String groupKey = sbn.getGroupKey();
        String key = sbn.getKey();

        Log.e(TAG, "Group - " + groupKey);
        Log.e(TAG, "Key - " + key);

        List<Bitmap> images = retrieveBitmaps(sbn);
        Bitmap render = getNotificationRender(sbn);

        String imageFile = null;
        String renderFile = null;

        String imageFileData = null;
        String renderFileData = null;

        if ( render != null ) {
            renderFile = Utility.saveBitmapToFile(getApplicationContext(), render,
                    normalize(key) + "_render");
            renderFileData = Utility.convertToBase64(renderFile);
        }

        if ( images != null && images.size() > 0 && images.get(0) != null ) {
            imageFile = Utility.saveBitmapToFile(getApplicationContext(), images.get(0),
                    normalize(key) + "_image");
            imageFileData = Utility.convertToBase64(imageFile);
        }

        Log.e(TAG, "ImageFile = " + imageFile);
        Log.e(TAG, "RenderFile = " + renderFile);

        if ( validPackageList.contains(sbn.getPackageName()) ) {
            String androidDeviceId = Utility.getAndroidDeviceId(getApplicationContext());
            Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> user = new HashMap<String, Object>();
            user.put("device_id", androidDeviceId);

            data.put("user", user);
            data.put("imageFile", imageFileData);
            data.put("renderFile", renderFileData);

            Map<String, Object> notification_1 = new HashMap<String, Object>();
            notification_1.put("id", sbn.getId());
            notification_1.put("key", sbn.getKey());
            notification_1.put("package_name", sbn.getPackageName());
            notification_1.put("group_key", sbn.getGroupKey());
            notification_1.put("post_time", sbn.getPostTime());
            notification_1.put("tag", sbn.getTag());

            Notification notification = sbn.getNotification();
            if ( notification != null ) {
                Bundle dataBundle = notification.extras;

                Map<String, Object> notifMap = new HashMap<>();
                Set<String> keys = dataBundle.keySet();
                for ( String k : keys ) {
                    try {
                        notifMap.put(k, dataBundle.get(k).toString());
                    } catch ( Exception e ) {
                        continue;
                    }
                }
                notification_1.put("notification", notifMap);
            }
            data.put("data", notification_1);

            ApiDataSource.dumpData(data)
                    .subscribeOn(Schedulers.from(executor))
                    .subscribe(Void -> {
                        Log.e("UMESH SUCCESS", data.toString());
                    }, Throwable -> {
                        Log.e("UMESH", "ERROR");
                        Throwable.printStackTrace();
                    }, () -> {
                    });
        }

    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        int notificationCode = matchNotificationCode(sbn);

        if ( notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE ) {

            StatusBarNotification[] activeNotifications = this.getActiveNotifications();

            if ( activeNotifications != null && activeNotifications.length > 0 ) {
                for ( int i = 0; i < activeNotifications.length; i++ ) {
                    if ( notificationCode == matchNotificationCode(activeNotifications[i]) ) {
                        Intent intent = new Intent("com.github.chagall.notificationlistenerexample");
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

        if ( packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME) ) {
            return (InterceptedNotificationCode.FACEBOOK_CODE);
        } else if ( packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME) ) {
            return (InterceptedNotificationCode.INSTAGRAM_CODE);
        } else if ( packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME) ) {
            return (InterceptedNotificationCode.WHATSAPP_CODE);
        } else {
            return (InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }

}

