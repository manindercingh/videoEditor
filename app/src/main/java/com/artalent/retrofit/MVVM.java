package com.artalent.retrofit;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.artalent.models.MusicModel;
import com.artalent.utility.CommonUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MVVM extends ViewModel {
    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    MutableLiveData<MusicModel> musicLiveData;
    String networkError = "Network not connected";
    String strToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC8zLjExMS45My4xNzZcL2FwaVwvdmVyaWZ5LW90cCIsImlhdCI6MTY1MjY5ODg5OCwiZXhwIjoxNjU1MjkwODk4LCJuYmYiOjE2NTI2OTg4OTgsImp0aSI6InNIREtDWGtSdnNBSDRHelIiLCJzdWIiOjE2LCJwcnYiOiIyM2JkNWM4OTQ5ZjYwMGFkYjM5ZTcwMWM0MDA4NzJkYjdhNTk3NmY3In0.4HYXmI8kS-cd13YpVB4tjC9eTs5k4A4pHHcpggiRqYo";

    public MutableLiveData<MusicModel> getMusicLiveData(Context requireActivity) {
        musicLiveData = new MutableLiveData<>();

        if (CommonUtils.isNetworkConnected(requireActivity)) {

            apiInterface.getMusicData(strToken).enqueue(new Callback<MusicModel>() {
                @Override
                public void onResponse(@NonNull Call<MusicModel> call, @NonNull Response<MusicModel> response) {
                    if (response.code() == 200) {
                        musicLiveData.postValue(response.body());
                    } else {
                        Toast.makeText(requireActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MusicModel> call, @NonNull Throwable t) {
                    Toast.makeText(requireActivity, t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.i("ar_talent_api", t.getMessage());

                }
            });

        } else {
            Toast.makeText(requireActivity, networkError, Toast.LENGTH_SHORT).show();
        }

        return musicLiveData;

    }


}
