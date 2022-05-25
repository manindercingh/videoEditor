package com.artalent.utility;


import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.artalent.R;

import java.io.File;

public class CommonUtils {

    private static AlertDialog alertDialog;

    public static boolean isNetworkConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;

    }

    public static void showDialog(Activity activity) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        View mView = activity.getLayoutInflater().inflate(R.layout.layout_progress_dialog, null);
        alert.setView(mView);
        alertDialog = alert.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public static void dismissDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    public static void downloadAudio(Context context, String url, String outputName) {
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

// get download service and enqueue file
        DownloadManager manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        long abc = manager.enqueue(request);

        if (abc != 0) {
            Toast.makeText(context, "download started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "no download started", Toast.LENGTH_SHORT).show();
        }
//        addWatermark(context, url);
    }
}
