package com.example.videoapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import Adapter.PostAdapter;
import Moudle.Post;
import Moudle.Result;
import da0.ApiService;
import da0.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class IndustryNewsFragment extends Fragment {


    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_industry_news, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);

        loadPosts();

        return view;
    }

    private void loadPosts() {
        ApiService apiService = RetrofitClient.getApiService(getContext());
        Call<Result<List<Post>>> call = apiService.findAllPosts();

        call.enqueue(new Callback<Result<List<Post>>>() {
            @Override
            public void onResponse(Call<Result<List<Post>>> call, Response<Result<List<Post>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> allPosts = response.body().getData();
                    // 前端过滤逻辑
                    List<Post> filtered = new ArrayList<>();
                    for (Post post : allPosts) {
                        if ("行业动态".equals(post.getSection())) {
                            filtered.add(post);
                        }
                    }
                    postList.clear();
                    postList.addAll(filtered);
                    postAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(getContext(), "数据解析失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<List<Post>>> call, Throwable t) {
                Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}