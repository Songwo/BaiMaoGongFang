package Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.R;
import com.example.videoapp.SearchResultActivity;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {
    private List<String> searches;

    public RecentSearchAdapter(List<String> searches) {
        this.searches = searches;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

/*
        // 直接按正序显示，最新记录在最后
        String search = searches.get(position);

        holder.textView.setText(shortenText(search, 8));
        holder.itemView.setOnClickListener(v -> {

            ((SearchResultActivity)v.getContext()).performLocalSearch(search);
        });*/


        String search = searches.get(position);

        holder.textView.setText(shortenText(search, 8));
        holder.itemView.setOnClickListener(v -> {
            // 修复2：将点击的搜索词添加到历史记录
            ((SearchResultActivity)v.getContext()).updateRecentSearches(search);
            ((SearchResultActivity)v.getContext()).performLocalSearch(search);
        });
    }

    public void updateData(List<String> newData) {
        this.searches = newData;
        notifyDataSetChanged();
    }

    private String shortenText(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvSearchText);
        }
    }
}




