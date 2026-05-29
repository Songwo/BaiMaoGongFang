package com.example.videoapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import Moudle.HistoryItem;
import Moudle.HistoryResponse;
import Moudle.MessageRequest;
import Moudle.NewResult;
import da0.ApiService;
import da0.RetrofitClient_python;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiActivity extends AppCompatActivity {

    private EditText inputBox;
    private Button submitButton;
    private ListView responseList;
    private ProgressBar loadingProgress;
    private ApiService apiService;
    private static final int MAX_RETRIES = 3;
    private List<String> responseDataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private PopupWindow historyPopup;
    private ListView historyListView;
    private static final int MAX_HISTORY_RETRIES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        inputBox = findViewById(R.id.inputBox);
        submitButton = findViewById(R.id.submitButton);
        responseList = findViewById(R.id.responseList);
        loadingProgress = findViewById(R.id.loadingProgress);
        View settingsButton = findViewById(R.id.settingsButton);

        apiService = RetrofitClient_python.getApiService(this);

        adapter = new ArrayAdapter<>(this, R.layout.list_item_response, R.id.responseText, responseDataList);
        responseList.setAdapter(adapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = inputBox.getText().toString().trim();
                if (!question.isEmpty()) {
                    sendQuestionToServer(question, 0);
                }
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryPopup();
            }
        });
    }

    private void showHistoryPopup() {
        if (historyPopup == null) {
            historyListView = new ListView(this);
            historyListView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            historyPopup = new PopupWindow(historyListView,
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.6),
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    true);

            historyPopup.setBackgroundDrawable(null);
            historyPopup.setOutsideTouchable(true);
            historyPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    // 处理弹窗关闭逻辑
                }
            });

            historyListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });

            fetchHistory(0);
        }

        if (!historyPopup.isShowing()) {
            // 获取屏幕高度
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            // 计算 PopupWindow 的垂直偏移量，使其居中显示
            int yOffset = (screenHeight - historyPopup.getHeight()) / 2;
            historyPopup.showAtLocation(findViewById(android.R.id.content), Gravity.START | Gravity.CENTER_VERTICAL, 0, yOffset);
        }
    }

    private void fetchHistory(int retryCount) {

        if (retryCount >= MAX_HISTORY_RETRIES) {
            Toast.makeText(AiActivity.this, "多次尝试后仍无法获取历史记录，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<HistoryResponse> call = apiService.getChatHistory();
        call.enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HistoryItem> historyItems = response.body().getHistory();
                    List<String> userHistory = new ArrayList<>();
                    for (HistoryItem item : historyItems) {
                        if ("user".equals(item.getRole())) {
                            userHistory.add(item.getContent());
                        }
                    }
                    // 使用自定义的列表项布局
                    ArrayAdapter<String> historyAdapter = new ArrayAdapter<>(AiActivity.this,
                            R.layout.history_list_item, R.id.history_text, userHistory);
                    historyListView.setAdapter(historyAdapter);

                    historyListView.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedHistory = userHistory.get(position);
                        inputBox.setText(selectedHistory);
                        historyPopup.dismiss();
                    });
                } else {
                    Toast.makeText(AiActivity.this, "获取历史记录失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                t.printStackTrace();
                if (t instanceof java.net.ProtocolException) {
                    // 遇到 ProtocolException 时重试
                    fetchHistory(retryCount + 1);
                } else {
                    Toast.makeText(AiActivity.this, "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendQuestionToServer(String question, int retryCount) {
        if (retryCount >= MAX_RETRIES) {
            Toast.makeText(AiActivity.this, "请求多次失败，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingProgress.setVisibility(View.VISIBLE);

        MessageRequest requestBody = new MessageRequest(question);

        Call<NewResult> call = apiService.sendMessage(requestBody);
        call.enqueue(new Callback<NewResult>() {
            @Override
            public void onResponse(Call<NewResult> call, Response<NewResult> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().getReplay();
                    if (answer != null) {
                        responseDataList.add(answer);
                        adapter.notifyDataSetChanged();
                    } else {
                        // 处理返回数据为空的情况
                        Toast.makeText(AiActivity.this, "返回数据为空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 处理请求成功但响应体为空的情况
                    Toast.makeText(AiActivity.this, "请求成功，但响应体为空", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewResult> call, Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                t.printStackTrace();
                if (t instanceof java.net.ProtocolException) {
                    // 遇到 ProtocolException 时重试
                    sendQuestionToServer(question, retryCount + 1);
                } else {
                    // 显示网络错误提示
                    Toast.makeText(AiActivity.this, "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

















