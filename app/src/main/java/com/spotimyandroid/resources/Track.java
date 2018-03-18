package com.spotimyandroid.resources;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jacopo on 11/03/2018.
 */

public class Track implements Parcelable {
    private String name;
    private String artist;
    private String album;
    private String date;
    private String cover;
    private String id;



    private String lyric;
    private int duration;



    public Track(JSONObject o) {
        try {
            this.name = o.getString("name");
            this.artist =  o.getJSONArray("artists").getJSONObject(0).getString("name");
            this.id =  o.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
            this.name = "name";
            this.artist = "artists";
            this.album = "album";
            this.date = "date";
            this.duration = 0;
            this.id = "";
        }
        try {
            this.cover =  o.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
        } catch (JSONException e) {
            this.cover="cover";
        }
        try {
            this.album =  o.getJSONObject("album").getString("name");
        } catch (JSONException e) {
            this.album="album";
        }


    }



    public static Track[] toArray(JSONArray array){
        Track[] r = new Track[array.length()];
        for (int i =0 ; i< array.length();i++){
            try {
                Track elem= new Track(array.getJSONObject(i));
                r[i]=elem;
            } catch (JSONException e) {
                e.printStackTrace();
                return new Track[0];
            }
        }
        return r;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artists) {
        this.artist = artists;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getId() {
        return id;
    }

    public boolean hasCover(){
        if (cover.equals("cover")) return false;
        else return true;
    }

    @Override
    public String toString() {
        return "Track{" +
                "name='" + name + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", date='" + date + '\'' +
                ", cover='" + cover + '\'' +
                ", lyric='" + lyric + '\'' +
                ", duration=" + duration +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        Track t = (Track) obj;
        return t.getId().equals(this.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(artist);
        parcel.writeString(album);
        parcel.writeString(cover);
        parcel.writeString(id);

    }


    protected Track(Parcel in) {
        name = in.readString();
        artist = in.readString();
        album = in.readString();
        cover = in.readString();
        id = in.readString();

    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };



}
