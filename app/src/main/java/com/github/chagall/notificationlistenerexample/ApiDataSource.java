package com.github.chagall.notificationlistenerexample;

import android.service.notification.StatusBarNotification;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;

/**
 * Created by umesh on 13/08/17.
 */

public class ApiDataSource {

    public static Observable<Response<Void>> dumpData(Map<String, Object> body) {
        return ApiClient.getService().dumpData("application/json", body);
    }
}