package com.example.videoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import Moudle.LoginRequest;
import Moudle.Result;
import Moudle.TokenManager;
import Moudle.User;
import da0.ApiService;
import da0.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 登录页面的 Activity 类，继承自 AppCompatActivity
public class LoginActivity extends AppCompatActivity {
    // 声明用户名和密码输入框、登录和注册按钮
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置当前 Activity 的布局文件
        setContentView(R.layout.activity_login);

        // 通过布局文件中的 ID 找到对应的视图控件
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // 为登录按钮设置点击事件监听器
        btnLogin.setOnClickListener(v -> {
            // 获取输入的用户名和密码，并去除首尾空格
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 检查用户名和密码是否为空
            if (username.isEmpty() || password.isEmpty()) {
                // 若为空，弹出提示信息
                Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            } else {
                // 若不为空，调用登录方法
                login(username, password);
            }
        });

        // 为注册按钮设置点击事件监听器
        btnRegister.setOnClickListener(v -> {
            // 创建一个跳转到注册页面的 Intent
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            // 启动注册页面的 Activity
            startActivity(intent);
        });
    }

    // 登录方法
    private void login(String username, String password) {
        // 创建一个 User 对象，包含用户名和密码
        User user = new User(username, password);
        // 获取 ApiService 实例
        ApiService apiService = RetrofitClient.getApiService(LoginActivity.this);
        // 调用登录接口，返回一个 Call 对象
        Call<Result> call = apiService.login(user);

        // 异步执行网络请求
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                // 检查请求是否成功
                if (response.isSuccessful()) {
                    // 获取响应结果
                    Result result = response.body();
                    if (result != null && result.getCode() == 200) {
                        // 若响应结果不为空且状态码为 200，保存 Token 和用户名
                        String token = (String) result.getData();
                        new TokenManager(LoginActivity.this).saveAuthData(token, username);

                        // 获取用户完整信息
                        fetchUserInfo(username);
                    } else {
                        // 若登录失败，弹出提示信息
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                // 若请求失败，弹出网络错误提示信息
                Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 获取用户信息方法
    private void fetchUserInfo(String username) {
        // 获取 ApiService 实例
        ApiService apiService = RetrofitClient.getApiService(LoginActivity.this);
        // 调用获取用户信息接口，返回一个 Call 对象
        Call<Result<User>> call = apiService.getUserInfo(username);

        // 异步执行网络请求
        call.enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                // 检查请求是否成功
                if (response.isSuccessful()) {
                    // 获取响应结果
                    Result<User> result = response.body();
                    if (result != null && result.getCode() == 200) {
                        // 若响应结果不为空且状态码为 200，获取用户信息
                        User user = result.getData();
                        if (user != null) {
                            // 保存用户完整信息到 SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("user_id", user.getId());
                            editor.putString("username", user.getUsername());
                            editor.putString("avatar", user.getAvatar());
                            editor.apply();

                            // 跳转到主页面
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            // 结束当前 Activity
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<User>> call, Throwable t) {
                // 若请求失败，弹出获取用户信息失败提示信息
                Toast.makeText(LoginActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}




