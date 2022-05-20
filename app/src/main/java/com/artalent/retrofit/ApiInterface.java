package com.artalent.retrofit;

import com.artalent.MusicModel;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Part;

public interface ApiInterface {

    @GET("get-music")
    Call<MusicModel> getMusicData(@Header("Authorization") String token);

    @GET("upload-music")
    Call<MusicModel> uploadMusic(@Field("")String fileUrl);
}
