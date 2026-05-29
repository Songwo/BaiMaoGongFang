package Moudle;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class User implements Serializable {
   //用户账号
    private String username;
    //账号密码
    private String password;
    //邮箱
    private String email;
    //用户id
    private String id;
    //注册时间
    private Date createTime ;
    //头像
    private String avatar ;
    //跟新时间
    private Date updateTime;
    //邮箱验证
    private String emailVerified ;
    //
    private String userPic;

    private String openid;
    //private Array pm;
    private List<String> pm;
    private int total_today ;
    private int correct_today ;
    private int incorrect_today ;


    private List<String> likedVideos; // 新增：点赞过的视频ID列表

    public List<String> getLikedVideos() {
        return likedVideos;
    }

    public void setLikedVideos(List<String> likedVideos) {
        this.likedVideos = likedVideos;
    }

    public User() {
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password, String email, String id, Date createTime, String avatar, Date updateTime, String emailVerified, String userPic, String openid, List<String> pm, int total_today, int correct_today, int incorrect_today) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.id = id;
        this.createTime = createTime;
        this.avatar = avatar;
        this.updateTime = updateTime;
        this.emailVerified = emailVerified;
        this.userPic = userPic;
        this.openid = openid;
        this.pm = pm;
        this.total_today = total_today;
        this.correct_today = correct_today;
        this.incorrect_today = incorrect_today;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(String emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public List<String> getPm() {
        return pm;
    }

    public void setPm(List<String> pm) {
        this.pm = pm;
    }

    public int getTotal_today() {
        return total_today;
    }

    public void setTotal_today(int total_today) {
        this.total_today = total_today;
    }

    public int getCorrect_today() {
        return correct_today;
    }

    public void setCorrect_today(int correct_today) {
        this.correct_today = correct_today;
    }

    public int getIncorrect_today() {
        return incorrect_today;
    }

    public void setIncorrect_today(int incorrect_today) {
        this.incorrect_today = incorrect_today;
    }
}
