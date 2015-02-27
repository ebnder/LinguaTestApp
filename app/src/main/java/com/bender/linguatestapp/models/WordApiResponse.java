package com.bender.linguatestapp.models;

import org.json.JSONArray;
import org.json.JSONObject;

public class WordApiResponse {

    private String lang;
    private String[] text;
    private int code;

    public WordApiResponse(JSONObject json) {
        lang = json.optString("lang", "");
        code = json.optInt("code", -1);
        JSONArray array = json.optJSONArray("text");
        text = new String[array.length()];
        for (int i=0; i<array.length(); i++) {
            text[i] = array.optString(i);
        }
    }

    public String getTranslation() {
        return text[0];
    }

    public int getResponseCode() {
        return code;
    }
}
