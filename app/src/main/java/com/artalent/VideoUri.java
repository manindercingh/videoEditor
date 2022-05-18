package com.artalent;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.File;
import java.util.List;

public class VideoUri {
    private String videoPaths;
    private int duration;
    private Context requireContext;

    public VideoUri(String videoPaths, Context requireContext) {
        this.videoPaths = videoPaths;
        this.requireContext = requireContext;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(requireContext, Uri.fromFile(new File(videoPaths)));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Integer.parseInt(time);

    }

    public String getVideoPaths() {
        return videoPaths;
    }


    public int getDuration() {
        return duration;
    }


    public String uris(List<VideoUri> list) {
        String s = "";
        for (VideoUri video : list
        ) {
            s = s + "\n" + video.getVideoPaths();
        }
        return s;
    }

    public int totalDuration(List<VideoUri> list) {
        int d = 0;
        for (VideoUri video : list
        ) {
            d = d + video.getDuration();
        }
        return d;
    }
}
