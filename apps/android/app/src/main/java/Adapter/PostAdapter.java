package Adapter;



import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Moudle.Post;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.videoapp.PostActivity;
import com.example.videoapp.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private Context context;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // 使用自定义的布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.tvSection.setText(post.getSection());
        holder.tvTitle.setText(post.getTitle());
        holder.tvContent.setText(post.getContent());
        holder.tvUsername.setText(post.getUsername());
        holder.tvTimestamp.setText(post.getTimestamp().toString());
        holder.tvReplyCount.setText(String.valueOf(post.getReplyCount()));

        // Load avatar image
        Glide.with(holder.itemView.getContext())
                .load("http://wacyg.fun/"+post.getAvatar())
                .placeholder(R.drawable.usermge) // 加载过程中的占位图
                .error(R.drawable.postfl2) // 加载失败时显示的默认头像
                .apply(RequestOptions.circleCropTransform())
                .into(holder.civAvatar);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("postId", post.getId());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvSection, tvTitle, tvContent, tvUsername, tvTimestamp, tvReplyCount;
        ImageView civAvatar;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSection = itemView.findViewById(R.id.post_section);
            tvTitle = itemView.findViewById(R.id.post_title);
            tvContent = itemView.findViewById(R.id.post_content);
            tvUsername = itemView.findViewById(R.id.post_username);
            tvTimestamp = itemView.findViewById(R.id.post_timestamp);
            tvReplyCount = itemView.findViewById(R.id.post_reply_count);
            civAvatar = itemView.findViewById(R.id.post_avatar);
        }
    }
}







