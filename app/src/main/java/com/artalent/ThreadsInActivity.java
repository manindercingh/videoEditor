package com.artalent;

import static android.content.ContentValues.TAG;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

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
    MainActivity mainActivity;
    File afterEditFilePath;
    boolean allTrue;
    String filepath;

    ThreadsInActivity(int index, MainActivity mainActivity, String paths) {
        this.index = index;
        this.paths = paths;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Log.i(MainActivity.TAG, "CURRENT_THREAD : " + Thread.currentThread());
        runScaleCommand();
    }

    public synchronized void changeListValues(int index) {
        Log.i(MainActivity.TAG, "called");
        st++;
        MainActivity.isAllTrue.size();
        MainActivity.isAllTrue.set(index, true);
        looper:for (int i = 0; MainActivity.isAllTrue.size() > i; i++) {
            if (!MainActivity.isAllTrue.get(i)) {
                allTrue = false;
                break looper;
            } else {
                allTrue = true;
            }

        }
        if (allTrue) {

            Log.i(MainActivity.TAG, "ON_MERGING RESOLUTIONS" + getResolution() + " PATH : " + list() + " " + Thread.currentThread());


            mainActivity.mergeAndExportVideos();

            Log.i(MainActivity.TAG, "IF_STATEMENT : " + MainActivity.isAllTrue.toString());
            Log.i(MainActivity.TAG, "AFTER_SCALING_PATHS : " + list());

        } else {
            Log.i(MainActivity.TAG, " ELSE : " + MainActivity.isAllTrue + " val " + st);
        }

    }

    void runScaleCommand() {
        File moviesDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "ARTalent" + "/" + ".cache");
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
        executeComplexCommand(complexCommand);
    }

    public void executeComplexCommand(String command) {

        FFmpegSession session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            MainActivity.yourRealPath = afterEditFilePath.getAbsolutePath();
            selectedVideoUri = Uri.fromFile(new File(filepath));
            VideoUri videoUri2 = new VideoUri(filepath, mainActivity);
            MainActivity.videoUris.set(index, videoUri2);
            MediaPlayer mediaPlayer = MediaPlayer.create(mainActivity, Uri.parse(MainActivity.videoUris.get(index).getVideoPaths()));
            if (mediaPlayer.getVideoWidth() == 1280 && mediaPlayer.getVideoHeight() == 720) {
                changeListValues(index);
                Log.i(TAG, "changeListValues" + mediaPlayer.getVideoHeight() + " " + mediaPlayer.getVideoWidth());
            } else {
                Log.i(TAG, "RE_RUN_SCALE_COMMAND" + mediaPlayer.getVideoHeight() + " " + mediaPlayer.getVideoWidth());
                paths = MainActivity.videoUris.get(index).getVideoPaths();
                runScaleCommand();
            }

            Log.i(MainActivity.TAG, "ACTIVITY_FILEPATH" + filepath + " t " + Thread.currentThread());
            Log.i(MainActivity.TAG, "CURRENT_THREAD");

        } else if (ReturnCode.isCancel(session.getReturnCode())) {


            File newFile = new File(filepath);
            newFile.delete();
            Log.i(MainActivity.TAG, "Cancel" + " " + session.getReturnCode());
        } else {

            File newFile = new File(filepath);
            newFile.delete();

            Log.i(MainActivity.TAG, "Failed" + " " + session.getReturnCode());
            Log.d(TAG, String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));

        }

    }

    private String list() {
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < MainActivity.videoUris.size(); i++) {

            values.append(" , ").append(MainActivity.videoUris.get(i).getVideoPaths());
        }
        return values.toString();
    }

    private String getResolution() {
        StringBuilder values = new StringBuilder();

        for (int i = 0; i < MainActivity.videoUris.size(); i++) {
            MediaPlayer mp = MediaPlayer.create(mainActivity, Uri.parse(MainActivity.videoUris.get(i).getVideoPaths()));
            values.append(" , HEIGHT ").append(mp.getVideoHeight()).append("WIDTH").append(mp.getVideoWidth());
            Log.i(MainActivity.TAG, "RESOLUTION_HEIGHT : " + mp.getVideoHeight() + " RESOLUTION_WIDTH : " + mp.getVideoWidth());

        }
        return values.toString();
    }


}
