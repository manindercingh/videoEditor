package com.artalent.activities;

import static com.artalent.activities.VideoEditorActivity.TAG;
import static com.artalent.utility.AwsConstants.MY_ACCESS_KEY_ID;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.artalent.R;
import com.artalent.models.MusicItemsAdapter;
import com.artalent.models.MusicModel;
import com.artalent.retrofit.MVVM;
import com.artalent.utility.AWSUtils;
import com.artalent.utility.AwsConstants;
import com.artalent.utility.UriUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity implements MusicItemsAdapter.GetMusicItem, AWSUtils.OnAwsImageUploadListener {
    private final List<MusicModel.MusicList> musicItemsList = new ArrayList<>();
    private final int REQUEST_TAKE_AUDIO = 214;
    private EditText edtSearchView;
    private ImageView icBack;
    private RecyclerView rvMusicItems;
    private MediaPlayer mediaPlayer;
    private RelativeLayout rlAdd, mView;
    private MusicItemsAdapter musicItemsAdapter;
    private String strFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        findIds();
        setTextWatcher();
        getMusic();
        setClicks();
    }


//    public void upload() {
////                ap-south-1:03175e9c-3209-422d-bff8-e714aedc2e74
//        BasicAWSCredentials awsCreds = new BasicAWSCredentials(MY_ACCESS_KEY_ID, MY_SECRET_KEY);
//        AmazonS3Client s3Client = (AmazonS3Client) AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
//                .withRegion(Regions.AP_SOUTH_1)
//                .build();
//        s3Client.putObject(new PutObjectRequest("images123456", MY_SECRET_KEY, new File(strFilePath)).withCannedAcl(CannedAccessControlList.PublicRead));
//        try {
//            String url = s3Client.getResourceUrl("images123456", MY_SECRET_KEY);
//            Log.i("ar_talent", url + "");
//        } catch (Exception error) {
//            Log.i("ar_talent", error + "");
//            Toast.makeText(MusicActivity.this, "" + error, Toast.LENGTH_LONG).show();
//        }
//    }

    public void upload() {
        TransferNetworkLossHandler.getInstance(MusicActivity.this);
        CognitoCachingCredentialsProvider credentialsProvider;
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                AwsConstants.COGNITO_IDENTITY_ID, // Identity Pool ID
                AwsConstants.COGNITO_REGION
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
        final TransferObserver observer = transferUtility.upload(
                "images123456", // this is the bucket name on S3
                MY_ACCESS_KEY_ID, // this is the path and name
                new File(strFilePath), // path to the file locally
                CannedAccessControlList.PublicRead // to make the file public
        );
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED)) {
                    Toast.makeText(MusicActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    //Success
                } else if (state.equals(TransferState.FAILED)) {
                    //Fail
                    Toast.makeText(MusicActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MusicActivity.this, "other error : " + state, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Toast.makeText(MusicActivity.this, "onProgressChanged", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int id, Exception ex) {
                Toast.makeText(MusicActivity.this, "ERROR : " + ex.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void findIds() {
        mediaPlayer = new MediaPlayer();
        rvMusicItems = findViewById(R.id.rvMusicItems);
        icBack = findViewById(R.id.icBack);
        edtSearchView = findViewById(R.id.edtSearchView);
        rvMusicItems = findViewById(R.id.rvMusicItems);
        rlAdd = findViewById(R.id.rlAdd);
        mView = findViewById(R.id.rlView);
    }

    private void setClicks() {

        icBack.setOnClickListener(v -> onBackPressed());

        rlAdd.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), REQUEST_TAKE_AUDIO);
        });
    }

    private void setTextWatcher() {

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Toast.makeText(MusicActivity.this, s.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        edtSearchView.addTextChangedListener(textWatcher);

    }

    private void setAdapters() {
        musicItemsAdapter = new MusicItemsAdapter(musicItemsList, MusicActivity.this, MusicActivity.this);
        rvMusicItems.setAdapter(musicItemsAdapter);
    }

    private void getMusic() {

        new MVVM().getMusicLiveData(MusicActivity.this).observe(this, musicModel -> {
            if (musicModel.status == 200) {

                for (int i = 0; i < musicModel.music_list.size(); i++) {
                    musicItemsList.add(musicModel.getMusic_list().get(i));
                }

                setAdapters();

            } else {
                Toast.makeText(MusicActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_AUDIO && data != null) {

            strFilePath = UriUtils.getPathFromUri(MusicActivity.this, data.getData());

            AWSUtils awsUtils = new AWSUtils(strFilePath, this, this);
            awsUtils.beginUpload();

//            Toast.makeText(MusicActivity.this, "path : " + CommonUtils.getRealPath(MusicActivity.this, data.getData()), Toast.LENGTH_SHORT).show();
            Toast.makeText(MusicActivity.this, "path : " + strFilePath, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Audio Uploading Cancelled", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void getMusic(int sIndex, String musicUrl) {

        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(musicUrl);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> Toast.makeText(MusicActivity.this, "Buffering" + percent, Toast.LENGTH_SHORT).show());
                mediaPlayer.setOnCompletionListener(MediaPlayer::reset);
                Log.i("ar_talent", "run");
            } catch (IOException e) {
                Log.i("ar_talent", e.toString());
            }
        } else {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicUrl);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                mediaPlayer.setOnCompletionListener(MediaPlayer::reset);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("ar_talent", "error message " + e);
            }

        }

    }

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        return super.getDelegate();
    }

    @Override
    public void selectMusic(int sIndex, String url, String musicName) {
//        if (AwsConstants.VIDEO_LENGTH > mediaPlayer.getDuration()) {
            Log.i(TAG, "VIDEO_LENGTH : "+AwsConstants.VIDEO_LENGTH+" MUSIC_LENGTH : "+mediaPlayer.getDuration());
            Intent intent = new Intent(MusicActivity.this, EditAudioActivity.class);
            intent.putExtra("MUSIC_URL", url);
            intent.putExtra("MUSIC_NAME", musicName);
            startActivity(intent);
            finish();
//        } else {
//            Log.i(TAG, "VIDEO_LENGTH : "+AwsConstants.VIDEO_LENGTH+" MUSIC_LENGTH : "+mediaPlayer.getDuration());
//
//            Snackbar.make(mView, "Please choose another music", 2500).show();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        Log.i("ar_talent", "onDestroy()");
    }

    @Override
    public void showProgressDialog() {
        Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void hideProgressDialog() {

    }

    @Override
    public void onSuccess(String imgUrl) {
        Toast.makeText(this, "Url : " + imgUrl, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String errorMsg) {
        Toast.makeText(this, "Error : " + errorMsg, Toast.LENGTH_SHORT).show();

    }
}



