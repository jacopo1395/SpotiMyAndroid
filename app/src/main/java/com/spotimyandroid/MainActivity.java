package com.spotimyandroid;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spotimyandroid.http.Api;
import com.spotimyandroid.resources.Album;
import com.spotimyandroid.resources.Artist;
import com.spotimyandroid.resources.Track;
import com.spotimyandroid.utils.ApplicationSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private SearchView searchView;

    private Api server;
    private ScrollView scrollView;
    private LinearLayout tracksView;
    private LinearLayout albumsView;
    private LinearLayout artistsView;
    private ApplicationSupport as;
    private MediaPlayer mediaPlayer;
    private BottomNavigationView bottomNavigationView;
    private ImageView player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        server = new Api(this);


        if (as==null){
            as = (ApplicationSupport) this.getApplication();
            as.prepare();
        }

        mediaPlayer = as. getMP();
        initview();

    }

    @Override
    protected void onResume() {
        super.onResume();
        player();
    }

    private void initview() {
        this.bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav);
        this.scrollView=(ScrollView) findViewById(R.id.results);
        this.searchView=(SearchView) findViewById(R.id.search);
        this.tracksView = (LinearLayout) findViewById(R.id.tracks);
        this.artistsView = (LinearLayout) findViewById(R.id.artistsView);
        this.albumsView = (LinearLayout) findViewById(R.id.albumsView);

        final AsyncTask[] task = new AsyncTask[1];
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                scrollView.setVisibility(View.INVISIBLE);
                if (task[0]!=null) task[0].cancel(true);
                doMySearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                scrollView.setVisibility(View.INVISIBLE);
                if (task[0]!=null) task[0].cancel(true);
                task[0] = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        doMySearch(s);
                        return null;
                    }
                };
                task[0].execute();


                return false;
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.settings) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        this.player = (ImageView) findViewById(R.id.player);
        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                startActivity(intent);
            }
        });


    }

    public void player(){
        final ImageButton pause=(ImageButton) findViewById(R.id.pause);
        if(mediaPlayer.isPlaying()) {
            pause.setImageResource(android.R.drawable.ic_media_pause);
        }
        else{
            pause.setImageResource(android.R.drawable.ic_media_play);
        }
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()) {
                    pause.setImageResource(android.R.drawable.ic_media_play);
                    mediaPlayer.pause();
                }
                else if(as.getLenghtQueue()>0){
                    pause.setImageResource(android.R.drawable.ic_media_pause);
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
    }


    private void doMySearch(final String query) {
        System.out.println("do search");
        server.findTrack(query.replace(" ","%20"), new Api.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray array = result.getJSONObject("tracks").getJSONArray("items");

                    addElemToTracksView(Track.toArray(array));

                    server.findArtist(query, new Api.VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            try {
                                JSONArray array = result.getJSONObject("artists").getJSONArray("items");
                                addElemToArtistsView(Artist.toArray(array));

                                server.findAlbum(query, new Api.VolleyCallback() {
                                    @Override
                                    public void onSuccess(JSONObject result) {
                                        try {
                                            JSONArray array = result.getJSONObject("albums").getJSONArray("items");
                                            addElemToAlbumsView(Album.toArray(array));
                                            scrollView.setVisibility(View.VISIBLE);
                                        } catch (JSONException e) {
                                            System.out.println("errore");
                                            e.printStackTrace();
                                        }

                                    }
                                });

                            } catch (JSONException e) {
                                System.out.println("errore");
                                e.printStackTrace();
                            }

                        }
                    });

                } catch (JSONException e) {
                    System.out.println("errore");
                    e.printStackTrace();
                }

            }
        });





    }

    private void addElemToAlbumsView(final Album[] albums) {
        albumsView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i =0 ;i<albums.length;i++){
            View elem = inflater.inflate(R.layout.item_album, null);
            TextView name = (TextView) elem.findViewById(R.id.name);
            name.setText(albums[i].getName());
            ImageView cover = (ImageView) elem.findViewById(R.id.cover);
//            System.out.println(artists[i].getImage());
            if (albums[i].hasCover()) {
//                new DownloadImageTask(cover).execute(albums[i].getCover());
//                setImage(albums[i].getCover(), cover);
//                cover.setImageBitmap(loadBitmap(albums[i].getCover()));
                Glide.with(this).load(albums[i].getCover()).into(cover);
            }
            final int finalI = i;
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
                    intent.putExtra("album", albums[finalI]);
                    startActivity(intent);
                }
            });
            albumsView.addView(elem);
        }
    }

    private void addElemToArtistsView(final Artist[] artists) {
        artistsView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i =0 ;i<artists.length;i++){
            View elem = inflater.inflate(R.layout.item_artist, null);
            TextView name = (TextView) elem.findViewById(R.id.artist);
            name.setText(artists[i].getName());
            CircleImageView image = (CircleImageView) elem.findViewById(R.id.image);
//            System.out.println(artists[i].getImage());
            if (artists[i].hasImage()) {
//                new DownloadImageTask(image).execute(artists[i].getImage());
//                image.setImageBitmap(loadBitmap(artists[i].getImage()));
                Glide.with(this).load(artists[i].getImage()).into(image);
            }

            final int finalI = i;
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ArtistActivity.class);
                    TextView name = (TextView) view.findViewById(R.id.artist);
                    intent.putExtra("artist", artists[finalI]);
                    startActivity(intent);
                }
            });
            artistsView.addView(elem);
        }
    }

    public void addElemToTracksView(final Track[] tracks){
        tracksView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i =0 ;i<tracks.length;i++){
            View elem = inflater.inflate(R.layout.item_track, null);
            TextView name = (TextView) elem.findViewById(R.id.name);
            name.setText(tracks[i].getName());
            TextView artist = (TextView) elem.findViewById(R.id.artist);
            artist.setText(tracks[i].getArtist());
            TextView album = (TextView) elem.findViewById(R.id.album);
            album.setText(tracks[i].getAlbum());
            final int finalI = i;
//            as.addTrackToQueue(tracks[i]);
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
//                    TextView name = (TextView) view.findViewById(R.id.name);
//                    TextView artist = (TextView) view.findViewById(R.id.artist);
//                    String message = name.getText()+" - "+artist.getText();
//                    intent.putExtra("song", message);
//                    intent.putExtra("track", tracks[finalI]);
                    as.newQueue(tracks);
                    as.setPosition(finalI);
                    startActivity(intent);
                }
            });
            tracksView.addView(elem);
        }

    }




}
