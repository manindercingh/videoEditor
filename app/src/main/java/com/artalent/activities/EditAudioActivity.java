package com.artalent.activities;

import static com.artalent.activities.VideoEditorActivity.TAG;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artalent.R;
import com.artalent.utility.AwsConstants;
import com.mohammedalaa.seekbar.DoubleValueSeekBarView;
import com.mohammedalaa.seekbar.OnDoubleValueSeekBarChangeListener;

import java.io.File;

import soup.neumorphism.NeumorphCardView;

public class EditAudioActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    long pauseMusicDuration;
    private ImageView imgBack, imgPlay, imgPause;
    private TextView txtAudioTitle;
    private DoubleValueSeekBarView double_range_seekbar;
    private NeumorphCardView crdDone;
    private MediaPlayer mediaPlayer;
    private int videoLength, audioLength;
    private float musicDuration;

    private void downloadAudio(Context context, String url, String outputName) {
        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();
        File direct = new File(context.getApplicationInfo().dataDir);
        Log.i("ar_talent", "audioPath : " + outputName);
        if (!direct.isDirectory()) direct.mkdirs();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download Audio");
        request.setTitle(outputName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }
        request.setDestinationInExternalPublicDir(context.getApplicationInfo().dataDir, outputName);

        DownloadManager manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        long abc = manager.enqueue(request);

        if (abc != 0) {
            Toast.makeText(context, "download started", Toast.LENGTH_SHORT).show();
            audioDownloadedListener();
        } else {
            Toast.makeText(context, "no download started", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_audio);
        findIds();
        getSelectedMusic();
        setClicks();
    }

    private void getSelectedMusic() {
        Intent i = getIntent();
        String musicURL = i.getStringExtra("MUSIC_URL");
        String musicName = i.getStringExtra("MUSIC_NAME");
        if (musicName != null) {
            txtAudioTitle.setText(musicName);
        }

        try {
            mediaPlayer.setDataSource(musicURL);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnCompletionListener(MediaPlayer::reset);
            musicDuration = mediaPlayer.getDuration();
            playMusic();
            videoLength = AwsConstants.VIDEO_LENGTH / 1000;
            audioLength = mediaPlayer.getDuration() / 1000;
            Log.i("ar_talent", "run");
        } catch (Exception e) {
            Log.i("ar_talent", "Error : " + e);
        }

        Log.i("ar_talent", "MusicName : " + musicName);
        Log.i("ar_talent", "Music Url : " + musicURL);
        Log.i(TAG, "videoLength: " + videoLength + " audioLength: " + musicDuration);
        downloadAudio(EditAudioActivity.this, musicURL, musicName);
        AwsConstants.MUSIC_NAME = musicName;


    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        Log.i("ar_talent", "onDestroy()");
    }

    private void setClicks() {
        imgBack.setOnClickListener(v -> {
            doubleBackToExitPressedOnce = true;
            onBackPressed();
            finish();
        });

        crdDone.setOnClickListener(v -> Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show());

        imgPlay.setOnClickListener(v -> {
            if (imgPlay.getVisibility() == View.VISIBLE) {
                playMusic();
            } else {
                pauseMusic();
            }
        });
        imgPause.setOnClickListener(v -> {
            double_range_seekbar.cancelDragAndDrop();
            if (imgPause.getVisibility() == View.VISIBLE) {
                pauseMusic();
            } else {
                playMusic();
            }
        });


        double_range_seekbar.setMaxValue(audioLength);
        double_range_seekbar.setCurrentMaxValue(videoLength);

        double_range_seekbar.setOnRangeSeekBarViewChangeListener(new OnDoubleValueSeekBarChangeListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onValueChanged(@Nullable DoubleValueSeekBarView doubleValueSeekBarView, int i, int i1, boolean b) {

                if (i < 0) {
                    doubleValueSeekBarView.setCurrentMinValue(0);
                    doubleValueSeekBarView.setCurrentMaxValue(videoLength);

                } else if (i1 > audioLength) {

                    doubleValueSeekBarView.setCurrentMinValue(audioLength - videoLength);
                    doubleValueSeekBarView.setCurrentMaxValue(audioLength);

                } else {
                    doubleValueSeekBarView.setCurrentMaxValue(i + videoLength);
                    doubleValueSeekBarView.setCurrentMinValue(i1 - videoLength);
                }

                pauseMusicDuration = i * 1000L;
                Log.i(TAG, "PAUSE_MUSIC_DURATION : " + pauseMusicDuration);


                mediaPlayer.seekTo(i * 1000);
                mediaPlayer.start();
                playMusic();

                new Handler().postDelayed(() -> {
                    mediaPlayer.pause();
                    imgPlay.setVisibility(View.VISIBLE);
                    imgPause.setVisibility(View.GONE);
                }, pauseMusicDuration);

//                Toast.makeText(EditAudioActivity.this, "start : " + start + " end : " + end + " total : " + musicDuration, Toast.LENGTH_SHORT).show();

//                if (endedTrim - startedTrim)

            }

            @Override
            public void onStartTrackingTouch(@Nullable DoubleValueSeekBarView doubleValueSeekBarView, int i, int i1) {

            }

            @Override
            public void onStopTrackingTouch(@Nullable DoubleValueSeekBarView doubleValueSeekBarView, int i, int i1) {
                float start = i;
                float end = i1;
//                Toast.makeText(EditAudioActivity.this, "start : " + start + " end : " + end + " total : " + musicDuration, Toast.LENGTH_SHORT).show();
                float startedTrim;
                float endedTrim;


//                startedTrim = musicDuration * start / 100;
//                endedTrim = musicDuration * end / 100;
//
//                Toast.makeText(EditAudioActivity.this, "start : " + startedTrim + " end : " + endedTrim + " total : " + musicDuration, Toast.LENGTH_SHORT).show();

            }
        });
//        double_range_seekbar.setOnClickListener(v -> double_range_seekbar.setEnabled(true));
    }

    private void playMusic() {
        imgPlay.setVisibility(View.GONE);
        imgPause.setVisibility(View.VISIBLE);
        mediaPlayer.start();
    }

    private void pauseMusic() {
        imgPause.setVisibility(View.GONE);
        imgPlay.setVisibility(View.VISIBLE);
        mediaPlayer.pause();
        mediaPlayer.getCurrentPosition();
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

    private void audioDownloadedListener() {
//        new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                DownloadManager downloadManager = new DownloadManager.Request(intent.getData());
//                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//                    long downloadId = intent.getLongExtra(
//                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
//                    DownloadManager.Query query = new DownloadManager.Query();
//                    query.setFilterById(enqueue);
//                    Cursor c = dm.query(query);
//                    if (c.moveToFirst()) {
//                        int columnIndex = c
//                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
//                        if (DownloadManager.STATUS_SUCCESSFUL == c
//                                .getInt(columnIndex)) {
//                            Toast.makeText(context, "AUDIO DOWNLOADED", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            }
//        };
    }
}