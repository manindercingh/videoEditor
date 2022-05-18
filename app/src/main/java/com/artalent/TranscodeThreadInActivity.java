package com.artalent;

import android.os.Environment;
import android.util.Log;

import com.artalent.utility.CommonUtils;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TranscodeThreadInActivity implements Runnable {

    String filePath;
    MainActivity mainActivity;
    boolean allTrue = true;
    int index;
    List<String> tsPaths = new ArrayList<>();

    public TranscodeThreadInActivity(MainActivity mainActivity, int index) {
        this.mainActivity = mainActivity;
        this.index = index;
    }

    @Override
    public void run() {
        convertTS();
    }

    public synchronized void changeListValues(int index) {
        Log.i(MainActivity.TAG, "called");
        MainActivity.isAllTSTrue.size();
        MainActivity.isAllTSTrue.set(index, true);
        for (int i = 0; MainActivity.isAllTSTrue.size() > i; i++
        ) {
            if (!MainActivity.isAllTSTrue.get(i)) {
                allTrue = false;
            } else {
                allTrue = true;
            }

        }
        if (allTrue) {
            new Thread() {
                public void run() {
                    mainActivity.mergeAndExportVideos();
                }
            }.start();
            Log.i(MainActivity.TAG, "IF_STATEMENT : " + MainActivity.isAllTSTrue.toString());
        } else {
            Log.i(MainActivity.TAG, " ELSE : " + MainActivity.isAllTSTrue);
        }

    }


    public void convertTS() {
        File moviesDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "Ar-Talent" + "/" + ".cache");
        if (!moviesDir.isDirectory()) moviesDir.mkdir();

        String currentDate = new SimpleDateFormat("dd-MM-yyyy-HH_mm_ss", Locale.getDefault()).format(new Date());
        String filePrefix = "temp_f" + currentDate;
        String fileExtension = ".ts";
        File dest = new File(moviesDir, filePrefix + fileExtension);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtension);
        }
        filePath = dest.getAbsolutePath();
        String complexCommand = "-y -i " + " " + MainActivity.videoUris.get(index).getVideoPaths() + " -c" + " copy" + " -bsf:v h264_mp4toannexb -f mpegts " + filePath;
//        String com = "-y -i " + MainActivity.videoUris.get(index).getVideoPaths() + " -c copy -bsf:v h264_mp4toannexb -f mpegts " + filePath;
//        exec('ffmpeg -i abc.mp4 -c:v libx264 -c:a aac -b:a 160k -bsf:v h264_mp4toannexb -f mpegts -crf 32 pqr.ts');
        String com = "-y -i " + MainActivity.videoUris.get(index).getVideoPaths() + " -c copy -bsf:v h264_mp4toannexb -f mpegts " + filePath;
        Log.i(MainActivity.TAG, "NEW_EXPORT_COMMAND_IN_THREAD : " + complexCommand);
        executeTS(com);
    }

    public void executeTS(String command) {

        FFmpegSession session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {

            try {
                CommonUtils.dismissDialog();
//            tsPaths.add(index, filePath);
                Log.i(MainActivity.TAG, "SUCCESS" + " " + session.getAllLogsAsString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            CommonUtils.dismissDialog();
            File newFile = new File(filePath);
            newFile.delete();
            Log.i(MainActivity.TAG, "CANCELLED" + " " + session.getAllLogsAsString());
        } else {
            File newFile = new File(filePath);
            newFile.delete();
            CommonUtils.dismissDialog();
            Log.i(MainActivity.TAG, "Failed" + " " + session.getAllLogsAsString());
            Log.d(MainActivity.TAG, String.format("Command failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));

        }

//        FFmpeg.executeAsync(command, (executionId, returnCode) -> {
//
//            if (returnCode == RETURN_CODE_SUCCESS) {
//                CommonUtils.dismissDialog();
//                Log.i(TAG, "SUCCESS");
//                Log.d(TAG, "SUCCESS with output : " + returnCode);
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
//                CommonUtils.dismissDialog();
//                File newFile = new File(filePath);
//                newFile.delete();
//                Log.i(TAG, "Failed" + " " + returnCode);
//            } else {
//
//                File newFile = new File(filePath);
//                newFile.delete();
//                CommonUtils.dismissDialog();
//                Log.i(TAG, "Failed" + " " + returnCode);
//            }
//
//        });

    }


}
