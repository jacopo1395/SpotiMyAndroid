package com.spotimyandroid.resources;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jacopo on 13/03/2018.
 */

public class Album implements Parcelable {

    private String name;


    private String artist;
    private String cover;
    private String id;



    public Album(JSONObject o) {
        try {
            this.name = o.getString("name");
            this.id = o.getString("id");
            this.artist = o.getJSONArray("artists").getJSONObject(0).getString("name");

        } catch (JSONException e) {
            e.printStackTrace();
            this.name = "name";

        }

        try {
            JSONArray a = o.getJSONArray("images");
            this.cover=a.getJSONObject(1).getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
            this.cover="cover";

        }


    }

    public static Album[] toArray(JSONArray array) {
        Album[] a = new Album[array.length()];
        for (int i =0 ; i< array.length();i++){
            try {
                Album elem= new Album(array.getJSONObject(i));
                a[i]=elem;
            } catch (JSONException e) {
                e.printStackTrace();
                return new Album[0];
            }
        }
        return a;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getArtist() {
        return artist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public boolean hasCover() {
        if (cover.equals("cover")) return false;
        else return true;
    }

    @Override
    public String toString() {
        return "Album{" +
                "name='" + name + '\'' +
                ", artist='" + artist + '\'' +
                ", cover='" + cover + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(cover);
        parcel.writeString(id);
        parcel.writeString(artist);

    }


    protected Album(Parcel in) {
        name = in.readString();
        cover = in.readString();
        id = in.readString();
        artist = in.readString();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

}
