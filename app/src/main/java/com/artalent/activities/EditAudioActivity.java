package com.artalent.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artalent.R;
import com.artalent.utility.CommonUtils;
import com.mohammedalaa.seekbar.DoubleValueSeekBarView;
import com.mohammedalaa.seekbar.OnDoubleValueSeekBarChangeListener;

import java.io.File;

import soup.neumorphism.NeumorphCardView;

public class EditAudioActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    private ImageView imgBack, imgPlay, imgPause;
    private TextView txtAudioTitle;
    private DoubleValueSeekBarView double_range_seekbar;
    private NeumorphCardView crdDone;
    private MediaPlayer mediaPlayer;
    private String musicURL, musicName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_audio);
        findIds();
        getSelectedMusic();
        setClicks();
    }

    private void getSelectedMusic() {

        File dir = new File(getCacheDir(), "test");
        if (dir.exists()) {
            Toast.makeText(this, "Directory Exists" , Toast.LENGTH_SHORT).show();
        } else{
            dir.mkdir();
        }

        Intent i = getIntent();
        musicURL = i.getStringExtra("MUSIC_URL");
        musicName = i.getStringExtra("MUSIC_NAME");
        Toast.makeText(this, "Music Url : " + musicName, Toast.LENGTH_LONG).show();
        if (musicName != null) {
            txtAudioTitle.setText(musicName);
        }

        try {
            mediaPlayer.setDataSource(musicURL);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnCompletionListener(MediaPlayer::reset);
            playMusic();
            Log.i("ar_talent", "run");
        } catch (Exception e) {
            Log.i("ar_talent", "Error : " + e);
        }

        Log.i("ar_talent", "MusicName : " + musicName);
        Log.i("ar_talent", "Music Url : " + musicURL);
        CommonUtils.downloadAudio(EditAudioActivity.this, musicURL, musicName);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.setOnPreparedListener(MediaPlayer::pause);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void setClicks() {
        imgBack.setOnClickListener(v -> onBackPressed());
        imgPlay.setOnClickListener(v -> {
            if (imgPlay.getVisibility() == View.VISIBLE) {
                playMusic();
            } else {
                pauseMusic();
            }
        });
        imgPause.setOnClickListener(v -> {
            if (imgPause.getVisibility() == View.VISIBLE) {
                pauseMusic();
            } else {
                playMusic();
            }
        });

        double_range_seekbar.setOnRangeSeekBarViewChangeListener(new OnDoubleValueSeekBarChangeListener() {
            @Override
            public void onValueChanged(@Nullable DoubleValueSeekBarView doubleValueSeekBarView, int start, int end, boolean b) {
                Toast.makeText(EditAudioActivity.this, "start : " + start + " end : " + end, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(@Nullable DoubleValueSeekBarView doubleValueSeekBarView, int i, int i1) {

            }

            @Override
            public void onStopTrackingTouch(@Nullable DoubleValueSeekBarView doubleValueSeekBarView, int i, int i1) {

            }
        });
    }

    private void playMusic() {
        imgPlay.setVisibility(View.GONE);
        imgPause.setVisibility(View.VISIBLE);
    }

    private void pauseMusic() {
        imgPause.setVisibility(View.GONE);
        imgPlay.setVisibility(View.VISIBLE);
    }

    private void findIds() {
        mediaPlayer = new MediaPlayer();
        double_range_seekbar = findViewById(R.id.double_range_seekbar);
        txtAudioTitle = findViewById(R.id.txtAudioTitle);
        imgBack = findViewById(R.id.imgBack);
        imgPlay = findViewById(R.id.imgPlay);
        imgPause = findViewById(R.id.imgPause);
        crdDone = findViewById(R.id.crdDone);
    }
}