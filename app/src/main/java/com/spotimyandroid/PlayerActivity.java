package com.spotimyandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spotimyandroid.http.Api;
import com.spotimyandroid.resources.Track;
import com.spotimyandroid.utils.ApplicationSupport;
import com.spotimyandroid.utils.StringsValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Jacopo on 11/03/2018.
 */

public class PlayerActivity extends AppCompatActivity{

    private SeekBar seekBar;
    private ImageButton previous;
    private ImageButton pause;
    private ImageButton next;
    private TextView track;
    private TextView album;
    private TextView artist;
    private TextView lyric;
    private ImageView cover;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Track trackInfo;
    private Api server;
    private ApplicationSupport as;
    private AsyncTask downloadSong;
    private BroadcastReceiver mReceiverPlay;
    private BroadcastReceiver mReceiverNext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        as = ((ApplicationSupport) this.getApplication());
        mediaPlayer = as.getMP();
        server = new Api(this);
        trackInfo = as.getCurrentTrack();

        initiview();

        Intent i = getIntent();
        String info = i.getStringExtra("info");
        if(!info.equals("openonly")) {
            as.play();
//            downloadSong = new AsyncTask() {
//                @Override
//                protected Object doInBackground(Object[] objects) {
//                    as.play();
//                    return null;
//                }
//            };
//
//            downloadSong.execute();
        }


        mReceiverPlay = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Do what you need in here
                trackInfo = as.getCurrentTrack();
                initiview();
                primaryProgressBarUpdater();
                enableSeek();
            }
        };

        mReceiverNext = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Do what you need in here
                trackInfo = as.getCurrentTrack();
                initiview();
                primaryProgressBarUpdater();
                enableSeek();

            }
        };


    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiverPlay, new IntentFilter(StringsValues.BROADCAST_PLAY));
        registerReceiver(mReceiverNext, new IntentFilter(StringsValues.BROADCAST_NEXT));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiverPlay);
        unregisterReceiver(mReceiverNext);
        super.onDestroy();
    }

    private void enableSeek(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int duration = mediaPlayer.getDuration();
                mediaPlayer.seekTo(duration * seekBar.getProgress() / 100);



            }
        });


    }

    private void initiview() {
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(99); // It means 100% .0-99
        seekBar.setProgress(0);


        pause=(ImageButton) findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(as.state==StringsValues.PLAY){
                    if (mediaPlayer.isPlaying()) {
                        pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        mediaPlayer.pause();
                    } else {
                        pause.setImageResource(R.drawable.ic_pause_black_24dp);
                        mediaPlayer.start();
                        primaryProgressBarUpdater();
                    }
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

        track = (TextView) findViewById(R.id.track);
        album = (TextView) findViewById(R.id.album);
        artist = (TextView) findViewById(R.id.artist);
        lyric = (TextView) findViewById(R.id.lyric);
        lyric.setMovementMethod(new ScrollingMovementMethod());

        track.setText(trackInfo.getName());
        album.setText(trackInfo.getAlbum());
        artist.setText(trackInfo.getArtist());
        server.lyric(trackInfo.getArtist(), trackInfo.getName(), new Api.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    lyric.setText(parseLyric(result.getString("lyric")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        cover=(ImageView) findViewById(R.id.cover);
        if (trackInfo.hasCover()){
            Glide.with(this).load(trackInfo.getCover()).into(cover);

        }




    }


    private void primaryProgressBarUpdater() {
        if (mediaPlayer==null) return;
        if(as.state!=StringsValues.PLAY) return;
        int x=(int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100);


        seekBar.setProgress(x); // This math construction give a percentage of "was playing"/"song length"
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primaryProgressBarUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        }
    }


    private String parseLyric(String s){
       s=s.replaceAll("&#xE8;","è");
       s=s.replaceAll("&#xE9;","é");
       s=s.replaceAll("&#xF2;","ò");
       s=s.replaceAll("&#xE0;","à");
       s=s.replaceAll("&#xEC;","ì");
       s=s.replaceAll("&#xF9;","ù");
       s=s.replaceAll("&#x2019;","'");
       return s;
    }





}
