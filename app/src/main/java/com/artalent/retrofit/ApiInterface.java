package com.artalent.retrofit;

import com.artalent.MusicModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiInterface {

    @GET("get-music")
    Call<MusicModel> getMusicData(@Header("Authorization") String token);
}
