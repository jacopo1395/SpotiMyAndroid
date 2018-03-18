package com.spotimyandroid.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.spotimyandroid.http.RequestQueue_Singeton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jacopo on 11/03/2018.
 */


public class Api {
    private static String SERVER_IP = "10.0.2.2"; //localhost
//    private static String SERVER_IP = "192.168.1.15"; //pc
//    private static String SERVER_IP = "104.40.208.29";
    public static final String TAG = "API";
    private Context context;
    private RequestQueue queue;
    private int offset;


    public Api(Context context) {
        this.context = context;
        // Get a RequestQueue
        queue = RequestQueue_Singeton.getInstance(context).getRequestQueue();
    }

    private void call(String url, final VolleyCallback callback) {
        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
//                        Log.d("response", response.toString());
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
                try {
                    callback.onSuccess(new JSONObject("{\"status\":\"error\"}"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // Set the tag on the request.
        jsonRequest.setTag(TAG);
        // Add the request to the RequestQueue.
        //queue.add(stringRequest);
        // Add a request (in this example, called stringRequest) to your RequestQueue.
        RequestQueue_Singeton.getInstance(context).addToRequestQueue(jsonRequest);
    }



    public void findTrack(String query, final VolleyCallback callback) {
        // Instantiate the RequestQueue.

        String url = "http://" + SERVER_IP + ":3000/track/" + sostituisci(query);
        call(url, callback);
    }

    public void findArtist(String query, final VolleyCallback callback) {
        // Instantiate the RequestQueue.

        String url = "http://" + SERVER_IP + ":3000/artist/" + sostituisci(query);
        call(url, callback);
    }

    public void findAlbum(String query, final VolleyCallback callback) {
        // Instantiate the RequestQueue.

        String url = "http://" + SERVER_IP + ":3000/album/" + sostituisci(query);
        call(url, callback);
    }

    public void lyric(String artist, String track, final VolleyCallback callback) {
        // Instantiate the RequestQueue.

        String url = "http://" + SERVER_IP + ":3000/lyric/" + sostituisci(artist)+"/"+sostituisci(track);
        call(url, callback);
    }

    public void findTracksOfAlbum(String id, final VolleyCallback callback) {
        // Instantiate the RequestQueue.

        String url = "http://" + SERVER_IP + ":3000/tracks_of_album/" + sostituisci(id);
        call(url, callback);
    }

    public void findAlbumsOfArtist(String id, final VolleyCallback callback) {
        // Instantiate the RequestQueue.

        String url = "http://" + SERVER_IP + ":3000/albums_of_artist/" + sostituisci(id);
        call(url, callback);
    }

    public void findPopularOfArtist(String id, final VolleyCallback callback) {
        // Instantiate the RequestQueue.

        String url = "http://" + SERVER_IP + ":3000/popular_of_artist/" + sostituisci(id);
        call(url, callback);
    }

    public static String getTrackURL(String artist, String track) {
        track = track.split("-")[0];
        return  "http://" + SERVER_IP + ":3000/play/" + sostituisci(artist)+"/"+ sostituisci(track);
    }



    public void play(String artist, String track, final VolleyCallback callback) {
        String url = "http://" + SERVER_IP + ":3000/play/" + sostituisci(artist)+"/"+sostituisci(track);
        call(url, callback);
    }


    public static String sostituisci(String s){
        s=s.replaceAll(" ", "%20");
        s=s.replaceAll("/","%2F");
        return s;
    }

    public void cancel() {
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    public void setOffset(int i) {
        this.offset=i;
    }


    public interface VolleyCallback {
        void onSuccess(JSONObject result);
    }

}