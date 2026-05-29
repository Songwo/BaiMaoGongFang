package com.example.videoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Adapter.RecentSearchAdapter;
import Adapter.VideoAdapter;
import Moudle.PageResponse;
import Moudle.VideoItem;
import da0.ApiService;
import da0.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// SearchResultActivity.java
public class SearchResultActivity extends AppCompatActivity {
    private List<String> recentSearches = new ArrayList<>();
    private List<VideoItem> searchResults = new ArrayList<>();
    private RecyclerView resultList;
    ImageView ic_home,ic_profile;

    // 添加这两个成员变量
    private TextView tvNoResults;
    private RecyclerView recentSearchGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // 初始化新增的视图
        tvNoResults = findViewById(R.id.tvNoResults);
        recentSearchGrid = findViewById(R.id.recentSearchGrid);
        loadSearchHistory(); // 新增！初始化时加载历史记录
        // 初始化视图
        initViews();
        // 处理搜索逻辑
        handleSearch(getIntent().getStringExtra("keyword"));
        // 设置实时搜索
        setupSearchInput();
    }





    private void initViews() {

        RecyclerView recentGrid = findViewById(R.id.recentSearchGrid);
        recentGrid.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false));
        RecentSearchAdapter recentAdapter = new RecentSearchAdapter(recentSearches);
        recentGrid.setAdapter(recentAdapter);

        // 搜索结果列表
         resultList = findViewById(R.id.searchResultList);
        resultList.setLayoutManager(new LinearLayoutManager(this));
        VideoAdapter videoAdapter = new VideoAdapter(searchResults, this);
        resultList.setAdapter(videoAdapter);

       ic_home=findViewById(R.id.ic_main);
       ic_home.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               VideoAdapter.stopCurrentPlayback(); // 新增停止播放
               Intent xx=new Intent(SearchResultActivity.this,MainActivity.class);
               startActivity(xx);
           }
       });

       ic_profile=findViewById(R.id.ic_profile);
       ic_profile.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               VideoAdapter.stopCurrentPlayback(); // 新增停止播放
               Intent xx=new Intent(SearchResultActivity.this,ProfileActivity.class);
               startActivity(xx);
           }
       });
    }



    private void handleSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 显示最近搜索记录
            recentSearchGrid.setVisibility(View.VISIBLE);
            resultList.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
            return;
        }

        updateRecentSearches(keyword);
        performLocalSearch(keyword);
        recentSearchGrid.setVisibility(View.GONE); // 隐藏最近搜索
    }



    public void updateRecentSearches(String keyword) {
        // 修复3：优化去重逻辑
        if (recentSearches.contains(keyword)) {
            recentSearches.remove(keyword);
        }

        recentSearches.add(keyword); // 新记录添加在末尾

        // 保持最多10条（从头部删除旧记录）
        while (recentSearches.size() > 10) {
            recentSearches.remove(0);
        }

        // 修复4：立即刷新适配器
        RecentSearchAdapter adapter = (RecentSearchAdapter) recentSearchGrid.getAdapter();
        adapter.updateData(new ArrayList<>(recentSearches)); // 使用新列表触发更新

        saveSearchHistory();
    }

    // 修改后的存储方法
    private void saveSearchHistory() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        prefs.edit()
                .putString("history", new Gson().toJson(recentSearches)) // 使用Gson序列化List
                .apply();
    }



    private void loadSearchHistory() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String json = prefs.getString("history", "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            recentSearches = new Gson().fromJson(json, type);

            // 修复1：保持最新记录在列表末尾（无需subList操作）
            Collections.reverse(recentSearches); // 反转列表顺序
            if (recentSearches.size() > 10) {
                recentSearches = recentSearches.subList(0, 10); // 保留最新10条
            }
            Collections.reverse(recentSearches); // 恢复正确顺序
        }
    }



    // 在SearchResultActivity.java中添加
    @Override
    protected void onPause() {
        super.onPause();
        VideoAdapter.stopCurrentPlayback(); // 停止播放
    }


    // 修改 SearchResultActivity.java 的 performLocalSearch 方法
    public void performLocalSearch(String keyword) {
        new AsyncTask<String, Void, List<VideoItem>>() {
            @Override
            protected List<VideoItem> doInBackground(String... keys) {
                Set<String> uniqueIds = new HashSet<>(); // 新增去重集合
                List<VideoItem> results = new ArrayList<>();
                String searchKey = keys[0].toLowerCase().trim();

                for (VideoItem video : MainActivity.cachedVideos) {
                    if (video.getTitle().toLowerCase().contains(searchKey)) {
                        if (!uniqueIds.contains(video.getId())) { // 去重检查
                            results.add(video);
                            uniqueIds.add(video.getId());
                        }
                    }
                }
                return results;
            }

            @Override
            protected void onPostExecute(List<VideoItem> results) {
                searchResults.clear();
                searchResults.addAll(results);
                VideoAdapter adapter = (VideoAdapter) resultList.getAdapter();
                adapter.notifyDataSetChanged();

                // 控制提示显示
                if (results.isEmpty()) {
                    tvNoResults.setVisibility(View.VISIBLE);
                    resultList.setVisibility(View.GONE);
                    recentSearchGrid.setVisibility(View.GONE);
                    // 也可以添加Toast提示
                    Toast.makeText(SearchResultActivity.this,
                            "没有找到相关视频", Toast.LENGTH_SHORT).show();
                } else {
                    tvNoResults.setVisibility(View.GONE);
                    resultList.setVisibility(View.VISIBLE);
                }
                // 确保最近搜索记录一直显示
                recentSearchGrid.setVisibility(View.VISIBLE);
            }
        }.execute(keyword);
    }


   private void setupSearchInput() {
       EditText searchInput = findViewById(R.id.searchInput);
       searchInput.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

           }

           @Override
           public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

           }

           @Override
           public void afterTextChanged(Editable s) {
               String query = s.toString().trim();
               if (query.isEmpty()) {
                   // 显示最近搜索记录
                   recentSearchGrid.setVisibility(View.VISIBLE);
                   resultList.setVisibility(View.GONE);
                   tvNoResults.setVisibility(View.GONE);
               } else {
                   performLocalSearch(query);
                   // 确保最近搜索记录一直显示
                   recentSearchGrid.setVisibility(View.VISIBLE);
               }
           }

       });
   }




}


