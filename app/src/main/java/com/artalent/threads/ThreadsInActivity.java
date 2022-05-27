package com.artalent.threads;

import static android.content.ContentValues.TAG;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.artalent.activities.VideoEditorActivity;
import com.artalent.models.VideoUri;
import com.artalent.utility.CommonUtils;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.util.Random;

public class ThreadsInActivity implements Runnable {

    int index;
    String paths;
    Uri selectedVideoUri;
    int st = 0;
    VideoEditorActivity mainActivity;
    File afterEditFilePath;
    boolean allTrue;
    String filepath;

    public ThreadsInActivity(int index, VideoEditorActivity mainActivity, String paths) {
        this.index = index;
        this.paths = paths;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Log.i(VideoEditorActivity.TAG, "CURRENT_THREAD : " + Thread.currentThread());
        runScaleCommand();
    }

    public synchronized void changeListValues(int index) {
        Log.i(VideoEditorActivity.TAG, "called");
        st++;
        VideoEditorActivity.isAllTrue.size();
        VideoEditorActivity.isAllTrue.set(index, true);
        looper:for (int i = 0; VideoEditorActivity.isAllTrue.size() > i; i++) {
            if (!VideoEditorActivity.isAllTrue.get(i)) {
                allTrue = false;
                break looper;
            } else {
                allTrue = true;
            }

        }
        if (allTrue) {

            Log.i(VideoEditorActivity.TAG, "ON_MERGING RESOLUTIONS" + getResolution() + " PATH : " + list() + " " + Thread.currentThread());


            mainActivity.mergeAndExportVideos();

            Log.i(VideoEditorActivity.TAG, "IF_STATEMENT : " + VideoEditorActivity.isAllTrue.toString());
            Log.i(VideoEditorActivity.TAG, "AFTER_SCALING_PATHS : " + list());

        } else {
            Log.i(VideoEditorActivity.TAG, " ELSE : " + VideoEditorActivity.isAllTrue + " val " + st);
        }

    }

    void runScaleCommand() {
        File moviesDir = new File(mainActivity.getApplicationInfo().dataDir);
        if (!moviesDir.isDirectory()) moviesDir.mkdirs();

        Random random = new Random();
        int randomNumber = random.nextInt(80 - 21) + 65;
        String filePrefix = "scaled_video_random" + randomNumber;
        String fileExtension = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = index;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }

        filepath = dest.getAbsolutePath();
        afterEditFilePath = dest;
        String complexCommand = "-y " + "-i " + paths + " -vf " + "scale=w=1280:h=720:force_original_aspect_ratio=1,pad=1280:720:(ow-iw)/2:(oh-ih)/2 " + filepath;
//        String complexCommand = "-y " + "-i " + paths + " -vf " + "scale=w=1280:h=720:force_original_aspect_ratio=1,pad=1280:720:(ow-iw)/2:(oh-ih)/2 -b:v 1M -crf 24 " + filepath;
       runExecution(complexCommand);
    }

    public void executeComplexCommand(String command) {

        FFmpegSession session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            CommonUtils.dismissDialog();

            VideoEditorActivity.yourRealPath = afterEditFilePath.getAbsolutePath();
            selectedVideoUri = Uri.fromFile(new File(filepath));
            VideoUri videoUri2 = new VideoUri(filepath, mainActivity);
            VideoEditorActivity.videoUris.set(index, videoUri2);
            MediaPlayer mediaPlayer = MediaPlayer.create(mainActivity, Uri.parse(VideoEditorActivity.videoUris.get(index).getVideoPaths()));
            if (mediaPlayer.getVideoWidth() == 1280 && mediaPlayer.getVideoHeight() == 720) {
                changeListValues(index);
                Log.i(TAG, "changeListValues" + mediaPlayer.getVideoHeight() + " " + mediaPlayer.getVideoWidth());
            } else {
                Log.i(TAG, "RE_RUN_SCALE_COMMAND" + mediaPlayer.getVideoHeight() + " " + mediaPlayer.getVideoWidth());
                paths = VideoEditorActivity.videoUris.get(index).getVideoPaths();
                runScaleCommand();
            }

            Log.i(VideoEditorActivity.TAG, "ACTIVITY_FILEPATH" + filepath + " t " + Thread.currentThread());
            Log.i(VideoEditorActivity.TAG, "CURRENT_THREAD");

        } else if (ReturnCode.isCancel(session.getReturnCode())) {


            File newFile = new File(filepath);
            newFile.delete();
            Log.i(VideoEditorActivity.TAG, "Cancel" + " " + session.getReturnCode());
        } else {

            File newFile = new File(filepath);
            newFile.delete();

            Log.i(VideoEditorActivity.TAG, "Failed" + " " + session.getReturnCode());
            Log.d(TAG, String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));

        }

    }

    private String list() {
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < VideoEditorActivity.videoUris.size(); i++) {

            values.append(" , ").append(VideoEditorActivity.videoUris.get(i).getVideoPaths());
        }
        return values.toString();
    }

    private String getResolution() {
        StringBuilder values = new StringBuilder();

        for (int i = 0; i < VideoEditorActivity.videoUris.size(); i++) {
            MediaPlayer mp = MediaPlayer.create(mainActivity, Uri.parse(VideoEditorActivity.videoUris.get(i).getVideoPaths()));
            values.append(" , HEIGHT ").append(mp.getVideoHeight()).append("WIDTH").append(mp.getVideoWidth());
            Log.i(VideoEditorActivity.TAG, "RESOLUTION_HEIGHT : " + mp.getVideoHeight() + " RESOLUTION_WIDTH : " + mp.getVideoWidth());

        }
        return values.toString();
    }

    public void runExecution(String complexCommand){

        new Thread() {
            @Override
            public void run() {

                //Do long operation stuff here search stuff

                try {

                    // code runs in a thread
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CommonUtils.showDialog(mainActivity);

                        }
                    });
                }
                catch (final Exception ex) {

                }
                finally {
                    executeComplexCommand(complexCommand);

                }
            }
        }.start();

    }

}
