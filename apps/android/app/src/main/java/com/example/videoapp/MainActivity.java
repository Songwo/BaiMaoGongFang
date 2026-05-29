package com.example.videoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import Adapter.VideoAdapter;
import Moudle.PageResponse;
import Moudle.VideoItem;
import da0.ApiService;
import da0.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 主页面的 Activity 类，继承自 AppCompatActivity
public class MainActivity extends AppCompatActivity {
    // 声明轮播图 ViewPager2、自动滚动处理器、视频列表 RecyclerView、加载对话框等
    private ViewPager2 bannerPager;
    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private RecyclerView videoRecyclerView;
    private ProgressDialog loadingDialog;
    private int currentPage = 1;
    private boolean isLoading = false;
    private List<VideoItem> videoItems;
    private VideoAdapter adapter;
    private boolean hasMore = true;

    private TextView[] pageIndicators;
    private int currentSelectedPage = 0;
    private float touchStartX = 0;

    // 全局视频数据缓存
    public static List<VideoItem> cachedVideos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置当前 Activity 的布局文件
        setContentView(R.layout.activity_main);
        // 初始化 Logo
        ImageView logoImageView = findViewById(R.id.userAvatar);
        logoImageView.setImageResource(R.drawable.logo);
        // 初始化轮播图
        bannerPager = findViewById(R.id.bannerPager);
        // 初始化视频列表
        videoRecyclerView = findViewById(R.id.videoGrid);
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 新增代码：初始化右上角菜单按钮
        ImageView rightButton = findViewById(R.id.rightButton);
        rightButton.setOnClickListener(v -> showPopupMenu(v));

        // 从后端获取轮播图和视频数据
        initBanner();
        // 初始化轮播图的小点
        initIndicator();
        // 加载视频
        loadVideos(0);
        // 底部导航栏点击事件
        findViewById(R.id.navHome).setOnClickListener(v -> {
            // 停止当前播放的视频
            VideoAdapter.stopCurrentPlayback();
            // 跳转到首页
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // 停止当前播放的视频
            VideoAdapter.stopCurrentPlayback();
            // 跳转到我的页面
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.navping).setOnClickListener(v -> {
            // 跳转到论坛页面
            Intent intent = new Intent(MainActivity.this, Forum.class);
            startActivity(intent);
        });
        findViewById(R.id.navAI).setOnClickListener(v -> {
            // 跳转到论坛页面
            Intent intent = new Intent(MainActivity.this,AiActivity.class);
            startActivity(intent);
        });
        // 搜索功能
        EditText searchInput = findViewById(R.id.searchInput);
        ImageView searchButton = findViewById(R.id.searchButton);

        // 搜索按钮点击事件
        searchButton.setOnClickListener(v -> performSearch(searchInput));

        // 监听 Enter 键点击事件
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchInput);
                return true;
            }
            return false;
        });


        /*searchButton.setOnClickListener(v -> {
            // 获取输入的搜索关键词
            String keyword = searchInput.getText().toString().trim();
            if (!keyword.isEmpty()) {
                // 若关键词不为空，创建一个跳转到搜索结果页面的 Intent
                Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                intent.putExtra("keyword", keyword);
                // 启动搜索结果页面的 Activity
                startActivity(intent);
            } else {
                // 若关键词为空，弹出提示信息
                Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            }
        });*/

        // 初始化分页指示器
        initPageIndicators();
    }

    // 执行搜索操作的方法
    private void performSearch(EditText searchInput) {
        String keyword = searchInput.getText().toString().trim();
        if (!keyword.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
            intent.putExtra("keyword", keyword);
            startActivity(intent);
        } else {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
        }
    }

    // 在 Activity 暂停时停止播放视频
    @Override
    protected void onPause() {
        super.onPause();
        VideoAdapter.stopCurrentPlayback();
    }

    // 显示弹出菜单
    private void showPopupMenu(View anchorView) {
        // 创建一个 PopupMenu 对象
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        // 加载菜单布局文件
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

        // 设置菜单项点击事件监听器
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                // 若点击退出登录菜单项，执行退出登录操作
                performLogout();
                return true;
            }
            return false;
        });

        // 显示弹出菜单
        popupMenu.show();
    }

    // 执行退出登录操作
    private void performLogout() {
        // 清除登录状态
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .remove("auth_token")
                .remove("user_id")
                .apply();

        // 跳转到登录页
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // 结束当前 Activity
        finish();
    }

    // 初始化分页指示器
    private void initPageIndicators() {
        pageIndicators = new TextView[]{
                findViewById(R.id.page1),
                findViewById(R.id.page2),
                findViewById(R.id.page3)
        };

        for (int i = 0; i < pageIndicators.length; i++) {
            final int page = i;
            pageIndicators[i].setOnClickListener(new DebouncedOnClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    if (currentSelectedPage != page) {
                        // 若点击的页面与当前页面不同，加载该页面
                        loadPage(page);
                    }
                }
            });
        }
    }

    // 加载指定页面
    private void loadPage(int page) {
        currentSelectedPage = page;
        // 更新分页指示器状态
        updatePageIndicators();
        // 加载该页面的视频数据
        loadVideos(page);
    }

    // 加载下一页
    private void loadNextPage() {
        if (currentSelectedPage < pageIndicators.length - 1) {
            // 若当前页面不是最后一页，加载下一页
            loadPage(currentSelectedPage + 1);
        }
    }

    // 加载上一页
    private void loadPreviousPage() {
        if (currentSelectedPage > 0) {
            // 若当前页面不是第一页，加载上一页
            loadPage(currentSelectedPage - 1);
        }
    }

    // 更新分页指示器状态
    private void updatePageIndicators() {
        for (int i = 0; i < pageIndicators.length; i++) {
            pageIndicators[i].setTextColor(
                    i == currentSelectedPage ?
                            getColor(R.color.blue_light) :
                            getColor(R.color.gray)
            );
        }
    }

    // 预加载下一页数据
    private void preloadNextPage() {
        if (!isLoading && hasMore) {
            isLoading = true;
            // 获取 ApiService 实例
            ApiService api = RetrofitClient.getApiService(this);
            // 调用获取视频列表接口，请求下一页数据
            api.getVideoItems(currentPage + 1, 10)
                    .enqueue(new Callback<PageResponse<VideoItem>>() {
                        @Override
                        public void onResponse(Call<PageResponse<VideoItem>> call, Response<PageResponse<VideoItem>> response) {
                            isLoading = false;
                            if (response.isSuccessful() && response.body() != null) {
                                // 若请求成功，将新数据添加到视频列表中
                                List<VideoItem> newItems = response.body().getContent();
                                videoItems.addAll(newItems);
                                adapter.notifyItemRangeInserted(videoItems.size() - newItems.size(), newItems.size());
                                currentPage++;
                                hasMore = !response.body().isLast();
                            }
                        }

                        @Override
                        public void onFailure(Call<PageResponse<VideoItem>> call, Throwable t) {
                            isLoading = false;
                        }
                    });
        }
    }

    // 加载视频数据
    private void loadVideos(int page) {
        // 显示加载对话框
        showLoading();
        // 获取 ApiService 实例
        ApiService api = RetrofitClient.getApiService(this);
        // 调用获取视频列表接口，请求指定页面的数据
        api.getVideoItems(page + 1, 10)
                .enqueue(new Callback<PageResponse<VideoItem>>() {
                    @Override
                    public void onResponse(Call<PageResponse<VideoItem>> call, Response<PageResponse<VideoItem>> response) {
                        // 隐藏加载对话框
                        hideLoading();
                        if (response.isSuccessful() && response.body() != null) {
                            // 若请求成功，从分页数据中提取视频列表
                            videoItems = response.body().getContent();
                            // 设置视频列表的 RecyclerView
                            setupVideoRecycler(videoItems);
                            hasMore = !response.body().isLast();

                            // 使用 ID 进行去重
                            Set<String> idSet = new HashSet<>();
                            for (VideoItem item : cachedVideos) {
                                idSet.add(item.getId());
                            }

                            for (VideoItem newItem : response.body().getContent()) {
                                if (!idSet.contains(newItem.getId())) {
                                    cachedVideos.add(newItem);
                                    idSet.add(newItem.getId());
                                }
                            }
                        } else {
                            // 若请求失败，显示错误提示信息
                            showError("加载失败: 服务器错误");
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<VideoItem>> call, Throwable t) {
                        // 隐藏加载对话框
                        hideLoading();
                        // 显示网络错误提示信息
                        showError("网络错误: " + t.getMessage());
                    }
                });
    }

    // 显示错误提示信息
    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // 显示加载对话框
    private void showLoading() {
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("加载中...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    // 隐藏加载对话框
    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // 设置视频列表的 RecyclerView
    private void setupVideoRecycler(List<VideoItem> videos) {
        videoItems = videos;
        // 创建视频列表的适配器
        adapter = new VideoAdapter(videoItems, MainActivity.this);
        videoRecyclerView.setAdapter(adapter);
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // 初始化轮播图
    private void initBanner() {
        bannerPager = findViewById(R.id.bannerPager);

        // 创建本地图片列表
        List<Integer> localBanners = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4,
                R.drawable.banner5
        );

        // 设置适配器
        BannerAdapter adapter = new BannerAdapter(localBanners);
        bannerPager.setAdapter(adapter);

        // 自动轮播逻辑
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int current = bannerPager.getCurrentItem();
                int next = (current + 1) % localBanners.size();
                bannerPager.setCurrentItem(next, true);
                autoScrollHandler.postDelayed(this, 4000);
            }
        };

        // 启动自动轮播
        autoScrollHandler.postDelayed(autoScrollRunnable, 4000);
    }

    // 初始化轮播图的小点
    private void initIndicator() {
        List<Integer> localBanners = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4,
                R.drawable.banner5
        );
        LinearLayout indicator = findViewById(R.id.indicator);
        int count = localBanners.size();

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            dot.setBackgroundResource(R.drawable.dot_selector);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(6, 0, 6, 0);
            indicator.addView(dot, params);
        }

        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // 更新小点的选中状态
                updateIndicator(position);
            }
        });
    }

    // 更新小点的选中状态
    private void updateIndicator(int position) {
        LinearLayout indicator = findViewById(R.id.indicator);
        int count = indicator.getChildCount();
        for (int i = 0; i < count; i++) {
            indicator.getChildAt(i).setSelected(i == position);
        }
    }

    // 在 Activity 销毁时移除自动滚动的回调，防止内存泄漏
    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    // 轮播图适配器
    private static class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        private final List<Integer> bannerList;

        public BannerAdapter(List<Integer> bannerList) {
            this.bannerList = bannerList;
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new BannerViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            holder.imageView.setImageResource(bannerList.get(position));
        }

        @Override
        public int getItemCount() {
            return bannerList.size();
        }

        static class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public BannerViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}




