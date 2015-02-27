package com.bender.linguatestapp;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class NetworkManager {

    private static NetworkManager manager = new NetworkManager();
    private AsyncHttpClient client = new AsyncHttpClient();

    public static NetworkManager getInstance() {
        return manager;
    }

    private static final String API_URL = "https://translate.yandex.net/api/v1.5/tr.json/";
    private static final String API_KEY = "trnsl.1.1.20150224T181650Z.2a2c4adcf7fbe1d5.de27469a7f75abb1674083e689338c400c899386";
    private static final String FUNC_TRANSLATE = "translate";
    private static final String PARAM_KEY = "key=";
    private static final String PARAM_TEXT = "text=";
    private static final String PARAM_LANG = "lang=";

    public void getTranslation(String query, JsonHttpResponseHandler handler) {
        query = API_URL + FUNC_TRANSLATE + "?" + PARAM_KEY + API_KEY +"&"+ PARAM_TEXT + query + "&" + PARAM_LANG + "en-ru";
        Log.d("NetworkManager", "Query: "+query);
        client.get(query, handler);
    }
}
