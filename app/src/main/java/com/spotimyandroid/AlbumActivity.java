package com.spotimyandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spotimyandroid.resources.MyAlbum;
import com.spotimyandroid.resources.MyTrack;
import com.spotimyandroid.utils.ApplicationSupport;
import com.spotimyandroid.utils.BottomNavigationViewHelper;
import com.spotimyandroid.utils.StringsValues;

import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.TrackSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Jacopo on 13/03/2018.
 */

public class AlbumActivity extends AppCompatActivity{
    private TextView albumView;
    private TextView artistView;
    private LinearLayout tracksView;
    private List<TrackSimple> tracks;
    private MyAlbum albumInfo;
    private ImageView cover;
    private ApplicationSupport as;
    private MediaPlayer mediaPlayer;
    private BroadcastReceiver mReceiverPlay;
    private ImageView pause;
    private ImageView player;
    public Album album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        as = (ApplicationSupport) this.getApplication();
        Intent intent = getIntent();
        albumInfo = intent.getParcelableExtra("album");
        mediaPlayer = as.getMP();
        as.spotify.getAlbum(albumInfo.getId(), new Callback<Album>() {
            @Override
            public void success(Album album1, Response response) {
                album = album1;
                tracks = album.tracks.items;
                albumInfo.setArtist(album1.artists.get(0).name);

                initview();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        mReceiverPlay = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Do what you need in here
                LinearLayout playerBar = (LinearLayout) findViewById(R.id.playerBar);
                playerBar.setVisibility(View.VISIBLE);
                if(mediaPlayer.isPlaying()) {
                    pause.setImageResource(R.drawable.ic_pause_black_24dp);
                }
                else{
                    pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        player();
        registerReceiver(mReceiverPlay, new IntentFilter(StringsValues.BROADCAST_PLAY));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiverPlay);
        super.onDestroy();
    }


    private void initview() {
        albumView = (TextView) findViewById(R.id.album);
        albumView.setText(albumInfo.getName());
        artistView = (TextView) findViewById(R.id.artist);
        artistView.setText(albumInfo.getArtist());
        tracksView = (LinearLayout) findViewById(R.id.tracks);
        tracksView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i=0; i<tracks.size();i++){
            View elem = inflater.inflate(R.layout.item_album_track, null);
            final TextView track = (TextView) elem.findViewById(R.id.track);
            track.setText(tracks.get(i).name);
            TextView position = (TextView) elem.findViewById(R.id.position);
            position.setText(Integer.toString(i+1));
            final int finalI = i;
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    as.newQueue(MyTrack.toArraySimple(tracks, album));
                    as.setPosition(finalI);

                    intent.putExtra("action","play");
                    startActivity(intent);
                }
            });
            tracksView.addView(elem);

        }

        cover = (ImageView) findViewById(R.id.cover);
        if (albumInfo.hasCover())
            Glide.with(this).load(albumInfo.getCover()).into(cover);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottombar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    return true;
                }
                if (item.getItemId() == R.id.settings) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                if (item.getItemId() == R.id.profile) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        player();

    }

    public void player(){
        LinearLayout playerBar = (LinearLayout) findViewById(R.id.playerBar);
        if(as.state== StringsValues.PLAY) playerBar.setVisibility(View.VISIBLE);
        else playerBar.setVisibility(View.INVISIBLE);
        pause=(ImageButton) findViewById(R.id.pause);
        if(mediaPlayer!=null && mediaPlayer.isPlaying()) {
            pause.setImageResource(R.drawable.ic_pause_black_24dp);
        }
        else{
            pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()) {
                    pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mediaPlayer.pause();
                }
                else if(as.getLenghtQueue()>0){
                    pause.setImageResource(R.drawable.ic_pause_black_24dp);
                    mediaPlayer.start();
                }
            }
        });

        ImageView next = (ImageView) findViewById(R.id.next);
        ImageView previous = (ImageView) findViewById(R.id.previous);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                as.nextTrack();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                as.previousTrack();
            }
        });

        this.player = (ImageView) findViewById(R.id.player);
        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("action","openonly");
                startActivity(intent);
            }
        });
    }



}
