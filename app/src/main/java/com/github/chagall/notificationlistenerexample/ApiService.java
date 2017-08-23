package com.github.chagall.notificationlistenerexample;

import android.service.notification.StatusBarNotification;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by umesh on 10/08/17.
 */

public interface ApiService {
    @POST("users/dump")
    Observable<Response<Void>> dumpData(@Header("Content-Type") String type , @Body Map<String, Object> body);

}
