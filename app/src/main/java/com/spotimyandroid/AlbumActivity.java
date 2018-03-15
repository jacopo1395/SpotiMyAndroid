package com.spotimyandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spotimyandroid.http.Api;
import com.spotimyandroid.resources.Album;
import com.spotimyandroid.resources.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jacopo on 13/03/2018.
 */

public class AlbumActivity extends AppCompatActivity{
    private Api server;
    private TextView albumView;
    private TextView artistView;
    private LinearLayout tracksView;

    private Album albumInfo;
    private Track[] tracks;
    private ImageView cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();
        albumInfo = intent.getParcelableExtra("album");
        System.out.println(albumInfo);
        server = new Api(this);
        server.findTracksOfAlbum(albumInfo.getId(), new Api.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray array = result.getJSONArray("items");
                    tracks = Track.toArray(array);

                    initview();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });





    }

    private void initview() {
        albumView = (TextView) findViewById(R.id.album);
        albumView.setText(albumInfo.getName());
        artistView = (TextView) findViewById(R.id.artist);
        artistView.setText(albumInfo.getArtist());
        tracksView = (LinearLayout) findViewById(R.id.tracks);
        tracksView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i=0; i<tracks.length;i++){
            tracks[i].setAlbum(albumInfo.getCover());
            View elem = inflater.inflate(R.layout.item_album_track, null);
            final TextView track = (TextView) elem.findViewById(R.id.track);
            track.setText(tracks[i].getName());
            TextView position = (TextView) elem.findViewById(R.id.position);
            position.setText(Integer.toString(i+1));

            final int finalI = i;
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    intent.putExtra("track", tracks[finalI]);
                    startActivity(intent);
                }
            });
            tracksView.addView(elem);

        }

        cover = (ImageView) findViewById(R.id.cover);
        System.out.println(albumInfo);
        if (albumInfo.hasCover())
//            new DownloadImageTask(cover).execute(albumInfo.getCover());
            Glide.with(this).load(albumInfo.getCover()).into(cover);

    }




}
