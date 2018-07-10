package io.sece.vlc.rcvr;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class TransmitterAPI {
    private String url;
    private Context context;
    private Object tag;


    public TransmitterAPI(Context context, String url) {
        this.context = context;
        this.url = url;
        if (!this.url.endsWith("/")) this.url += "/";
    }


    public CompletableFuture<JSONObject> jsonRequest(int method, String endpoint, JSONObject data) {
        final CompletableFuture<JSONObject> rv = new CompletableFuture<>();
        JsonObjectRequest req = new JsonObjectRequest(method, url + endpoint, data,
                rv::complete, rv::completeExceptionally);
        req.setTag(this);
        APIClient.getInstance(context).addToRequestQueue(req);
        return rv;
    }


    public void cancelPending() {
        APIClient.getInstance(context).cancelAll(this);

    }


    public CompletableFuture<JSONObject> calibrate(int hue, int duration) {
        return calibrate(Arrays.asList(hue), duration);
    }


    public CompletableFuture<JSONObject> transmit(int fps, int duration, String modulator) {
        JSONObject data = new JSONObject();
        try {
            data.put("FPS", fps);
            data.put("timeout", duration);
            data.put("modulator", modulator);
        } catch (JSONException e) {
            CompletableFuture<JSONObject> rv = new CompletableFuture<>();
            rv.completeExceptionally(e);
            return rv;
        }
        return jsonRequest(Request.Method.POST, "transmit", data);
    }


    public CompletableFuture<JSONObject> calibrate(List<Integer> hues, int duration) {
        JSONObject data = new JSONObject();
        try {
            data.put("duration", duration);
            data.put("hueValue", hues);
        } catch (JSONException e) {
            CompletableFuture<JSONObject> rv = new CompletableFuture<>();
            rv.completeExceptionally(e);
            return rv;
        }
        return jsonRequest(Request.Method.POST, "calibration", data);
    }
}
