package com.artalent.utility;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import java.io.File;

public class AWSUtils {
    OnAwsImageUploadListener onAwsImageUploadListener;
    String filePath, filePathKey = "upload/";
    private File image = null;
    private TransferUtility mTransferUtility = null;
    private AmazonS3Client sS3Client = null;
    private CognitoCachingCredentialsProvider sCredProvider = null;
    private Context context;

    public AWSUtils(String filePath, Context context, OnAwsImageUploadListener onAwsImageUploadListener) {
        this.onAwsImageUploadListener = onAwsImageUploadListener;
        this.filePath = filePath;
        this.context = context;
    }

    private AmazonS3Client getsS3Client() {
        if (sS3Client == null) {
//            sS3Client = new AmazonS3Client(getCredProvider(context));
//            sS3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1));

            sS3Client = new AmazonS3Client(new BasicAWSCredentials(AwsConstants.MY_ACCESS_KEY_ID,
                    AwsConstants.MY_SECRET_KEY));
        }
        return sS3Client;
    }

    private TransferUtility getTransferUtility(Context context) {
        if (mTransferUtility == null) {
            mTransferUtility = new TransferUtility(
                    getsS3Client(),
                    context.getApplicationContext()
            );
        }
        return mTransferUtility;
    }

    public void beginUpload() {

        if (TextUtils.isEmpty(filePath)) {
            onAwsImageUploadListener.onError("Could not find the filepath of the selected file");
        }

        onAwsImageUploadListener.showProgressDialog();
        File file = new File(filePath);
        image = file;


        try {
            TransferNetworkLossHandler.getInstance(context);
            TransferObserver observer = getTransferUtility(context).upload(
                    AwsConstants.BUCKET_NAME, //Bucket name
                    filePathKey + file.getName(), image //File name with folder path
            );
            observer.setTransferListener(new UploadListener());
        } catch (Exception e) {
            e.printStackTrace();
            onAwsImageUploadListener.hideProgressDialog();
        }
    }

    private String generateS3SignedUrl(String path) {
        File mFile = new File(path);
        AmazonS3Client s3client = getsS3Client();
        ResponseHeaderOverrides overrideHeader = null;
        overrideHeader.setContentType("audio/mp3");
        String mediaUrl = mFile.getName();

        GeneratePresignedUrlRequest generatePreSignedUrlRequest = new
                GeneratePresignedUrlRequest(AwsConstants.BUCKET_NAME, filePathKey + mediaUrl);
        generatePreSignedUrlRequest.setMethod(HttpMethod.GET);
        generatePreSignedUrlRequest.setResponseHeaders(overrideHeader);
        return s3client.generatePresignedUrl(generatePreSignedUrlRequest).toString();
    }

    public interface OnAwsImageUploadListener {
        void showProgressDialog();

        void hideProgressDialog();

        void onSuccess(String imgUrl);

        void onError(String errorMsg);
    }

    private class UploadListener implements TransferListener {

        @Override
        public void onStateChanged(int id, TransferState state) {

            if (state == TransferState.COMPLETED) {
                onAwsImageUploadListener.hideProgressDialog();

                String finalImageUrl = AwsConstants.S3_URL + filePathKey + image.getName();

                Toast.makeText(context, "File Uploaded Successfully ", Toast.LENGTH_SHORT).show();
//                Toast.makeText(context, "" + finalImageUrl, Toast.LENGTH_SHORT).show();
//                onAwsImageUploadListener.onSuccess(generateS3SignedUrl(finalImageUrl));
            } else if (state == TransferState.CANCELED || state == TransferState.FAILED) {
                onAwsImageUploadListener.hideProgressDialog();
                onAwsImageUploadListener.onError("Error in uploading file.");
            }

        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

        }

        @Override
        public void onError(int id, Exception ex) {
            onAwsImageUploadListener.hideProgressDialog();
        }
    }
}
