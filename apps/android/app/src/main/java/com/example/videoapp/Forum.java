package com.example.videoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import Adapter.VideoAdapter;

public class Forum extends AppCompatActivity {

    private FrameLayout contentFrame;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        contentFrame = findViewById(R.id.content_frame);

        // 初始化显示安全漏洞讨论页面
        loadFragment(new SafetyVulnerabilitiesFragment());

        // 设置点击事件监听器
        setClickListener(R.id.safety_vulnerabilities, new SafetyVulnerabilitiesFragment());
        setClickListener(R.id.network_attacks, new NetworkAttacksFragment());
        setClickListener(R.id.network_tools, new NetworkToolsFragment());
        setClickListener(R.id.industry_news, new IndustryNewsFragment());


        // 设置圆形头像
        setCircularImage(R.id.safety_vulnerabilities_icon, "https://vcg03.cfp.cn/creative/vcg/800/new/VCG41N2163273470.jpg");
        setCircularImage(R.id.network_attacks_icon, "https://vcg00.cfp.cn/creative/vcg/800/new/VCG41N2197880531.jpg");
        setCircularImage(R.id.network_tools_icon, "https://vcg03.cfp.cn/creative/vcg/800/new/VCG41N1491684201.jpg");
        setCircularImage(R.id.industry_news_icon, "https://vcg00.cfp.cn/creative/vcg/800/new/VCG41N1954478686.jpg");



        findViewById(R.id.pinghome).setOnClickListener(v -> {

            // 跳转到首页
            startActivity(new Intent(Forum.this, MainActivity.class));
        });
        findViewById(R.id.pingProfile).setOnClickListener(v -> {

            // 跳转到我的页面
            Intent intent = new Intent(Forum.this, ProfileActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.ping).setOnClickListener(v ->{
            Intent intent = new Intent(Forum.this, Forum.class);
            startActivity(intent);
        });

        findViewById(R.id.subpost).setOnClickListener(v -> {
            Intent intent = new Intent(Forum.this, AddPostActivity.class);
            startActivity(intent);
        });


        findViewById(R.id.navAIfroum).setOnClickListener(v -> {
            Intent intent = new Intent(Forum.this, AiActivity.class);
            startActivity(intent);
        });


    }






    private void setClickListener(int viewId, final Fragment fragment) {
        LinearLayout view = findViewById(viewId);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(fragment);
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }

    private void setCircularImage(int imageViewId, String imageUrl) {
        ImageView imageView = findViewById(imageViewId);
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .error(R.drawable.postfl2)
                .into(imageView);
    }
    }
