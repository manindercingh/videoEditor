package com.artalent;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.artalent.retrofit.MVVM;
import com.artalent.utility.UriUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity implements MusicItemsAdapter.GetMusicItem {
    private final List<MusicModel.MusicList> musicItemsList = new ArrayList<>();
    private final int REQUEST_TAKE_AUDIO = 214;
    private EditText edtSearchView;
    private ImageView icBack;
    private RecyclerView rvMusicItems;
    private MediaPlayer mediaPlayer;
    private RelativeLayout rlAdd;
    private MusicItemsAdapter musicItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        findIds();
        setTextWatcher();
        getMusic();
        setClicks();
    }

    private void findIds() {
        mediaPlayer = new MediaPlayer();
        rvMusicItems = findViewById(R.id.rvMusicItems);
        icBack = findViewById(R.id.icBack);
        edtSearchView = findViewById(R.id.edtSearchView);
        rvMusicItems = findViewById(R.id.rvMusicItems);
        rlAdd = findViewById(R.id.rlAdd);
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
            try {
                mediaPlayer.setDataSource(MusicActivity.this, data.getData());
                mediaPlayer.prepare();
                mediaPlayer.setOnTimedMetaDataAvailableListener((mp, data1) -> Log.i("ar_talent", data1.toString()));
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Toast.makeText(MusicActivity.this, "path : " + CommonUtils.getRealPath(MusicActivity.this, data.getData()), Toast.LENGTH_SHORT).show();
            Toast.makeText(MusicActivity.this, "path : " + UriUtils.getPathFromUri(MusicActivity.this, data.getData()), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Audio Uploading Cancelled", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void getMusic(int sIndex, String musicUrl) {

        if (!mediaPlayer.isPlaying()) {
//            Toast.makeText(this, musicUrl, Toast.LENGTH_SHORT).show();

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
}


