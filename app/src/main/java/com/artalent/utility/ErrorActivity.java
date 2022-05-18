package com.artalent.utility;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.artalent.R;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        TextView textView = findViewById(R.id.txtError);
        String strError = "ERROR : \n" + getIntent().getStringExtra("TAG");
        textView.setText(strError);
    }
}