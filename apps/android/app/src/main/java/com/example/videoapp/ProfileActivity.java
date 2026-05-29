package com.example.videoapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import Moudle.Result;
import Moudle.TokenManager;
import Moudle.User;
import da0.ApiService;
import da0.RetrofitClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private TextView tvUsername, tvEmail, tvUserId, tvCreateTime, tvUpdateTime;
    private ProgressDialog progressDialog;
    private Button btnSelectImage;
    private TokenManager tokenManager;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvUserId = findViewById(R.id.tvUserId);
        tvCreateTime = findViewById(R.id.tvCreateTime);
        tvUpdateTime = findViewById(R.id.tvUpdateTime);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        findViewById(R.id.navHome22).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.navping22).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, Forum.class);
            startActivity(intent);
        });


        findViewById(R.id.navAIprofile).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AiActivity.class);
            startActivity(intent);
        });

        // 初始化 tokenManager
        tokenManager = new TokenManager(this);

        // 显示加载对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中...");
        progressDialog.show();

        // 获取用户信息
        fetchUserInfo();



        // 注册图片选择回调
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Uri selectedImageUri = data.getData();
                                if (selectedImageUri != null) {
                                    // 显示选择的图片
                                    Glide.with(ProfileActivity.this)
                                            .load(selectedImageUri)
                                            .placeholder(R.drawable.usermge)
                                            .error(R.drawable.usermge)
                                            .circleCrop()
                                            .into(ivProfileImage);

                                    // 上传图片
                                    uploadAvatar(selectedImageUri);
                                }
                            }
                        }
                    }
                });

        // 选择图片按钮点击事件
        btnSelectImage.setOnClickListener(v -> {
            Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(pickImageIntent);
        });
    }

    private void fetchUserInfo() {
        String username = tokenManager.getUsername();

        ApiService apiService = RetrofitClient.getApiService(ProfileActivity.this);
        Call<Result<User>> call = apiService.getUserInfo(username);

        call.enqueue(new Callback<Result<User>>() {
            @Override
            public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Result<User> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            User user = result.getData();
                            if (user != null) {
                                // 基础信息
                                tvUsername.setText(user.getUsername());
                                tvEmail.setText(user.getEmail());
                                tvUserId.setText(user.getId());

                                // 日期格式化
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                                // 注册时间处理
                                if (user.getCreateTime() != null) {
                                    tvCreateTime.setText(sdf.format(user.getCreateTime()));
                                } else {
                                    tvCreateTime.setText("未注册");
                                }

                                // 更新时间处理
                                if (user.getUpdateTime() != null) {
                                    tvUpdateTime.setText(sdf.format(user.getUpdateTime()));
                                } else {
                                    tvUpdateTime.setText("暂无更新");
                                }

                                // 头像加载
                                Glide.with(ProfileActivity.this)
                                        .load("http://wacyg.fun/" + user.getAvatar())
                                        .placeholder(R.drawable.usermge)
                                        .error(R.drawable.usermge)
                                        .circleCrop()
                                        .into(ivProfileImage);

                                Log.d("ProfileActivity", "Avatar URL: " + user.getAvatar());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<User>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAvatar(Uri imageUri) {
        progressDialog.setMessage("上传中...");
        progressDialog.show();

        String filePath = getRealPathFromUri(imageUri);
        if (filePath != null) {
            File file = new File(filePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);



            ApiService apiService = RetrofitClient.getApiService(ProfileActivity.this);
            Call<Result> call = apiService.uploadAvatar(body);

            call.enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Result result = response.body();
                        if (result != null) {
                            Log.d("ProfileActivity", "Upload response: " + new Gson().toJson(result));
                            if (result.getCode() == 200) {
                                Toast.makeText(ProfileActivity.this, "头像上传成功", Toast.LENGTH_SHORT).show();
                                // 更新用户信息到 SharedPreferences
                                updateUserAvatarInPrefs(result.getData().toString());
                                // 可以在这里更新用户信息
                                fetchUserInfo();
                            } else {
                                Toast.makeText(ProfileActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }


    // 更新 SharedPreferences 中的用户头像信息
    private void updateUserAvatarInPrefs(String newAvatarUrl) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("avatar", newAvatarUrl);
        editor.apply();
    }
}









