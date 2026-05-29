package Moudle;

import java.util.Date;
//帖子类
public class Post {

    private String id;
    //作者ID也就是当先用户的ID
    private String authorId;
    // 用户的姓名
    private String username;
    //标题
    private String title;
    //帖子类别
    private String section;
    //该属性用于存储评论的内容
    private String content;
    //评论数
    private int replyCount = 0;
    //该属性用于存储评论作者的头像 URL
    private String avatar;
    //用于自动设置该属性为文档创建的时间
    private Date timestamp;



    public Post() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
