package com.artalent.utility;

import com.amazonaws.regions.Regions;

import java.io.File;

public class AwsConstants {
    public static String FINAL_MUSIC_NAME_CACHE = "";
    public static boolean iS_TRIMMED_AUDIO_IMPORTED = false;
    //    public static String FINAL_MUSIC_NAME_CACHE = "";
    public static File FINAL_FILE_CACHE;
    public static String COGNITO_IDENTITY_ID = "ap-south-1:03175e9c-3209-422d-bff8-e714aedc2e74";
    public static Regions COGNITO_REGION = Regions.AP_SOUTH_1;
    public static String BUCKET_NAME = "images123456";
    public static String MY_ACCESS_KEY_ID = "AKIAQ43UDM6HMGPHY7C6";
    public static String MY_SECRET_KEY = "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";
    public static String S3_URL = "https://" + BUCKET_NAME + ".s3.ap-south-1.amazonaws.com";
    public static int VIDEO_LENGTH = 0;
}
