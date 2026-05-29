package Moudle;

import java.util.Date;
import java.util.Objects;

public class VideoItem {
    private String id;
    private String title;
    private String url;       // 字段名必须与后端返回的 "url" 一致
    private Date createTime;

    public VideoItem() {
    }

    public VideoItem(String id, String title, String url, Date createTime) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoItem item = (VideoItem) o;
        return Objects.equals(id, item.id); // 使用唯一ID判断相等性
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
