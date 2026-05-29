package Moudle;


import org.intellij.lang.annotations.Pattern;

import java.util.Date;
//评论类
public class Comment {
    private String id;
    private String postId; // 关联 Post.id (ObjectId 格式)
   //作者的Id
    private String authorid;
    //作者姓名
    private String username;
    //评论的内容
    private String content;
    // 自动生成创建时间
    private Date timestamp;
    //作者头像的url
    private String avatar;


/*

    // 新增日期字符串字段，用于传递格式化后的日期
    private String timestampStr;

    public String getTimestampStr() {
        return timestampStr;
    }

    public void setTimestampStr(String timestampStr) {
        this.timestampStr = timestampStr;
    }
*/

    public Comment() {
    }

    public Comment(String id, String postId, String authorId, String username, String content, Date timestamp, String avatar) {
        this.id = id;
        this.postId = postId;
        this.authorid = authorId;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.avatar = avatar;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthorId() {
        return authorid;
    }

    public void setAuthorId(String authorId) {
        this.authorid = authorId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}