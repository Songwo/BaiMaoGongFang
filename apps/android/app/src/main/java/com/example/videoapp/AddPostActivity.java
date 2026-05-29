package com.example.videoapp;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import Moudle.Post;
import da0.ApiService;
import da0.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;

public class AddPostActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor, etContent;
    private Spinner spSection;
    private LinearLayout btnBack, btnSubmit;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        etTitle = findViewById(R.id.et_title);

        etContent = findViewById(R.id.et_content);
        spSection = findViewById(R.id.sp_section);
        btnBack = findViewById(R.id.btn_back);
        btnSubmit = findViewById(R.id.btn_submit);

        // 设置帖子类别下拉选项
        List<String> sections = Arrays.asList("安全漏洞讨论", "网络攻击案例", "网络工具推荐", "行业动态");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sections);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSection.setAdapter(adapter);

        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 提交按钮点击事件
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();

            String section = spSection.getSelectedItem().toString();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty()  || content.isEmpty()) {
                Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
            } else {
                // 获取用户 ID
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String authorId = prefs.getString("user_id", "");
                String avatar = prefs.getString("avatar","");
                String username=prefs.getString("username","");
                // 创建帖子对象
                Post post = new Post();
                post.setTitle(title);
                post.setUsername(username);
                post.setAuthorId(authorId);
                post.setSection(section);
                post.setContent(content);
                post.setAvatar(avatar);

                // 调用插入帖子接口
                insertPost(post);
            }
        });
    }

    private void insertPost(Post post) {
        ApiService apiService = RetrofitClient.getApiService(AddPostActivity.this);
        // 这里需要在 ApiService 中添加插入帖子的接口方法
        Call<Post> call = apiService.insertPost(post);

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddPostActivity.this, "帖子发布成功", Toast.LENGTH_SHORT).show();
                    // 设置结果码，通知 Forum 页面刷新数据
                    setResult(RESULT_OK);

                    finish();
                } else {
                    Toast.makeText(AddPostActivity.this, "帖子发布失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(AddPostActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



