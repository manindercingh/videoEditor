package com.artalent.retrofit;

import com.artalent.models.MusicModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiInterface {

    @GET("get-music")
    Call<MusicModel> getMusicData(@Header("Authorization") String token);

    @GET("upload-music")
    Call<MusicModel> uploadMusic(@Field("")String fileUrl);
}
