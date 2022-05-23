package com.artalent.models;

public class MusicItemModel {
    public String audioTitle;
    public int id;

    public MusicItemModel(String audioTitle, int id) {
        this.audioTitle = audioTitle;
        this.id = id;
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String audioTitle) {
        this.audioTitle = audioTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
