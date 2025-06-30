package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DothiphongkhachActivity extends AppCompatActivity {

    EditText mot, hai, ba, bon, nam, sau;
    TextView textTienDien;
    Button btnLuu;
    BarChart barChart;
    float kwh = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dothiphongkhach);

        mot = findViewById(R.id.mot);
        hai = findViewById(R.id.hai);
        ba = findViewById(R.id.ba);
        bon = findViewById(R.id.bon);
        nam = findViewById(R.id.nam);
        sau = findViewById(R.id.sau);
        textTienDien = findViewById(R.id.textView13);
        btnLuu = findViewById(R.id.button2);
        barChart = findViewById(R.id.barChart);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Phong_khach/diennang");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float diennangWh = snapshot.getValue(Float.class);
                if (diennangWh != null) {
                    kwh = diennangWh / 1000f;

                    // Lưu theo ngày
                    DatabaseReference dailyRef = FirebaseDatabase.getInstance()
                            .getReference("Phong_khach/diennang_ngay/" + currentDate);
                    dailyRef.setValue(diennangWh); // Lưu đơn vị Wh để hiển thị sau

                    // Sau khi lưu, vẽ biểu đồ
                    veBieuDoCot();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textTienDien.setText("Không lấy được dữ liệu từ Firebase");
            }
        });

        btnLuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    float m1 = Float.parseFloat(mot.getText().toString());
                    float m2 = Float.parseFloat(hai.getText().toString());
                    float m3 = Float.parseFloat(ba.getText().toString());
                    float m4 = Float.parseFloat(bon.getText().toString());
                    float m5 = Float.parseFloat(nam.getText().toString());
                    float m6 = Float.parseFloat(sau.getText().toString());

                    float tong = tinhTienDien(kwh, m1, m2, m3, m4, m5, m6);
                    textTienDien.setText("Tổng số tiền điện là: " + tong + " VNĐ");
                } catch (Exception e) {
                    textTienDien.setText("Vui lòng nhập đầy đủ các mức giá.");
                }
            }
        });
    }

    private float tinhTienDien(float kwh, float m1, float m2, float m3, float m4, float m5, float m6) {
        float tong = 0f;

        if (kwh <= 50) {
            tong = kwh * m1;
        } else if (kwh <= 100) {
            tong = 50 * m1 + (kwh - 50) * m2;
        } else if (kwh <= 200) {
            tong = 50 * m1 + 50 * m2 + (kwh - 100) * m3;
        } else if (kwh <= 300) {
            tong = 50 * m1 + 50 * m2 + 100 * m3 + (kwh - 200) * m4;
        } else if (kwh <= 400) {
            tong = 50 * m1 + 50 * m2 + 100 * m3 + 100 * m4 + (kwh - 300) * m5;
        } else {
            tong = 50 * m1 + 50 * m2 + 100 * m3 + 100 * m4 + 100 * m5 + (kwh - 400) * m6;
        }

        return tong;
    }

    private void veBieuDoCot() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Phong_khach/diennang_ngay");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();

                int index = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String ngay = child.getKey();
                    Float wh = child.getValue(Float.class);
                    if (wh != null) {
                        entries.add(new BarEntry(index, wh / 1000f)); // chuyển sang kWh
                        labels.add(ngay);
                        index++;
                    }
                }

                BarDataSet dataSet = new BarDataSet(entries, "Tổng điện năng mỗi ngày (kWh)");
                BarData barData = new BarData(dataSet);
                barChart.setData(barData);

                Description desc = new Description();
                desc.setText("Biểu đồ điện năng theo ngày");
                barChart.setDescription(desc);
                barChart.animateY(1000);
                barChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textTienDien.setText("Không thể tải biểu đồ.");
            }
        });
    }
}
