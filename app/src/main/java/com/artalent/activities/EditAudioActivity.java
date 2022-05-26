package com.artalent.activities;

import static com.artalent.activities.VideoEditorActivity.TAG;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.artalent.utility.AwsConstants;
import com.artalent.utility.CommonUtils;
import com.artalent.utility.ErrorActivity;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.mohammedalaa.seekbar.DoubleValueSeekBarView;
import com.mohammedalaa.seekbar.OnDoubleValueSeekBarChangeListener;

import java.io.File;
import java.util.concurrent.TimeUnit;

import soup.neumorphism.NeumorphCardView;

public class EditAudioActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    long pauseMusicDuration;
    private ImageView imgBack, imgPlay, imgPause;
    private TextView txtAudioTitle;
    private DoubleValueSeekBarView double_range_seekbar;
    private String fileName, inputPath;
    private boolean isDownloading;
    private int startingMS, endingMS;
    private NeumorphCardView crdDone;
    private MediaPlayer mediaPlayer;
    private int videoLength, audioLength;
    private float musicDuration;

    @SuppressLint("ObsoleteSdkInt")
    private void downloadAudio(Context context, String url, String outputName) {
        Log.i("ar_talent", "audioPath : " + outputName);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download Audio");
        request.setTitle(outputName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }
        File cacheDir = new File("ARTalent");

        if (!cacheDir.isDirectory()) {
            try {
                cacheDir.mkdir();
                Log.i(TAG, "CACHE DIRECTORY CREATED");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "CACHE DIRECTORY Exists");
        }
        request.setDestinationInExternalPublicDir(cacheDir.getPath(), outputName);
        Log.i(TAG, "download internal path : " + cacheDir.getPath());


        DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        long abc = manager.enqueue(request);

        if (abc != 0) {
            Toast.makeText(context, "download started", Toast.LENGTH_SHORT).show();
            isDownloading = true;
        } else {
            isDownloading = false;
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
        fileName = musicName;


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

        crdDone.setOnClickListener(v -> {

            if (isDownloading) {
                File cacheDir = new File(Environment.getExternalStorageDirectory() + "/" + "ARTalent", fileName);

                if (cacheDir.exists()) {
                    inputPath = cacheDir.getPath();
                    Log.i(TAG, "inputPath:" + inputPath);
                    executeTrimAudioCommand(startingMS, endingMS);

                } else {
                    Toast.makeText(this, "not", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "ERROR DOWNLOADING AUDIO", Toast.LENGTH_SHORT).show();
            }
        });

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
                startingMS = i*1000;
                endingMS = i1*1000;
                mediaPlayer.start();
                playMusic();

                new Handler().postDelayed(() -> {
                    mediaPlayer.pause();
                    imgPlay.setVisibility(View.VISIBLE);
                    imgPause.setVisibility(View.GONE);
                }, pauseMusicDuration);

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

    private void executeTrimAudioCommand(int startMs, int endMs) {

        File moviesDir = new File(getApplicationInfo().dataDir);
        String filePrefix = "trimming_audio";
        String fileExtension = ".mp4";
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        Log.i(TAG, "START_MS : " + startMs);
        Log.i(TAG, "END_MS : " + endMs);
        @SuppressLint("DefaultLocale") String startHms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(startMs),
                TimeUnit.MILLISECONDS.toMinutes(startMs) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startMs)),
                TimeUnit.MILLISECONDS.toSeconds(startMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startMs)));
        @SuppressLint("DefaultLocale") String endHms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(endMs),
                TimeUnit.MILLISECONDS.toMinutes(endMs) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(endMs)),
                TimeUnit.MILLISECONDS.toSeconds(endMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endMs)));
        Log.i(TAG, "TIME_FORMAT_START : " + startHms);
        Log.i(TAG, "TIME_FORMAT_END : " + endHms);
        String complexCommand = "-y -i " + inputPath + " -ss " + startHms + " -to " + endHms + " -c:a copy " + dest.getAbsolutePath();
        Log.i(TAG, "TIME_FORMAT_COMMAND : " + complexCommand);

        executeComplexCommand(complexCommand);

    }

    public void executeComplexCommand(String command) {
        FFmpegSession session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {

            Log.i(TAG, "SUCCESS with output : " + session.getAllLogsAsString());

        } else if (ReturnCode.isCancel(session.getReturnCode())) {

            CommonUtils.dismissDialog();
            Log.i(TAG, "CANCELLED" + " " + session.getAllLogsAsString());
            Intent intent = new Intent(EditAudioActivity.this, ErrorActivity.class);
            intent.putExtra("TAG", session.getAllLogsAsString());
            startActivity(intent);
        } else {
            CommonUtils.dismissDialog();
            Intent intent = new Intent(EditAudioActivity.this, ErrorActivity.class);
            intent.putExtra("TAG", session.getAllLogsAsString());
            startActivity(intent);
            Log.i(TAG, "Failed" + " " + session.getAllLogsAsString());
            Log.i(TAG, String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));

        }

//        FFmpeg.executeAsync(command, (executionId, returnCode) -> {
//
//            if (returnCode == RETURN_CODE_SUCCESS) {
//
//                Log.i(TAG, "SUCCESS");
//                Log.i(TAG, "SUCCESS with output : " + returnCode);
//                yourRealPath = afterEditFilePath.getAbsolutePath();
//                Log.i(TAG, "Path After Execution getAbsolutePath : " + yourRealPath);
//                Log.i(TAG, "Path After Execution PATH : " + afterEditFilePath.getPath());
//                Log.i(TAG, "File Name : " + afterEditFilePath.getName());
//                selectedVideoUri = Uri.fromFile(new File(filePath));
//                VideoUri videoUri2 = new VideoUri(filePath, MainActivity.this);
//                videoUris.set(selectedItemIndex, videoUri2);
//
//                if (EDITOR_TYPE.equals("FINAL_VIDEO")) {
//                    EDITOR_TYPE = "";
//                    videoUris.clear();
//                    VideoUri videoUri = new VideoUri(filePath, MainActivity.this);
//                    videoUris.add(videoUri);
//                    txtAudioTitle.setText(new File(filePath).getName());
//                    selectedItemIndex = 0;
//                    runOnUiThread(this::clearCache);
//                }
//                multipleViewsAdapter.notifyDataSetChanged();
//                videoView.setVideoPath(filePath);
//                imgFrame.setVisibility(View.GONE);
//                videoView.setVisibility(View.VISIBLE);
//                crdPlay.setVisibility(View.GONE);
//                crdPause.setVisibility(View.VISIBLE);
//                videoView.start();
//                onVideoCompleteListener();
//            } else if (returnCode == RETURN_CODE_CANCEL) {
//
//
//                File newFile = new File(filePath);
//                newFile.delete();
//                Log.i(TAG, "Failed" + " " + returnCode);
//            } else {
//
//                File newFile = new File(filePath);
//                newFile.delete();
//
//                Log.i(TAG, "Failed" + " " + returnCode);
//            }
//
//        });

    }
}