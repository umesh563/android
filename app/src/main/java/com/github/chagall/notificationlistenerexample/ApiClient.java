package com.github.chagall.notificationlistenerexample;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by umesh on 10/08/17.
 */

public class ApiClient {

    private static final String BASE_URL = "http://139.59.20.112/";
    private static Retrofit retrofit = null;

    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static Gson gson = new GsonBuilder()
            .create();
//
//    private static OkHttpClient getHttpClient(int connTimeout, int readTimeout, int writeTimeout) {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.connectTimeout(connTimeout, TimeUnit.SECONDS)    // Timeout to create a connection
//                .writeTimeout(writeTimeout, TimeUnit.SECONDS)     // Write timeout
//                .readTimeout(readTimeout, TimeUnit.SECONDS); // Timeout that the server supports
//
//        Request.Builder builder1 = n
//
//        return builder.build();
//    }

    private static Retrofit getClient() {
        if ( retrofit == null ) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }

    public static ApiService getService() {
        Retrofit client = getClient();
        return client.create(ApiService.class);
    }
}