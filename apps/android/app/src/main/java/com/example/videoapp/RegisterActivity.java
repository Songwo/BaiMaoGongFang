package com.example.videoapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import Moudle.RegisterRequest;
import da0.ApiService;
import da0.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etEmail;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> register());
    }

    // 账号验证：至少3位字符
    private boolean isUsernameValid(String username) {
        String usernamePattern = "^[\\u4e00-\\u9fa5a-zA-Z0-9]{3,}$";
        return username.matches(usernamePattern);
    }

    // 密码验证：8位以上，包含字母和数字
    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        return password.matches(passwordPattern);
    }

    // 邮箱格式验证
    private boolean isEmailValid(String email) {
        String emailPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(emailPattern);
    }

    private void register() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();


        // 验证账号
        if (!isUsernameValid(username)) {
            Toast.makeText(this, "账号需至少3位字符", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证密码
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "密码需8位以上且包含字母和数字", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证邮箱
        if (!isEmailValid(email)) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(RegisterActivity.this);
        Call<Void> call = apiService.register(new RegisterRequest(username, password, email));





        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    finish(); // 返回登录页面
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
}