
package com.example.videoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import Adapter.CommentAdapter;
import Adapter.VideoAdapter;
import Moudle.Comment;
import Moudle.Post;
import Moudle.Result;
import Moudle.User;
import da0.ApiService;
import da0.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends AppCompatActivity {

    private TextView postAuthor;
    private TextView postContent;
    private ImageView postAvatar;
    private EditText commentInput;
    private Button commentSubmit;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    String postId;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postAuthor = findViewById(R.id.post_author);
        postContent = findViewById(R.id.post_content);
        postAvatar = findViewById(R.id.post_avatar);
        commentInput = findViewById(R.id.comment_input);
        commentSubmit = findViewById(R.id.comment_submit);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(PostActivity.this,comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        // 获取传递过来的帖子 ID
     postId = getIntent().getStringExtra("postId");
        if (postId != null) {
            loadPostDetails(postId);
            loadComments(postId);
        }



        findViewById(R.id.navHome11).setOnClickListener(v -> {
            VideoAdapter.stopCurrentPlayback(); // 新增停止播放
            // 跳转到首页
            startActivity(new Intent(PostActivity.this, MainActivity.class));
        });
        findViewById(R.id.navProfile11).setOnClickListener(v -> {
            VideoAdapter.stopCurrentPlayback(); // 新增停止播放
            // 跳转到我的页面
            Intent intent = new Intent(PostActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.navping11).setOnClickListener(v ->{
            Intent intent = new Intent(PostActivity.this, Forum.class);
            startActivity(intent);
        });


        // 添加评论提交按钮的点击事件监听器
        commentSubmit.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString();
            if (!commentText.isEmpty()) {
                addComment(commentText);
            } else {
                Toast.makeText(PostActivity.this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });


    }




    private void loadPostDetails(String postId) {
        ApiService apiService = RetrofitClient.getApiService(this);
        Call<Result<Post>> call = apiService.findPostById(postId);

        call.enqueue(new Callback<Result<Post>>() {
            @Override
            public void onResponse(Call<Result<Post>> call, Response<Result<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post post = response.body().getData();
                    if (post != null) {

                        postAuthor.setText(post.getUsername());
                        postContent.setText(post.getContent());
                        Glide.with(PostActivity.this)
                                .load("http://47.117.70.79/"+post.getAvatar())
                                .placeholder(R.drawable.usermge) // 加载过程中的占位图
                                .error(R.drawable.usermge) // 加载失败时显示的默认头像
                                .apply(RequestOptions.circleCropTransform())
                                .into(postAvatar);
                    }
                } else {
                    Toast.makeText(PostActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Post>> call, Throwable t) {
                Toast.makeText(PostActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments(String postId) {
        ApiService apiService = RetrofitClient.getApiService(this);
        Call<Result<List<Comment>>> call = apiService.findCommentsByPostId(postId);

        call.enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> commentList = response.body().getData();
                    if (commentList != null) {
                        comments.clear();
                        comments.addAll(commentList);
                        commentAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(PostActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
                Toast.makeText(PostActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addComment(String commentText) {
        ApiService apiService = RetrofitClient.getApiService(this);
        Comment comment = new Comment();

        comment.setPostId(postId);
        comment.setContent(commentText);

       /* // 转换日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        comment.setTimestampStr(timestamp);*/

        // 获取当前登录用户信息
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String authorId = prefs.getString("user_id", "");
        String avatar = prefs.getString("avatar","");
        User user = getCurrentUser();
        if (user != null) {
            comment.setUsername(user.getUsername());
            comment.setAuthorId(user.getId());
            // 设置 avatar 字段
            comment.setAvatar(avatar);
        }

        Call<Result<Comment>> call = apiService.insertComment(comment); // 假设接口中有插入评论的方法
        call.enqueue(new Callback<Result<Comment>>() {
            @Override
            public void onResponse(Call<Result<Comment>> call, Response<Result<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PostActivity.this, "评论提交成功", Toast.LENGTH_SHORT).show();
                    commentInput.setText("");
                    loadComments(postId); // 重新加载评论列表
                } else {
                    Toast.makeText(PostActivity.this, "评论提交失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Comment>> call, Throwable t) {
                Toast.makeText(PostActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private User getCurrentUser() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String username = prefs.getString("username", null);
        String avatar = prefs.getString("avatar", null);

        if (userId != null && username != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername(username);
            user.setAvatar("http://wacyg.fun/"+avatar);
            return user;
        }
        return null;
    }
}


