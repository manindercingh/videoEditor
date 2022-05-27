package com.artalent.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.artalent.R;
import com.artalent.adapters.MultipleViewsAdapter;
import com.artalent.models.VideoUri;
import com.artalent.threads.ThreadsInActivity;
import com.artalent.utility.AwsConstants;
import com.artalent.utility.CommonUtils;
import com.artalent.utility.ErrorActivity;
import com.artalent.utility.UriUtils;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.google.android.material.snackbar.Snackbar;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import soup.neumorphism.NeumorphCardView;

//TO WORK
public class VideoEditorActivity extends AppCompatActivity implements MultipleViewsAdapter.Click {
    public static final String TAG = "ar_talent_video_editor";
    public static String WIDTH = "";
    public static String EDITOR_TYPE = "";
    public static int REQUEST_TAKE_GALLERY_VIDEO = 23879120;
    public static int VIDEO_DURATION = 0;
    public static int selectedItemIndex = 0;
    public static List<Boolean> isAllTrue = new ArrayList<>();
    public static List<Boolean> isAllTSTrue = new ArrayList<>();
    public static List<VideoUri> videoUris = new ArrayList<>();
    public static String yourRealPath, filePath, filterType, volumeLevel;
    private final String[] permission = {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private final int snackDuration = 1000;
    private final List<String> multiplePaths = new ArrayList<>();
    private String olderPath = "";
    private SeekBar seekBar;
    private VideoUri videoUri;
    private MultipleViewsAdapter multipleViewsAdapter;
    private RecyclerView rvViews;
    private VideoView videoView;
    private File afterEditFilePath;
    private NeumorphCardView crdDone, crdGallery, crdMusic, crdDelete, crdVolume, cardView, crdTrimmer, crdTick, crdBack;
    private ImageView icTrim, icPreDefinedFilters, icRotate, icSpeed, icFilters, imgFrame, icRotateLeft;
    //    icTransition
    private RangeSeekBar rangeSeekBar;
    private TextView txtAudioTitle;
    private int storageRequest, totalTime;
    private int minVal;
    private int maxVal;
    private LinearLayout llFilterOptions, llBlacknWhite, llNormal, llNoneRotate, llNoneFilter, llRotateRight, llDuoTone, llFilm, llRotate, llVintage, llCrossProcess, llGama, llPreDefinedFilters, llExposure, llBrightness, llContrast, llSaturation, llFade, llTint, llWarmth, llSpeedDec, llSpeedInc;
    private int mAnglePosition = 0;
    private ImageView crdPlay, crdPause, icStartTrimming;
    private RelativeLayout rlMainView, rlVideoFrame, rlAdd, rlSeekBar, rlSpeed;
    private Uri selectedVideoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);
        initializeFFMPEG();
        findIds();
        setClicks();
        setAdapters();
        clearCache();
//        getMusicFromUri();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openDialog() {
        Dialog dialog = new Dialog(VideoEditorActivity.this);
        dialog.setContentView(R.layout.layout_dialog_delete);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.findViewById(R.id.crdCancel).setOnClickListener(v2 -> dialog.dismiss());
        dialog.findViewById(R.id.btnCancel).setOnClickListener(v2 -> dialog.dismiss());
        dialog.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            try {
                videoUris.remove(selectedItemIndex);
                multipleViewsAdapter.notifyDataSetChanged();
                manageAddButton();
                if (videoUris.size() == 0) {
                    videoView.stopPlayback();
                    selectedVideoUri = null;
                    yourRealPath = "";
                    selectedItemIndex = 0;
                    videoView.setVisibility(View.GONE);
                    txtAudioTitle.setText(R.string.on_my_way_alkan_walker);
                    crdPlay.setVisibility(View.VISIBLE);
                    crdPause.setVisibility(View.GONE);
                    imgFrame.setVisibility(View.VISIBLE);
                }
                Log.i(TAG, videoUris.toString());
                Log.i(TAG, videoUri.uris(videoUris));
            } catch (Exception e) {
                e.printStackTrace();
            }

            dialog.dismiss();

        });
        dialog.findViewById(R.id.crdDeleteItem).setOnClickListener(v -> {

            try {
                videoUris.remove(selectedItemIndex);
                multipleViewsAdapter.notifyDataSetChanged();
                manageAddButton();
                if (videoUris.size() == 0) {
                    videoView.stopPlayback();
                    selectedVideoUri = null;
                    yourRealPath = "";
                    selectedItemIndex = 0;
                    videoView.setVisibility(View.GONE);
                    txtAudioTitle.setText(R.string.on_my_way_alkan_walker);
                    crdPlay.setVisibility(View.VISIBLE);
                    crdPause.setVisibility(View.GONE);
                    imgFrame.setVisibility(View.VISIBLE);

                }
                Log.i(TAG, videoUris.toString());
                Log.i(TAG, videoUri.uris(videoUris));
            } catch (Exception e) {
                e.printStackTrace();
            }

            dialog.dismiss();

        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            storageRequest = 0;

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else {

            storageRequest = 2;

        }
    }

    private void initializeFFMPEG() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        WIDTH = String.valueOf(metrics.widthPixels - 10);
    }

    private void requestStoragePermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            storageRequest = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permission, 1);
            }

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else {

            storageRequest = 2;
        }

    }

    void onVideoCompleteListener() {
        crdPlay.setVisibility(View.GONE);
        crdPause.setVisibility(View.VISIBLE);
        videoView.setOnCompletionListener(mp -> {
            crdPlay.setVisibility(View.VISIBLE);
            crdPause.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && data != null) {
            String strFilePath = UriUtils.getPathFromUri(this, data.getData());
            selectedVideoUri = data.getData();
            manageVideo();
            assert strFilePath != null;
            String strFileName = new File(strFilePath).getName();
            txtAudioTitle.setText(strFileName);
            setAdapters();
        } else {
            Snackbar.make(rlMainView, "Video Uploading Cancelled", snackDuration).show();
        }
    }

    private void manageVideo() {
        yourRealPath = UriUtils.getPathFromUri(VideoEditorActivity.this, selectedVideoUri);
        olderPath = yourRealPath;
        MediaPlayer m = MediaPlayer.create(VideoEditorActivity.this, selectedVideoUri);
        VIDEO_DURATION = m.getDuration();
        videoView.setVideoPath(yourRealPath);
        imgFrame.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        rlVideoFrame.setVisibility(View.VISIBLE);
        videoUri = new VideoUri(yourRealPath, VideoEditorActivity.this);
        videoUris.add(videoUri);
        selectedItemIndex = videoUris.size() - 1;
        manageAddButton();
        MediaPlayer mp = MediaPlayer.create(this, selectedVideoUri);
        int duration = mp.getDuration();
        Log.i(TAG, "" + "HEIGHT : " + mp.getVideoHeight() + "WIDTH : " + mp.getVideoWidth());
        rangeSeekBar.setRangeValues(0, duration);
        rangeSeekBar.setSelectedMinValue(0);
        rangeSeekBar.setSelectedMaxValue(duration);
        Log.i(TAG, "" + duration);
        videoView.start();
        onVideoCompleteListener();
    }

    private void manageAddButton() {
        if (videoUri.totalDuration(videoUris) >= 90000) {
            rlAdd.setVisibility(View.GONE);
        } else {
            rlAdd.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setClicks() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (filterType.equalsIgnoreCase("EXPOSURE")) {
                    Toast.makeText(VideoEditorActivity.this, "EXPOSURE : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                } else if (filterType.equalsIgnoreCase("BRIGHTNESS")) {
                    Toast.makeText(VideoEditorActivity.this, "BRIGHTNESS : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                } else if (filterType.equalsIgnoreCase("CONTRAST")) {
                    Toast.makeText(VideoEditorActivity.this, "CONTRAST : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                } else if (filterType.equalsIgnoreCase("SATURATION")) {
                    Toast.makeText(VideoEditorActivity.this, "SATURATION : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                } else if (filterType.equalsIgnoreCase("FADE")) {
                    Toast.makeText(VideoEditorActivity.this, "FADE : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                } else if (filterType.equalsIgnoreCase("TINT")) {
                    Toast.makeText(VideoEditorActivity.this, "TINT : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                } else if (filterType.equalsIgnoreCase("WARMTH")) {
                    Toast.makeText(VideoEditorActivity.this, "WARMTH : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                } else if (filterType.equalsIgnoreCase("VOLUME")) {

                    float v = seekBar.getProgress();
                    float calc = (float) (v / 10.0);
                    volumeLevel = String.valueOf(calc);
                    Toast.makeText(VideoEditorActivity.this, "VOLUME : " + volumeLevel, Toast.LENGTH_SHORT).show();
                }

            }
        });


        crdGallery.setOnClickListener(v -> {

            if (storageRequest == 0) {
                requestStoragePermission();
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
            }

        });
        rlAdd.setOnClickListener(v -> {
            try {
                if (storageRequest == 0) {
                    requestStoragePermission();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
                }
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }

        });

        llSpeedInc.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                EDITOR_TYPE = "llSpeedInc";
                runOnUiThread(this::executeFastMotionVideoCommand);
                cardView.setVisibility(View.VISIBLE);
                rlSpeed.setVisibility(View.GONE);
            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }
        });
        llSpeedDec.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                EDITOR_TYPE = "llSpeedDec";
//                runOnUiThread(this::executeSlowMotionVideoCommand);
                executeSlowMotionVideoCommand();
                cardView.setVisibility(View.VISIBLE);
                rlSpeed.setVisibility(View.GONE);

            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }
        });

        icSpeed.setOnClickListener(v -> {

            if (selectedVideoUri != null) {
                rlSpeed.setVisibility(View.VISIBLE);
                llRotate.setVisibility(View.GONE);
                crdTrimmer.setVisibility(View.GONE);
                llPreDefinedFilters.setVisibility(View.GONE);
                llFilterOptions.setVisibility(View.GONE);

                icSpeed.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.app_color));
                icPreDefinedFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icTrim.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icRotate.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
            } else
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
        });

        icFilters.setOnClickListener(v -> {
            Snackbar.make(rlMainView, "To be implemented", snackDuration).show();
            if (selectedVideoUri != null) {
                llFilterOptions.setVisibility(View.VISIBLE);
                llRotate.setVisibility(View.GONE);
                llPreDefinedFilters.setVisibility(View.GONE);
                crdTrimmer.setVisibility(View.GONE);
                rlSpeed.setVisibility(View.GONE);
                rlSeekBar.setVisibility(View.GONE);
                icFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.app_color));
                icSpeed.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icPreDefinedFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icTrim.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icRotate.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));

            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }

        });
        icRotateLeft.setOnClickListener(v -> {
            if (mAnglePosition == 0) {

                mAnglePosition = 270;

            } else {
                mAnglePosition = mAnglePosition - 90;
            }

            runOnUiThread(this::executeRotateVideoCommand);
        });

        llRotateRight.setOnClickListener(v -> {
//            if (mAnglePosition == 270) {
//                mAnglePosition = 0;
//            } else {
            mAnglePosition = mAnglePosition - 90;
//            }

            runOnUiThread(this::executeRotateVideoCommand);

        });

        llNoneRotate.setOnClickListener(v -> undoWithOlderVideo());
        llNormal.setOnClickListener(v -> undoWithOlderVideo());
        llNoneFilter.setOnClickListener(v -> undoWithOlderVideo());

        icRotate.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                EDITOR_TYPE = "icRotate";
                llRotate.setVisibility(View.VISIBLE);
                llPreDefinedFilters.setVisibility(View.GONE);
                crdTrimmer.setVisibility(View.GONE);
                llFilterOptions.setVisibility(View.GONE);
                rlSpeed.setVisibility(View.GONE);

                icRotate.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.app_color));
                icPreDefinedFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icTrim.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icSpeed.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));

            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }

        });

//        icTransition.setOnClickListener(v -> {
//            if (selectedVideoUri != null) {
//                EDITOR_TYPE = "icTransition";
//
//
//                runOnUiThread(() -> {
//
//                    executeFadeInFadeOutCommand();
//                });
//
//            } else {
//                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
//            }
//        });

        icPreDefinedFilters.setOnClickListener(v -> {
            if (selectedVideoUri == null) {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            } else {
                llPreDefinedFilters.setVisibility(View.VISIBLE);
                crdTrimmer.setVisibility(View.GONE);
                llFilterOptions.setVisibility(View.GONE);
                rlSpeed.setVisibility(View.GONE);
                llRotate.setVisibility(View.GONE);

                icPreDefinedFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.app_color));
                icTrim.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icRotate.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icSpeed.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
            }
        });

//        icTrim.setOnClickListener(v -> {
//
//            if (selectedVideoUri != null) {
//
//                cardView.setVisibility(View.VISIBLE);
//                crdTrimmer.setVisibility(View.VISIBLE);
//
//            } else {
//                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
//            }
//        })

        icTrim.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                icTrim.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.app_color));
                icPreDefinedFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icRotate.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icSpeed.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                icFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                crdTrimmer.setVisibility(View.VISIBLE);
                llRotate.setVisibility(View.GONE);
                llFilterOptions.setVisibility(View.GONE);
                rlSpeed.setVisibility(View.GONE);
                llPreDefinedFilters.setVisibility(View.GONE);
                llPreDefinedFilters.setVisibility(View.GONE);
            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }
        });

        rangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> {

            minVal = Integer.parseInt(minValue.toString());
            maxVal = Integer.parseInt(maxValue.toString());

            Log.i(TAG, " MINIMUM VALUE : " + minVal + ", MAXIMUM VALUE : " + maxVal);
            Log.i(TAG, "<< MINIMUM VALUE : " + minValue + ",<< MAXIMUM VALUE : " + maxValue);

            videoView.seekTo(minVal);
            videoView.start();

            int min = Integer.valueOf(minVal);
            int max = Integer.valueOf(maxVal);
            totalTime = max - min;
            stopVideoAfterSpecificTime();
            crdPlay.setVisibility(View.GONE);
            crdPause.setVisibility(View.VISIBLE);
//            onVideoCompleteListener();
        });

        icStartTrimming.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                try {
                    EDITOR_TYPE = "icStartTrimming";

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> CommonUtils.showDialog(VideoEditorActivity.this));
                        }
                    };
                    thread.start();
                    icTrim.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
                    runOnUiThread(() -> executeTrimVideoCommand(minVal, maxVal));
                    cardView.setVisibility(View.VISIBLE);
                    crdTrimmer.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }


        });


        crdVolume.setOnClickListener(v -> {

            if (selectedVideoUri != null) {
                filterType = "VOLUME";
                llFilterOptions.setVisibility(View.GONE);
                rlSpeed.setVisibility(View.GONE);
                llRotate.setVisibility(View.GONE);
                crdTrimmer.setVisibility(View.GONE);
                llPreDefinedFilters.setVisibility(View.GONE);
                seekBar.setProgress(0);
                rlSeekBar.setVisibility(View.VISIBLE);

            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }

        });

        crdPlay.setOnClickListener(v -> {

            if (selectedVideoUri != null) {
                try {
                    videoView.start();
                    crdPlay.setVisibility(View.GONE);
                    crdPause.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }

        });

        crdPause.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                try {
                    crdPlay.setVisibility(View.VISIBLE);
                    crdPause.setVisibility(View.GONE);
                    videoView.pause();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }

        });

        crdDelete.setOnClickListener(v -> openDialog());

        crdMusic.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                AwsConstants.VIDEO_LENGTH = videoUri.getDuration();
                Log.i(TAG, "VIDEO_LENGTH" + AwsConstants.VIDEO_LENGTH);
                Intent intent = new Intent(VideoEditorActivity.this, MusicActivity.class);
                startActivity(intent);
            } else {
                Snackbar.make(rlMainView, "Please upload a video", snackDuration).show();
            }
        });

        crdBack.setOnClickListener(v -> {

            if (filterType.equalsIgnoreCase("VOLUME")) {

            } else {
                llFilterOptions.setVisibility(View.VISIBLE);

            }
            rlSeekBar.setVisibility(View.GONE);

        });

        crdTick.setOnClickListener(v -> {

            rlSeekBar.setVisibility(View.GONE);
            llFilterOptions.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);

            if (filterType.equalsIgnoreCase("EXPOSURE")) {
                Toast.makeText(VideoEditorActivity.this, "EXPOSURE : ", Toast.LENGTH_SHORT).show();
            } else if (filterType.equalsIgnoreCase("BRIGHTNESS")) {
                Toast.makeText(VideoEditorActivity.this, "BRIGHTNESS : ", Toast.LENGTH_SHORT).show();
            } else if (filterType.equalsIgnoreCase("CONTRAST")) {
                Toast.makeText(VideoEditorActivity.this, "CONTRAST : ", Toast.LENGTH_SHORT).show();
            } else if (filterType.equalsIgnoreCase("SATURATION")) {
                Toast.makeText(VideoEditorActivity.this, "SATURATION : ", Toast.LENGTH_SHORT).show();
            } else if (filterType.equalsIgnoreCase("FADE")) {
                Toast.makeText(VideoEditorActivity.this, "FADE : ", Toast.LENGTH_SHORT).show();
            } else if (filterType.equalsIgnoreCase("TINT")) {
                Toast.makeText(VideoEditorActivity.this, "TINT : ", Toast.LENGTH_SHORT).show();
            } else if (filterType.equalsIgnoreCase("WARMTH")) {
                Toast.makeText(VideoEditorActivity.this, "WARMTH : ", Toast.LENGTH_SHORT).show();
            } else if (filterType.equalsIgnoreCase("VOLUME")) {
                Toast.makeText(VideoEditorActivity.this, "VOLUME : ", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> executeAudioCommand(volumeLevel));
            }

        });

        llBlacknWhite.setOnClickListener(v -> {
            runOnUiThread(this::executeBlackAndWhiteVideoCommand);
        });
        llVintage.setOnClickListener(v -> {
            runOnUiThread(this::executeVintageVideoCommand);

        });
        llDuoTone.setOnClickListener(v -> {
            Snackbar.make(rlMainView, "To be implemented", snackDuration).show();
        });
        llFilm.setOnClickListener(v -> {
            Snackbar.make(rlMainView, "To be implemented", snackDuration).show();
        });
        llGama.setOnClickListener(v -> {
            runOnUiThread(this::executeGammaVideoCommand);
        });
        llCrossProcess.setOnClickListener(v -> {
            runOnUiThread(this::executeCrossProcessVideoCommand);
        });

        llBrightness.setOnClickListener(v -> {
            filterType = "BRIGHTNESS";
            llFilterOptions.setVisibility(View.GONE);
            rlSeekBar.setVisibility(View.VISIBLE);
        });
        llWarmth.setOnClickListener(v -> {
            filterType = "WARMTH";
            llFilterOptions.setVisibility(View.GONE);
            rlSeekBar.setVisibility(View.VISIBLE);
        });
        llContrast.setOnClickListener(v -> {
            filterType = "CONTRAST";
            llFilterOptions.setVisibility(View.GONE);
            rlSeekBar.setVisibility(View.VISIBLE);
        });
        llSaturation.setOnClickListener(v -> {
            filterType = "SATURATION";
            llFilterOptions.setVisibility(View.GONE);
            rlSeekBar.setVisibility(View.VISIBLE);
        });
        llFade.setOnClickListener(v -> {
            filterType = "FADE";
            llFilterOptions.setVisibility(View.GONE);
            rlSeekBar.setVisibility(View.VISIBLE);
        });
        llTint.setOnClickListener(v -> {
            filterType = "TINT";
            llFilterOptions.setVisibility(View.GONE);
            rlSeekBar.setVisibility(View.VISIBLE);
        });
        llExposure.setOnClickListener(v -> {
            filterType = "EXPOSURE";
            llFilterOptions.setVisibility(View.GONE);
            rlSeekBar.setVisibility(View.VISIBLE);
        });

        crdDone.setOnClickListener(v -> {
            if (selectedVideoUri != null) {
                if (cardView.getVisibility() == View.VISIBLE) {
                    if (videoUri.totalDuration(videoUris) <= 90000) {

                        runOnUiThread(this::merging);
                    } else {
                        Toast.makeText(this, "Your Video is more than 90 Seconds", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    cardView.setVisibility(View.VISIBLE);
                    llRotate.setVisibility(View.GONE);
                    crdTrimmer.setVisibility(View.GONE);
                    rlSeekBar.setVisibility(View.GONE);
                    llFilterOptions.setVisibility(View.GONE);
                    rlSpeed.setVisibility(View.GONE);
                }
            } else {
                Snackbar.make(rlMainView, "Please Upload Video", snackDuration).show();
            }
        });

    }


    public void mergeAndExportVideos() {
        EDITOR_TYPE = "FINAL_VIDEO";
        File moviesDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "ARTalent");
        if (!moviesDir.isDirectory()) moviesDir.mkdirs();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy-HH_mm_ss", Locale.getDefault()).format(new Date());
        String filePrefix = "ar-talent" + currentDate;
        String fileExtension = ".mp4";
        String paths = "";
        String maps = "";
        String[] comand = new String[(videoUris.size() * 2)];
        for (int i = 0; i < videoUris.size(); i++) {
            comand[i * 2] = "-i";
            comand[i * 2 + 1] = videoUris.get(i).getVideoPaths();
        }
        for (int i = 0; i < videoUris.size(); i++) {
            paths = paths + "-i " + videoUris.get(i).getVideoPaths() + " ";
            maps = maps + "[" + i + ":v]" + " [" + i + ":a] ";
            Log.i(TAG, "MULTIPLE_PATHS " + paths);

            Log.i(TAG, "MULTIPLE_MAPS" + maps);
        }
        maps = "\"" + maps;
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "startTrim: src: " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String size = String.valueOf(videoUris.size());
        String[] complexCommand = {"-filter_complex", maps + "concat=n=" + size + ":v=1:a=1 [vv] [aa]\"", "-map", "\"[vv]\"", "-map", "\"[aa]\"", filePath};
        String[] cmdf = new String[complexCommand.length + comand.length + 1];
        cmdf[0] = "-y";
        System.arraycopy(comand, 0, cmdf, 1, comand.length);
        System.arraycopy(complexCommand, 0, cmdf, comand.length + 1, complexCommand.length);
        String newCommand = comand(cmdf);
        Log.i(TAG, "NEW_EXPORT_COMMAND" + newCommand);
        executeComplexCommand(newCommand);
    }


    public String comand(String[] str) {
        String command = "";
        for (String var : str) {
            command = command + " " + var;
        }

        Log.i(TAG, "COMMAND : " + command);

        return command;
    }

    private void findIds() {
        rlSeekBar = findViewById(R.id.rlSeekBar);
        rlMainView = findViewById(R.id.rlParent);
        seekBar = findViewById(R.id.seekBar);
        rlAdd = findViewById(R.id.rlAdd);
        txtAudioTitle = findViewById(R.id.txtAudioTitle);
        rvViews = findViewById(R.id.rvViews);
        crdGallery = findViewById(R.id.crdGallery);
        videoView = findViewById(R.id.videoFrame);
        rlVideoFrame = findViewById(R.id.rlVideoFrame);
        crdDone = findViewById(R.id.crdDone);
        crdMusic = findViewById(R.id.crdMusic);
        crdDelete = findViewById(R.id.crdDelete);
        crdPlay = findViewById(R.id.crdPlay);
        crdPause = findViewById(R.id.crdPause);
        crdVolume = findViewById(R.id.crdVolume);
        rlSpeed = findViewById(R.id.rlSpeed);
        crdTrimmer = findViewById(R.id.crdTrimmer);
        rangeSeekBar = findViewById(R.id.rangeSeekBar);
        icStartTrimming = findViewById(R.id.icStartTrimming);
        cardView = findViewById(R.id.cardView);
        llSpeedInc = findViewById(R.id.llSpeedInc);
        llSpeedDec = findViewById(R.id.llSpeedDec);
        icTrim = findViewById(R.id.icTrim);
        icPreDefinedFilters = findViewById(R.id.icPreDefinedFilters);
        llExposure = findViewById(R.id.llExposure);
        llBrightness = findViewById(R.id.llBrightness);
        llTint = findViewById(R.id.llTint);
        llContrast = findViewById(R.id.llContrast);
        llWarmth = findViewById(R.id.llWarmth);
        llSaturation = findViewById(R.id.llSaturation);
        llPreDefinedFilters = findViewById(R.id.llPreDefinedFilters);
        llFilterOptions = findViewById(R.id.llFilterOptions);
        llBlacknWhite = findViewById(R.id.llBlacknWhite);
        llFilm = findViewById(R.id.llFilm);
        llVintage = findViewById(R.id.llVintage);
        llDuoTone = findViewById(R.id.llDuoTone);
        llGama = findViewById(R.id.llGama);
        llCrossProcess = findViewById(R.id.llCrossProcess);
        llFade = findViewById(R.id.llFade);
        icRotate = findViewById(R.id.icRotate);
        icSpeed = findViewById(R.id.icSpeed);
        icFilters = findViewById(R.id.icFilters);
        llRotate = findViewById(R.id.llRotate);
        crdTick = findViewById(R.id.crdTick);
        crdBack = findViewById(R.id.crdBack);
        llRotateRight = findViewById(R.id.llRotateRight);
        llNoneRotate = findViewById(R.id.llNoneRotate);
        llNormal = findViewById(R.id.llNormal);
        llNoneFilter = findViewById(R.id.llNoneFilter);
        icRotateLeft = findViewById(R.id.icRotateLeft);
        imgFrame = findViewById(R.id.imgFrame);

    }

    /**
     * Command for executing transition video
     */
    private void executeFadeInFadeOutCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "fade_video";
        String fileExtension = ".mp4";
        yourRealPath = UriUtils.getPathFromUri(VideoEditorActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "startTrim: src: " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        Toast.makeText(this, videoUris.get(selectedItemIndex).getDuration() + "", Toast.LENGTH_SHORT).show();
//        String[] complexCommand = {"-y", "-i", yourRealPath, "-acodec", "copy", "-vf", "fade=t=in:st=0:d=5,fade=t=out:st=" + (duration - 5) + ":d=5", filePath};
        String complexCommand = "-y " + "-i " + yourRealPath + " -acodec" + " copy" + " -vf" + " fade=t=in:st=0:d=2,fade=t=out:st=" + (videoUris.get(selectedItemIndex).getDuration() - 2) + ":d=2 " + filePath;

        try {

            executeComplexCommand(complexCommand);
        } catch (Exception ignored) {
            Log.i(TAG, ignored.toString());
        }

    }

    /**
     * Command for creating fast motion video
     */
    private void executeFastMotionVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "speed_video";
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }


        Log.i(TAG, "startTrim: src: " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String complexCommand = "-y " + "-i " + yourRealPath + " -filter_complex" + " [0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a] " + "-map " + "[v] " + "-map " + "[a] " + "-b:v " + "2097k " + "-r " + "60 " + "-vcodec " + "mpeg4 " + filePath;
        executeComplexCommand(complexCommand);
    }

    /**
     * Command for creating slow motion video
     */
    private void executeSlowMotionVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "slowMotion_video";
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }


        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String complexCommand = "-y " + "-i " + yourRealPath + " -filter_complex " + "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a] " + "-map " + "[v] " + "-map " + "[a] " + "-b:v " + "2097k " + "-r " + "60 " + "-vcodec " + "mpeg4 " + filePath;

        executeComplexCommand(complexCommand);

    }

    /**
     * Command for getting path
     */

    private void undoWithOlderVideo() {
        videoView.setVideoPath(olderPath);
        videoView.start();
        VideoUri videoUri2 = new VideoUri(olderPath, VideoEditorActivity.this);
        videoUris.set(selectedItemIndex, videoUri2);
        setAdapters();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void executeComplexCommand(String command) {
        FFmpegSession session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            removeColors();
            if (EDITOR_TYPE.equalsIgnoreCase("SCALING_VIDEO")) {
                MediaPlayer mediaPlayer = MediaPlayer.create(VideoEditorActivity.this, Uri.parse(filePath));
                Log.i(TAG, "VIDEO_HEIGHT : " + mediaPlayer.getVideoHeight() + " " + " VIDEO_WIDTH : " + mediaPlayer.getVideoWidth());
                if (mediaPlayer.getVideoWidth() != 1280 && mediaPlayer.getVideoHeight() != 720) {
                    Toast.makeText(this, "START SCALING AGAIN", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();
                }
            }
            Log.i(TAG, "SUCCESS with output : " + session.getAllLogsAsString());
            yourRealPath = afterEditFilePath.getAbsolutePath();
            Log.i(TAG, "Path After Execution getAbsolutePath : " + yourRealPath);
            Log.i(TAG, "Path After Execution PATH : " + afterEditFilePath.getPath());
            Log.i(TAG, "File Name : " + afterEditFilePath.getName());
            selectedVideoUri = Uri.fromFile(new File(filePath));
            VideoUri videoUri2 = new VideoUri(filePath, VideoEditorActivity.this);
            videoUris.set(selectedItemIndex, videoUri2);
            if (EDITOR_TYPE.equalsIgnoreCase("FINAL_VIDEO")) {
                EDITOR_TYPE = "";
                videoUris.clear();
                VideoUri videoUri = new VideoUri(filePath, VideoEditorActivity.this);
                videoUris.add(videoUri);
                selectedItemIndex = 0;
//                runOnUiThread(this::clearCache);
            }

            try {
                multipleViewsAdapter.notifyDataSetChanged();
                videoView.setVideoPath(filePath);
                imgFrame.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                txtAudioTitle.setText(new File(filePath).getName());
                videoView.start();
                onVideoCompleteListener();
                Toast.makeText(VideoEditorActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.i(TAG, "ERROR : " + e);
            }

        } else if (ReturnCode.isCancel(session.getReturnCode())) {

            CommonUtils.dismissDialog();
            File newFile = new File(filePath);
            newFile.delete();
            Log.i(TAG, "CANCELLED" + " " + session.getAllLogsAsString());
            Intent intent = new Intent(VideoEditorActivity.this, ErrorActivity.class);
            intent.putExtra("TAG", session.getAllLogsAsString());
            startActivity(intent);
        } else {
            CommonUtils.dismissDialog();
            Intent intent = new Intent(VideoEditorActivity.this, ErrorActivity.class);
            intent.putExtra("TAG", session.getAllLogsAsString());
            startActivity(intent);
            File newFile = new File(filePath);
            newFile.delete();
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

    /**
     * Command for rotating video
     */
    private void executeRotateVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "rotation_video";
        String fileExtension = ".mp4";
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String rotation = "rotate=" + mAnglePosition;
        String complexCommand = "-y" + " -i " + yourRealPath + " -c" + " copy" + " -metadata:s:v:0 " + rotation + " " + filePath;

        executeComplexCommand(complexCommand);

    }

    /**
     * Command for trimming video
     */
    private void executeTrimVideoCommand(int startMs, int endMs) {

        File moviesDir = new File(getApplicationInfo().dataDir);
//  

        String filePrefix = "rotation_video";
        String fileExtension = ".mp4";
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        @SuppressLint("DefaultLocale") String startHms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(startMs),
                TimeUnit.MILLISECONDS.toMinutes(startMs) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startMs)),
                TimeUnit.MILLISECONDS.toSeconds(startMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startMs)));
        @SuppressLint("DefaultLocale") String endHms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(endMs),
                TimeUnit.MILLISECONDS.toMinutes(endMs) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(endMs)),
                TimeUnit.MILLISECONDS.toSeconds(endMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endMs)));
        Log.i(TAG, "TIME_FORMAT_START : " + startHms);
        Log.i(TAG, "TIME_FORMAT_END : " + endHms);
        String complexCommand = "-y -i " + yourRealPath + " -ss " + startHms + " -to " + endHms + " -c:v copy -c:a copy " + filePath;
        Log.i(TAG, "TIME_FORMAT_COMMAND : " + complexCommand);

        executeComplexCommand(complexCommand);


    }
//    private void executeTrimVideoCommand(int startMs, int endMs) {
//
//        File moviesDir = new File(getApplicationInfo().dataDir);
//  
//
//        String filePrefix = "rotation_video";
//        String fileExtension = ".mp4";
//        File dest = new File(moviesDir, filePrefix + fileExtension);
//        int fileNo = 0;
//        while (dest.exists()) {
//            fileNo++;
//            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
//        }
//        Log.i(TAG, "yourRealPath " + yourRealPath);
//        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
//        filePath = dest.getAbsolutePath();
//        afterEditFilePath = dest;
////        String complexCommand = "-ss " + "" + startMs / 1000 + " -y" + " -i " + yourRealPath + " -t" + "" + (endMs - startMs) / 1000 + " -vcodec " + "mpeg4 " + "-b:v " + "2097152 " + "-b:a " + "48000 " + "-ac " + "2 " + "-ar " + "22050 " + filePath;
////        String complexCommand = "-y -i " + yourRealPath + " -ss " + startMs / 1000 + " -vcodec copy -acodec copy -t " + endMs / 1000 + " " + " -strict" + " -2 " + filePath;
//        @SuppressLint("DefaultLocale") String startHms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(startMs),
//                TimeUnit.MILLISECONDS.toMinutes(startMs) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(startMs)),
//                TimeUnit.MILLISECONDS.toSeconds(startMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startMs)));
//        @SuppressLint("DefaultLocale") String endHms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(endMs),
//                TimeUnit.MILLISECONDS.toMinutes(endMs) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(endMs)),
//                TimeUnit.MILLISECONDS.toSeconds(endMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endMs)));
//        Log.i(TAG, "TIME_FORMAT_START : " + startHms);
//        Log.i(TAG, "TIME_FORMAT_END : " + endHms);
//        String complexCommand = "-y -i " + yourRealPath + " -ss " + startHms + " -to " + endHms + " -c:v copy -c:a copy " + filePath;
//        Log.i(TAG, "TIME_FORMAT_COMMAND : " + complexCommand);
//
//        executeComplexCommand(complexCommand);
//
//
//    }

    private void executeCropVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "crop_video";
        String fileExtension = ".mp4";
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
//        String complexCommand = "-y " + "-i " + yourRealPath + " -filter:v " + "scale=2560:1440,crop=1280:720 " + "-c:a " + "copy " + filePath;
//        ADD crop=1280:720:x:y
//        x=start margin and y = top margin
        String complexCommand = "-y " + "-i " + yourRealPath + " -vf " + "scale=iw*2:-1,crop=1280:720 " + "-c:a " + "copy " + filePath;
        executeComplexCommand(complexCommand);


    }

    void setAdapters() {
        multipleViewsAdapter = new MultipleViewsAdapter(VideoEditorActivity.this, videoUris, VideoEditorActivity.this);
        rvViews.setAdapter(multipleViewsAdapter);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setClick(String uri, int index) {
        selectedItemIndex = index;
        yourRealPath = uri;
        mAnglePosition = 0;
        videoView.setVideoPath(uri);
        videoView.start();
        onVideoCompleteListener();
        MediaPlayer mediaPlayer = MediaPlayer.create(VideoEditorActivity.this, Uri.parse(yourRealPath));
        Log.i(TAG, "VIDEO_RESOLUTION " + "WIDTH : " + mediaPlayer.getVideoWidth() + " HEIGHT : " + mediaPlayer.getVideoHeight());
        String title = new File(yourRealPath).getName();
        txtAudioTitle.setText(title);
        setAdapters();
    }

    private void stopVideoAfterSpecificTime() {
        new Handler().postDelayed(() -> {
            videoView.pause();
            crdPlay.setVisibility(View.VISIBLE);
            crdPause.setVisibility(View.GONE);
        }, totalTime);
    }

    private void startMerging() {
        EDITOR_TYPE = "START_MERGING";

        File moviesDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "ARTalent");
        if (!moviesDir.isDirectory()) moviesDir.mkdir();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy-HH_mm_ss", Locale.getDefault()).format(new Date());
        String filePrefix = "ar-talent" + currentDate;
        String fileExtension = ".mp4";

        for (int i = 0; i < videoUris.size(); i++) {
            multiplePaths.add(i, videoUris.get(i).getVideoPaths());
            isAllTrue.add(i, false);
            Log.i(TAG, "MULTIPLE_PATHS : " + multiplePaths);
            Log.i(TAG, "MULTIPLE_BOOLEAN : " + isAllTrue.toString());
        }

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
    }

    private void scaleVideos() {

        File moviesDir = new File(getApplicationInfo().dataDir);

        String filePrefix = "ar-talent_scaling";
        String fileExtension = ".mp4";
        yourRealPath = UriUtils.getPathFromUri(VideoEditorActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String complexCommand = "-y " + "-i " + yourRealPath + " -vf " + "scale=w=1280:h=720:force_original_aspect_ratio=1,pad=1280:720:(ow-iw)/2:(oh-ih)/2  " + filePath;

//        String[] complexCommand = {"-y", "-i", yourRealPath, "-vf", "scale=w=1280:h=720:force_original_aspect_ratio=0,pad=1280:720:(ow-iw)/2:(oh-ih)/2", filePath};
//        String[] complexCommand = {"-y", "-i", yourRealPath, "-vf", "scale=w=1280:h=720:force_original_aspect_ratio=0,pad=1280:720:(1280-854)/2:(720.9-480)/2", filePath};

        executeComplexCommand(complexCommand);


    }

    private void cropVideoOnImport() {

//        File moviesDir = new File(getApplicationInfo().dataDir);
        File moviesDir = new File(getApplicationInfo().dataDir);

        String filePrefix = "ar-talent_imported";
        String fileExtension = ".mp4";
        yourRealPath = UriUtils.getPathFromUri(VideoEditorActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String[] complexCommand = {"-y", "-i", yourRealPath, "-vf", "scale=w=1280:h=720:force_original_aspect_ratio=1,pad=1280:720:(ow-iw)/2:(oh-ih)/2", filePath};
//                 executeComplexCommand(complexCommand);

    }

    void clearCache() {

//        File cacheDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "ARTalent" + "/" + ".cache");
        File cacheDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "ARTalent");

        if (!cacheDir.isDirectory()) {
            try {
                cacheDir.mkdir();
                Log.i(TAG, "CACHE DIRECTORY CREATED");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                String[] children = cacheDir.list();
                for (int i = 0; Objects.requireNonNull(children).length > i; i++) {
                    new File(cacheDir, children[i]).delete();
                }
                Log.i(TAG, "CACHE CLEARED");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void merging() {
        isAllTrue.clear();
        for (int i = 0; i < videoUris.size(); i++) {
            isAllTrue.add(false);
        }
        for (int i = 0; i < videoUris.size(); i++
        ) {
            try {
                Thread t = new Thread(new ThreadsInActivity(i, VideoEditorActivity.this, videoUris.get(i).getVideoPaths()));
                t.start();

            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, e.toString());
            }

        }
    }


    private void executeBlackAndWhiteVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "black_and_white_video";
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String complexCommand = "-y -i " + yourRealPath + " -vf hue=s=0 " + filePath;
        executeComplexCommand(complexCommand);
    }

    private void executeVintageVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "vintage_video";
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }


        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;

//        String complexCommand = "-y -i " + yourRealPath + " -vf hue=s=0 " + filePath;
        String complexCommand = "-y -i " + yourRealPath + " -vf curves=vintage " + filePath;
        executeComplexCommand(complexCommand);
    }

    private void executeGammaVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "gamma_video";
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }


        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;

//        ffmpeg -i INPUT.MOV  OUTPUT.MOV
//        ffmpeg -i in.mp4 -vf "eq=gamma=0.5" out.mp4
        String complexCommand = "-y -i " + yourRealPath + " -vf curves=preset=cross_process " + filePath;
        executeComplexCommand(complexCommand);
    }

    private void executeCrossProcessVideoCommand() {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "cross_process_video";
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;
//        ffmpeg -i INPUT.MOV  OUTPUT.MOV
//        ffmpeg -i in.mp4 -vf "eq=gamma=0.5" out.mp4
        String complexCommand = "-y -i " + yourRealPath + " -vf curves=preset=cross_process " + filePath;
        executeComplexCommand(complexCommand);
    }

    private void executeAudioCommand(String volumeLevel) {
        File moviesDir = new File(getApplicationInfo().dataDir);


        String filePrefix = "increase_audio";
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }


        Log.i(TAG, "yourRealPath " + yourRealPath);
        Log.i(TAG, "yourRealPath  : " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        afterEditFilePath = dest;

//        ffmpeg -i INPUT.MOV  OUTPUT.MOV
//        ffmpeg -i input.mkv -filter:a "volume=4.0" output.mkv
        String complexCommand = "-y -i " + yourRealPath + " -filter:a \"volume=" + volumeLevel + "\" " + filePath;
        executeComplexCommand(complexCommand);
    }

    private void removeColors() {
        icRotate.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
        icPreDefinedFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
        icTrim.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
        icSpeed.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));
        icFilters.setColorFilter(ContextCompat.getColor(VideoEditorActivity.this, R.color.text_color));

    }

}
