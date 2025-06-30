package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class dangki extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private RelativeLayout btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangki);

        // Ánh xạ view
        edtFullName = findViewById(R.id.hvt);
        edtEmail = findViewById(R.id.userdk);
        edtPassword = findViewById(R.id.passdk);
        edtConfirmPassword = findViewById(R.id.passdk2);
        btnRegister = findViewById(R.id.dk2);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Xử lý khi nhấn nút Đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = edtFullName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String confirmPassword = edtConfirmPassword.getText().toString().trim();

                // Kiểm tra các trường nhập liệu
                if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(dangki.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(dangki.this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Đăng ký với Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(dangki.this, task -> {
                            if (task.isSuccessful()) {
                                // Đăng ký thành công, lưu thông tin vào Firebase Realtime Database
                                String userId = mAuth.getCurrentUser().getUid(); // Lấy UID của người dùng

                                // Tạo đối tượng để lưu thông tin người dùng
                                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users");

                                // Tạo một đối tượng Map để lưu trữ thông tin người dùng
                                User user = new User(fullName, email);

                                // Lưu thông tin người dùng vào Firebase Database
                                databaseRef.child(userId).setValue(user)
                                        .addOnCompleteListener(dangki.this, task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Lưu thành công
                                                Toast.makeText(dangki.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                finish();  // Trở lại màn hình đăng nhập
                                            } else {
                                                // Lỗi khi lưu thông tin
                                                Toast.makeText(dangki.this, "Lỗi lưu thông tin! " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Đăng ký thất bại
                                Toast.makeText(dangki.this, "Đăng ký thất bại! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Đối tượng User để lưu thông tin người dùng
    public static class User {
        public String fullName;
        public String email;

        public User() {
            // Constructor mặc định yêu cầu Firebase
        }

        public User(String fullName, String email) {
            this.fullName = fullName;
            this.email = email;
        }
    }
}
