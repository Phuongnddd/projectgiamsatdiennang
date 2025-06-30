package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText user, pass;
    private RelativeLayout dangnhap, dangki;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Liên kết các view
        user = findViewById(R.id.user);
        pass = findViewById(R.id.pass);
        dangnhap = findViewById(R.id.btl);
        dangki = findViewById(R.id.dku);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Xử lý sự kiện khi nhấn nút đăng nhập
        dangnhap.setOnClickListener(v -> login());

        // Xử lý sự kiện khi nhấn nút đăng ký
        dangki.setOnClickListener(v -> resigher());
    }

    // Chuyển sang màn hình đăng ký
    private void resigher()
    {
        Intent i = new Intent(MainActivity.this, dangki.class);
        startActivity(i);
    }

    // Xử lý đăng nhập
    private void login() {
        String email, password;

        // Lấy thông tin từ EditText
        email = user.getText().toString().trim();
        password = pass.getText().toString().trim();

        // Kiểm tra nếu email hoặc password trống
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng nhập với Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MAINCHINH.class); // HomeActivity là màn hình chính sau khi đăng nhập
                        startActivity(intent);
                        finish();
                    } else {
                        // Đăng nhập thất bại
                        Toast.makeText(MainActivity.this, "Đăng nhập thất bại! Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
