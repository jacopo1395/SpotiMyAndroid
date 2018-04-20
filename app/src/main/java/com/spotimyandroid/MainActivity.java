package com.spotimyandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
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
import com.spotimyandroid.resources.MyAlbum;
import com.spotimyandroid.resources.MyArtist;
import com.spotimyandroid.resources.MyTrack;
import com.spotimyandroid.utils.ApplicationSupport;
import com.spotimyandroid.utils.BottomNavigationViewHelper;
import com.spotimyandroid.utils.StringsValues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    private SearchView searchView;

    private ScrollView scrollView;
    private LinearLayout tracksView;
    private LinearLayout albumsView;
    private LinearLayout artistsView;
    private ApplicationSupport app;
    private MediaPlayer mediaPlayer;
    private ImageView player;
    private BroadcastReceiver mReceiver;
    private ImageButton pause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        app = (ApplicationSupport) this.getApplication();

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(app.getToken());
        app.spotify = api.getService();

        mediaPlayer = app.getMP();
        initview();

        mReceiver = new BroadcastReceiver() {
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
        registerReceiver(mReceiver, new IntentFilter(StringsValues.BROADCAST_PLAY));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initview() {
        this.scrollView=(ScrollView) findViewById(R.id.results);
        this.searchView=(SearchView) findViewById(R.id.search);
        this.tracksView = (LinearLayout) findViewById(R.id.tracks);
        this.artistsView = (LinearLayout) findViewById(R.id.artistsView);
        this.albumsView = (LinearLayout) findViewById(R.id.albumsView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(s.equals(""))recent();
                else doMySearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                if(s.equals(""))recent();
                else doMySearch(s);


                return false;
            }
        });

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

        recent();
        player();

    }


    public void recent(){
        TextView textTrack = (TextView) findViewById(R.id.textTracks);
        TextView textAlbums = (TextView) findViewById(R.id.textAlbums);
        TextView textArtists = (TextView) findViewById(R.id.textArtists);
        textTrack.setText(R.string.recent_tracks);
        textAlbums.setText(R.string.recent_albums);
        textArtists.setText(R.string.recent_artists);

        SharedPreferences sharedPref = getSharedPreferences( "recent", Context.MODE_PRIVATE );
        String s = sharedPref.getString("tracks", "");
        System.out.println(s);
        app.addRecentTracks(MyTrack.toArray(s));
        addElemToTracksView(MyTrack.toArray(s));

        String a = sharedPref.getString("albums", "");
        app.addRecentAlbums(MyAlbum.toArray(a));
        addElemToAlbumsView(MyAlbum.toArray(a));

        String ar = sharedPref.getString("artists", "");
        app.addRecentArtists(MyArtist.toArray(ar));
        addElemToArtistsView(MyArtist.toArray(ar));

    }


    public void player(){
        LinearLayout playerBar = (LinearLayout) findViewById(R.id.playerBar);
        if(app.state==StringsValues.PLAY) playerBar.setVisibility(View.VISIBLE);
        else playerBar.setVisibility(View.INVISIBLE);
        pause=(ImageButton) findViewById(R.id.pause);
        if(mediaPlayer.isPlaying()) {
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
                else if(app.getLenghtQueue()>0){
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
             app.nextTrack();
         }
        });
        previous.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 app.previousTrack();
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


    private void doMySearch(final String query) {
        System.out.println("do search");
        TextView textTrack = (TextView) findViewById(R.id.textTracks);
        TextView textAlbums = (TextView) findViewById(R.id.textAlbums);
        TextView textArtists = (TextView) findViewById(R.id.textArtists);
        textTrack.setText(R.string.tracks);
        textAlbums.setText(R.string.albums);
        textArtists.setText(R.string.artists);


        final Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.LIMIT, 5);
        app.spotify.searchTracks(query, options, new SpotifyCallback<TracksPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {

            }

            @Override
            public void success(TracksPager tracksPager, Response response) {
                addElemToTracksView(MyTrack.toArray(tracksPager.tracks.items));
                app.spotify.searchArtists(query, options, new SpotifyCallback<ArtistsPager>() {
                    @Override
                    public void failure(SpotifyError spotifyError) {
                        
                    }

                    @Override
                    public void success(ArtistsPager artistsPager, Response response) {
                        addElemToArtistsView(MyArtist.toArray(artistsPager.artists.items));
                        app.spotify.searchAlbums(query, options, new SpotifyCallback<AlbumsPager>() {
                            @Override
                            public void failure(SpotifyError spotifyError) {
                                
                            }

                            @Override
                            public void success(AlbumsPager albumsPager, Response response) {
                                addElemToAlbumsView(MyAlbum.toArraySimple(albumsPager.albums.items));
                            }
                        });
                    }
                });
            }
        });
        
    }


    private void addElemToTracksView(final List<MyTrack> tracks) {
        tracksView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i =0 ;i<tracks.size();i++){
            View elem = inflater.inflate(R.layout.item_track, null);
            TextView name = (TextView) elem.findViewById(R.id.name);
            name.setText(tracks.get(i).getName());
            TextView artist = (TextView) elem.findViewById(R.id.artist);
            artist.setText(tracks.get(i).getArtist());
            TextView album = (TextView) elem.findViewById(R.id.album);
            album.setText(tracks.get(i).getAlbum());
            final int finalI = i;
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    if(!tracks.get(finalI).equals(app.getCurrentTrack())) {
                        app.newQueue(tracks);
                    }
                    app.setPosition(finalI);
                    intent.putExtra("action","play");
                    startActivity(intent);
                }
            });
            tracksView.addView(elem);
        }
    }

    private void addElemToArtistsView(final List<MyArtist> artists) {
        artistsView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i =0; i<artists.size(); i++){
            View elem = inflater.inflate(R.layout.item_artist, null);
            TextView name = (TextView) elem.findViewById(R.id.artist);
            name.setText(artists.get(i).getName());
            CircleImageView image = (CircleImageView) elem.findViewById(R.id.image);
            if (artists.get(i).hasImage()) {
                Glide.with(this).load(artists.get(i).getImage()).into(image);
            }
            final int finalI = i;
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ArtistActivity.class);
                    intent.putExtra("artist", artists.get(finalI));
                    startActivity(intent);
                }
            });
            artistsView.addView(elem);
        }
    }

    private void addElemToAlbumsView(final List<MyAlbum> albums) {
        albumsView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i =0; i<albums.size(); i++){
            View elem = inflater.inflate(R.layout.item_album, null);
            TextView name = (TextView) elem.findViewById(R.id.name);
            name.setText(albums.get(i).getName());
            ImageView cover = (ImageView) elem.findViewById(R.id.cover);
            if (albums.get(i).hasCover()) {
                Glide.with(this).load(albums.get(i).getCover()).into(cover);
            }
            final int finalI = i;
            elem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
                    intent.putExtra("album", albums.get(finalI));
                    startActivity(intent);
                }
            });
            albumsView.addView(elem);
        }
    }




}
