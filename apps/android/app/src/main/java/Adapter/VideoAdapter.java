package Adapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.videoapp.R;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Moudle.VideoItem;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<VideoItem> videoItems;
    private Context context;
    private RecyclerView recyclerView; // 新增
    private static ExoPlayer currentPlayer;
    // 增加全局播放状态管理
    private static VideoViewHolder currentPlayingHolder = null;

    public VideoAdapter(List<VideoItem> videoItems, Context context) {
        this.videoItems = videoItems;
        this.context = context;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        private ExoPlayer player;
        PlayerView playerView;
        TextView tvTitle;
        public VideoViewHolder(View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            tvTitle = itemView.findViewById(R.id.tvTitle);

            initializePlayer();


        }

        private void initializePlayer() {


            // 释放旧播放器（如果存在）
            releasePlayer();

            // 创建新播放器
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context)
                    .setMediaCodecSelector(new CodecSelector());

            player = new ExoPlayer.Builder(context, renderersFactory)
                    .setSeekForwardIncrementMs(5000)
                    .setSeekBackIncrementMs(5000)
                    .build();

            playerView.setPlayer(player);
            playerView.setOnClickListener(v -> togglePlayback());

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Log.e("PlayerError", "错误类型: " + error.getErrorCodeName());
                    // 尝试重新初始化播放器
                    initializePlayer();
                }
            });
        }


        private void togglePlayback() {
            if (player.isPlaying()) {
                player.pause();
                if (currentPlayer == player) {
                    currentPlayer = null;
                }
            } else {
                // 暂停其他播放器
                if (currentPlayer != null && currentPlayer.isPlaying()) {
                    currentPlayer.pause();
                }
                currentPlayer = player;
                player.play();
            }
        }


        private boolean isSupportedFormat(String url) {
            return url.endsWith(".mp4") || url.contains("h264");
        }

        public void bind(VideoItem item) {

            // 检查视频格式
            if (!isSupportedFormat(item.getUrl())) {
                Toast.makeText(context, "不支持的视频格式", Toast.LENGTH_SHORT).show();
                return;
            }

            // 确保播放器存在
            if (player == null) {
                initializePlayer();
            }





            tvTitle.setText(item.getTitle());
            MediaItem mediaItem = MediaItem.fromUri(item.getUrl());
            player.setMediaItem(mediaItem);
            player.prepare();

        }





        public void releasePlayer() {
            if (player != null&& player.getPlaybackState() != Player.STATE_IDLE) {
                if (currentPlayer == player) {
                    currentPlayer = null;
                }
                player.release();
                player = null;
            }
        }


    }

    // 新增静态控制方法
    public static void stopCurrentPlayback() {
        if (currentPlayer != null) {
            currentPlayer.pause();
            currentPlayer = null;
        }
    }


    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        // 释放所有播放器
        for (int i = 0; i < getItemCount(); i++) {
            VideoViewHolder holder = (VideoViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                holder.releasePlayer();
            }
        }
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {


        // 暂停非当前项的播放
        if (currentPlayer != null && currentPlayer != holder.player) {
            holder.player.pause();
        }

        VideoItem item = videoItems.get(position);
        holder.bind(item);

    }

    @Override
    public int getItemCount() {
        return videoItems != null ? videoItems.size() : 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }


    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        // 只在非当前播放项时释放播放器
        if (currentPlayer != holder.player) {
            holder.releasePlayer();
        }
    }

    private static class CodecSelector implements MediaCodecSelector {
        @Override
        public List<MediaCodecInfo> getDecoderInfos(
                String mimeType,
                boolean requiresSecureDecoder,
                boolean requiresTunnelingDecoder
        ) throws MediaCodecUtil.DecoderQueryException {

            List<MediaCodecInfo> codecInfos = MediaCodecUtil.getDecoderInfos(
                    mimeType,
                    requiresSecureDecoder,
                    requiresTunnelingDecoder
            );



            // 方法二：兼容方案（如果仍需要）
            List<MediaCodecInfo> filtered = new ArrayList<>();
            for (MediaCodecInfo info : codecInfos) {
                String name = info.name.toLowerCase();
                if (name.startsWith("omx.google.") ||
                        name.contains("sw") ||
                        name.contains("soft")) {
                    filtered.add(info);
                }
            }

            return filtered.isEmpty() ? codecInfos : filtered;
        }
    }


}
