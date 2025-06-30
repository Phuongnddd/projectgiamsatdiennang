package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PhongkhachActivity extends AppCompatActivity {

    PieChart pieChart1, pieChart2, pieChart3, pieChart4;
    TextView textVoltage, textCurrent, textPower, textEnergy;
    ImageView imgDen, imgQuat;
    Button buttonChuyenTrang;
    DatabaseReference dbRef;
    boolean isDenBat = false;
    boolean isQuatBat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phongkhach);

        // Liên kết PieChart và TextView
        pieChart1 = findViewById(R.id.pieChart);
        pieChart2 = findViewById(R.id.pieChart2);
        pieChart3 = findViewById(R.id.pieChart3);
        pieChart4 = findViewById(R.id.pieChart4);

        textVoltage = findViewById(R.id.textView6);
        textCurrent = findViewById(R.id.textView8);
        textPower   = findViewById(R.id.textView12);
        textEnergy  = findViewById(R.id.textView60);

        // Liên kết hình ảnh điều khiển và nút
        imgDen = findViewById(R.id.img1);
        imgQuat = findViewById(R.id.img2);
        buttonChuyenTrang = findViewById(R.id.button);

        // Kết nối Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("Phong_khach");

        // Lắng nghe thay đổi dữ liệu
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float dienap = snapshot.child("dienap").getValue(Float.class);
                Float dongdien = snapshot.child("dongdien").getValue(Float.class);
                Float congsuat = snapshot.child("congxuat").getValue(Float.class);
                Float diennang = snapshot.child("diennang").getValue(Float.class);

                if (dienap != null) {
                    setupVoltageChart(pieChart1, dienap);
                    textVoltage.setText(String.format("%.1fV", dienap));
                }

                if (dongdien != null) {
                    setupCurrentChart(pieChart2, dongdien);
                    textCurrent.setText(String.format("%.1fA", dongdien));
                }

                if (congsuat != null) {
                    setupPowerChart(pieChart3, congsuat);
                    textPower.setText(String.format("%.0fW", congsuat));
                }

                if (diennang != null) {
                    setupEnergyChart(pieChart4, diennang);
                    textEnergy.setText(String.format("%.0fWh", diennang));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle lỗi nếu cần
            }
        });

        // Sự kiện bật/tắt đèn
        imgDen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDenBat = !isDenBat;
                imgDen.setImageResource(isDenBat ? R.drawable.desang : R.drawable.dentat);
                dbRef.child("den").setValue(isDenBat ? 1 : 0);
            }
        });

        // Sự kiện bật/tắt quạt
        imgQuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isQuatBat = !isQuatBat;
                imgQuat.setImageResource(isQuatBat ? R.drawable.fanbat : R.drawable.fantat);
                dbRef.child("quat").setValue(isQuatBat ? 1 : 0);
            }
        });

        // Sự kiện chuyển sang trang khác
        buttonChuyenTrang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhongkhachActivity.this, DothiphongkhachActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupVoltageChart(PieChart pieChart, float value) {
        drawPieChart(pieChart, value, 270f);
    }

    private void setupCurrentChart(PieChart pieChart, float value) {
        drawPieChart(pieChart, value, 35f);
    }

    private void setupPowerChart(PieChart pieChart, float value) {
        drawPieChart(pieChart, value, 1000f);
    }

    private void setupEnergyChart(PieChart pieChart, float value) {
        drawPieChart(pieChart, value, 1000f);
    }

    private void drawPieChart(PieChart pieChart, float value, float maxValue) {
        float percent = (value / maxValue) * 100f;
        if (percent > 100f) percent = 100f;

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(percent));
        entries.add(new PieEntry(100f - percent));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.YELLOW, Color.GRAY);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(0f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.setUsePercentValues(false);
        pieChart.setRotationAngle(135f);
        pieChart.setMaxAngle(270f);
        pieChart.setHoleRadius(80f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setRotationEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.invalidate();
    }
}
