package da0;


import java.util.List;
import java.util.Map;

import Moudle.Comment;
import Moudle.HistoryResponse;
import Moudle.MessageRequest;
import Moudle.NewResult;
import Moudle.PageResponse;
import Moudle.Post;
import Moudle.RegisterRequest;
import Moudle.Result;
import Moudle.User;
import Moudle.VideoItem;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

// 定义一个接口，用于描述与服务器交互的 API 方法
public interface ApiService {

    // 获取个人信息接口
    // @GET 注解表示这是一个 HTTP GET 请求，"user/mes/{username}" 是请求的路径
    // {username} 是一个路径参数，会被 @Path("username") 注解的参数替换
    @GET("user/mes/{username}")
    // Call<Moudle.Result<User>> 表示这个请求返回的是一个封装了 User 对象的 Result 对象
    Call<Moudle.Result<User>> getUserInfo(@Path("username") String username);

    // 登陆接口
    // @POST 注解表示这是一个 HTTP POST 请求，"user/login" 是请求的路径
    @POST("user/login")
    // Call<Moudle.Result> 表示这个请求返回的是一个 Result 对象
    Call<Moudle.Result> login(@Body User user);

    // 注册接口
    // @POST 注解表示这是一个 HTTP POST 请求，"user/register" 是请求的路径
    @POST("user/register")
    // Call<Void> 表示这个请求不返回任何数据
    Call<Void> register(@Body RegisterRequest request);

    // 获取视频资源接口
    // @GET 注解表示这是一个 HTTP GET 请求，"user/videos" 是请求的路径
    @GET("user/videos")
    // Call<PageResponse<VideoItem>> 表示这个请求返回的是一个封装了 VideoItem 列表的 PageResponse 对象
    Call<PageResponse<VideoItem>> getVideoItems(
            // @Query 注解表示这是一个查询参数，会被添加到请求的 URL 后面
            @Query("page") int page,
            @Query("size") int size
    );

    // 修改后
    // @GET 注解表示这是一个 HTTP GET 请求，"/post/findAll" 是请求的路径
    @GET("/post/findAll")
    // Call<Result<List<Post>>> 表示这个请求返回的是一个封装了 Post 列表的 Result 对象
    Call<Result<List<Post>>> findAllPosts();

    // 根据帖子 ID 查询帖子详情
    // @GET 注解表示这是一个 HTTP GET 请求，"/post/findById" 是请求的路径
    @GET("/post/findById")
    // Call<Result<Post>> 表示这个请求返回的是一个封装了 Post 对象的 Result 对象
    Call<Result<Post>> findPostById(@Query("id") String postId);

    // 根据帖子 ID 查询评论
    // @GET 注解表示这是一个 HTTP GET 请求，"/comments/find/{postId}" 是请求的路径
    // {postId} 是一个路径参数，会被 @Path("postId") 注解的参数替换
    @GET("/comments/find/{postId}")
    // Call<Result<List<Comment>>> 表示这个请求返回的是一个封装了 Comment 列表的 Result 对象
    Call<Result<List<Comment>>> findCommentsByPostId(@Path("postId") String postId);

    // 插入评论接口
    // @POST 注解表示这是一个 HTTP POST 请求，"/comments/insert" 是请求的路径
    @POST("/comments/insert")
    // Call<Result<Comment>> 表示这个请求返回的是一个封装了 Comment 对象的 Result 对象
    Call<Result<Comment>> insertComment(@Body Comment comment);

    // 插入帖子接口
    // @GET 注解表示这是一个 HTTP GET 请求，"/insertPost" 是请求的路径
    // 这里使用 GET 请求插入数据不太合适，通常插入数据应该使用 POST 请求
    @POST("/post/insertPost")
    // Call<Post> 表示这个请求返回的是一个 Post 对象
    Call<Post> insertPost(@Body Post post);

    // 上传头像接口
    // @Multipart 注解表示这是一个多部分请求，用于上传文件
    // @POST 注解表示这是一个 HTTP POST 请求，"/user/avatar" 是请求的路径
    @Multipart
    @POST("/user/avatar")
    // Call<Result> 表示这个请求返回的是一个 Result 对象
    Call<Result> uploadAvatar(@Part MultipartBody.Part file);


    // 发送问题到后端的接口
    @POST("api/chat")
    Call<NewResult> sendMessage(@Body MessageRequest message);



    @GET("api/chat/history")
    Call<HistoryResponse> getChatHistory();
}