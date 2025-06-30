package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MAINCHINH extends AppCompatActivity {
    private RelativeLayout kv1, kv2, kv3, kv4;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainchinh);

        kv1 = findViewById(R.id.a);
        kv2 = findViewById(R.id.b);
        kv3 = findViewById(R.id.c);
        kv4 = findViewById(R.id.d);

        kv1.setOnClickListener(v -> {
            Intent x = new Intent(MAINCHINH.this, PhongkhachActivity.class);
            startActivity(x);
        });

        kv2.setOnClickListener(v -> {
            Intent x = new Intent(MAINCHINH.this, PhongbepActivity.class);
            startActivity(x);
        });

        kv3.setOnClickListener(v -> {
            Intent x = new Intent(MAINCHINH.this, MainActivity.class);
            startActivity(x);
        });

        kv4.setOnClickListener(v -> {
            Intent x = new Intent(MAINCHINH.this, PhongnguActivity.class);
            startActivity(x);
        });

        // Áp dụng window insets để tránh status bar / navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
