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


}
