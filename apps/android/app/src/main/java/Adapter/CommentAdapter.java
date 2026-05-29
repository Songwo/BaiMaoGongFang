package Adapter;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.videoapp.R;

import java.util.List;

import Moudle.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    /*public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }*/

    private Context context; // 添加 Context 对象
    private SharedPreferences prefs; // 声明 SharedPreferences 对象
    private String authorId;
    private String avatar;
    private String username;
    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context; // 初始化 Context 对象
        this.comments = comments;
        // 在构造函数中初始化 SharedPreferences
        prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        authorId = prefs.getString("user_id", "");
        avatar = prefs.getString("avatar","");
        username=prefs.getString("username","");
    }
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.commentAuthor.setText(comment.getUsername());
        holder.commentContent.setText(comment.getContent());

        // 设置头像（这里假设使用Glide库加载图片）
         Glide.with(holder.itemView.getContext())
                 .load("http://wacyg.fun/"+comment.getAvatar())
                 .placeholder(R.drawable.usermge) // 加载过程中的占位图
                 .error(R.drawable.banner4) // 加载失败时显示的默认头像
                 .apply(RequestOptions.circleCropTransform())
                 .into(holder.commentAvatar);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView commentAvatar;
        TextView commentAuthor;
        TextView commentContent;

        CommentViewHolder(View itemView) {
            super(itemView);
            commentAvatar = itemView.findViewById(R.id.comment_avatar);
            commentAuthor = itemView.findViewById(R.id.comment_author);
            commentContent = itemView.findViewById(R.id.comment_content);
        }
    }
}
