package com.bmgf.controller;
// 导入必要的依赖
import com.bmgf.service.impl.*;
import com.bmgf.util.JwtUtil;
import com.bmgf.po.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bmgf.dao.UserRepository;
import com.bmgf.dao.VideoRepository;
import com.bmgf.util.LoginRequest;
import com.bmgf.util.PasswordUtil;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.bmgf.util.PasswordUtil.matches;

/**
 * 管理员控制器
 * 处理管理员相关操作：登录认证、视频上传管理
 */
@RestController
@RequestMapping("/admin") // 基础请求路径
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    // 依赖注入的组件
    private final VideoStorageService videoStorageService; // 视频存储服务
    private final VideoRepository videoRepository;        // 视频数据访问层
    private final String baseUrl;                         // 视频基础访问URL
    @Autowired
    private JwtUtil jwtUtil; // JWT工具类
    // 从配置文件中注入管理员信息
    @Value("${admin.Id}")
    private String adminId;          // 管理员ID
    @Value("${admin.username}")
    private String adminUsername;    // 管理员账号
    @Value("${admin.password}")
    private String adminPassword;    // 管理员密码（加密存储）
    /**
     * 构造器依赖注入
     * @param videoStorageService 视频存储服务
     * @param videoRepository     视频数据仓库
     * @param baseUrl             基础URL（从配置读取）
     */
    @Autowired
    public AdminController(
            VideoStorageService videoStorageService,
            VideoRepository videoRepository,
            @Value("${storage.media.video.location}") String baseUrl) {
        this.videoStorageService = videoStorageService;
        this.videoRepository = videoRepository;
        this.baseUrl = baseUrl;
    }
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    /**
     * 管理员登录接口
     * @param request 登录请求体（包含用户名密码）
     * @return 包含JWT令牌的响应结果
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        // 验证用户名密码是否匹配配置
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户不存在，请注册");
        }
        if (!matches(request.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }
        // 生成JWT令牌
//            String token = jwtUtil.generateToken(adminUsername,adminId,"ADMIN");
//            return Result.success(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                userDetails.getAuthorities() // 注入权限列表
        );
        return Result.success(token);
    }
    @Autowired
    private QuestionService questionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HoleService holeService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PostService postService;
    @Autowired
    private HonoraryTitleService honoraryTitleService;
    @Autowired
    private UserAccurayService userAccuracyService;
    @PostMapping("/insertQuestion")
    public Result insertQuestion(@RequestBody Question question) {
        questionService.saveQuestion(question);
        return Result.success();
    }
    @PostMapping("/insertPost")
    public Result insertPost(@RequestBody Post post) {
        postService.savePost(post);
        return Result.success();
    }
    @PostMapping("/insertHole")
    public Result insertHole(@RequestBody Hole ho) {
        holeService.save(ho);
        return Result.success();
    }
    @PostMapping("/insertHonoraryTitle")
    public Result insertHonoraryTitle(@RequestBody HonoraryTitle title) {
        if (honoraryTitleService.insert(title)){
            return Result.success();
        }else {
            return Result.error("插入失败");
        }
    }
    @PostMapping("/insertComment")
    public Result insertComment(@RequestBody Comment comment) {
        commentService.addComment(comment);
        return Result.success();
    }
    @GetMapping("/findAllUser")
    public Result findAll() {
        return Result.success(userRepository.findAll());
    }
    @GetMapping("/findAllTotalScore")
    public Result findAllTotalScore() {
        return Result.success(userService.findAllTotalScore());
    }
    @GetMapping("/findAllHole")
    public Result fidAllHole(){
        return Result.success(holeService.findHole());
    }
    @GetMapping("/findAllVideo")
    public Result fidAllVideo(){
        return Result.success(videoRepository.findAll());
    }
    @Autowired
    private RewardService rewardService;
    @GetMapping("/findAllReward")
    public Result findAllReward(){
        return Result.success(rewardService.getReward());
    }
    @GetMapping("/findAllQuestion")
    public Result fidAllQuestion(){
        return Result.success(questionService.findAll());
    }
    @Autowired
    private UserService userService;
    @PutMapping("/UpdateUser")
    public Result UpdateUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        // 参数校验（保持不变）
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String id = user.getId();
        if (id == null || id.trim().isEmpty()) {
            return Result.error("用户ID不能为空");
        }

        User existingUser = userService.findByIdINt(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        Date originalCreateTime = existingUser.getCreateTime();
        String originalPassword = existingUser.getPassword(); // 获取已加密的原密码

        // 动态更新非空字段（保持不变）
        updateNonNullFields(user, existingUser);

        // 处理密码加密逻辑（核心修改部分）
        String processedPassword = Optional.ofNullable(user.getPassword())
                .filter(p -> !p.isEmpty())
                .map(PasswordUtil::encode) // 新密码加密
                .orElse(originalPassword); // 保留原密码

        // 设置最终值
        existingUser.setId(id);
        existingUser.setCreateTime(originalCreateTime);
        existingUser.setPassword(processedPassword); // 使用处理后的密码
        existingUser.setUpdateTime(new Date());

        userRepository.save(existingUser);
        return Result.success();
    }
    // 使用反射实现字段动态更新
    private void updateNonNullFields(User source, User target) {
        Class<?> clazz = User.class;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(source);

                // 跳过ID字段和特殊字段
                if ("id".equals(field.getName()) || "createTime".equals(field.getName())) {
                    continue;
                }
                // 特殊处理基本类型字段
                if (field.getType().isPrimitive()) {
                    if (value != null && ((Number) value).doubleValue() != 0) {
                        field.set(target, value);
                    }
                } else if (value != null) {
                    // 处理字符串类型非空
                    if (value instanceof String && ((String) value).isEmpty()) {
                        continue;
                    }
                    // 处理集合类型非空
                    if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                        continue;
                    }
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                log.error("字段更新失败：{}", field.getName(), e);
            }
        }
    }
    @PutMapping("/UpdateHole")
    public Result UpdateHole(@Valid @RequestBody Hole hole, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String id = hole.getId();
        if (id == null || id.isEmpty()) {
            return Result.error("漏洞ID不能为空");
        }
        Hole existingHole = holeService.findById(id);
        if (existingHole == null) {
            return Result.error("漏洞不存在");
        }
        // 保留创建时间
        Date originalCreatedAt = existingHole.getCreated_at();
        // 动态更新
        updateNonNullFields(hole, existingHole, Hole.class);
        // 保护字段
        existingHole.setId(id);
        existingHole.setCreated_at(originalCreatedAt);
        existingHole.setUpdated_at(new Date()); // 自动更新修改时间
        holeService.save(existingHole);
        return Result.success();
    }
    @Value("${storage.media.avatar.location}")
    private String uploadDir;
    @Autowired
    private MongoTemplate mongoTemplate;
    @PostMapping("/UpdateAvatar")
    public Result uploadAvatar(@RequestParam("f1ile") MultipartFile file,@RequestParam("id") String id,
                               HttpServletRequest request) {
        // 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (!file.getContentType().startsWith("image/")) {
            return Result.error("仅支持图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }
        try {
            // 生成唯一文件名
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            // 更新用户头像路径（JPA）
            String avatarUrl = "avatar/" + fileName;
            userRepository.findById(id).ifPresent(u -> {
                u.setAvatar(avatarUrl);
                userRepository.save(u);
            });
            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("上传失败");
        }
    }
    @PutMapping("/UpdatePost")
    public Result UpdatePost(@Valid @RequestBody Post post, BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String postId = post.getId();
        if (postId == null || postId.isEmpty()) {
            return Result.error("帖子ID不能为空");
        }
        Post existingPost = postService.findById(postId);
        if (existingPost == null) {
            return Result.error("帖子不存在");
        }
        // 保留不可变字段
        Date originalTimestamp = existingPost.getTimestamp();
        String originalAuthorId = existingPost.getAuthorId();
        // 动态更新字段
        updateNonNullFields(post, existingPost, Post.class);
        // 恢复保护字段
        existingPost.setId(postId);
        existingPost.setTimestamp(originalTimestamp);
        existingPost.setAuthorId(originalAuthorId); // 作者ID不可修改
        // 自动维护其他字段（如需要可添加最后修改时间）
        // existingPost.setUpdateTime(new Date());
        postService.savePost(existingPost);
        return Result.success();
    }
    @PutMapping("/UpdateQuestion")
    public Result UpdateQuestion(@Valid @RequestBody Question ques, BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String quesId = ques.getId();
        if (quesId == null || quesId.isEmpty()) {
            return Result.error("问题ID不能为空");
        }
        Question existingQues = questionService.findById(quesId);
        if (existingQues == null) {
            return Result.error("问题不存在");
        }
        questionService.saveQuestion(ques);
        return Result.success();
    }
    @PutMapping("/UpdateVideo")
    public Result UpdateVideo(@Valid @RequestBody VideoItem vid, BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String vidId = vid.getId();
        if (vidId == null || vidId.isEmpty()) {
            return Result.error("问题ID不能为空");
        }
        VideoItem existingQues = videoService.findById(vidId);
        if (existingQues == null) {
            return Result.error("问题不存在");
        }
        videoService.saveVideo(vid);
        return Result.success();
    }
    @Autowired
    private VideoCategoryService videoCategoryService;
    @PutMapping("/UpdateVideoCategory")
    public Result UpdateVideoCategory(@Valid @RequestBody Video_Category vid, BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String vidId = vid.getId();
        if (vidId == null || vidId.isEmpty()) {
            return Result.error("分类ID不能为空");
        }
        Video_Category existingQues = videoCategoryService.findById(vidId);
        if (existingQues == null) {
            return Result.error("分类不存在");
        }
        videoCategoryService.save(vid);
        return Result.success();
    }
    @Autowired
    private CategoryService categoryService;
    @PutMapping("/UpdateCategory")
    public Result UpdateCategory(@Valid @RequestBody Category vid, BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String vidId = vid.getId();
        if (vidId == null || vidId.isEmpty()) {
            return Result.error("分类ID不能为空");
        }
        Category existingQues = categoryService.findById(vidId);
        if (existingQues == null) {
            return Result.error("分类不存在");
        }
        categoryService.save(vid);
        return Result.success();
    }
    @Autowired
    private RewardService rService;
    @PutMapping("/UpdateReward")
    public Result UpdateReward(@Valid @RequestBody Reward re, BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String reId = re.getId();
        if (reId == null || reId.isEmpty()) {
            return Result.error("奖励ID不能为空");
        }
        Reward existingRew = rService.findById(reId);
        if (existingRew == null) {
            return Result.error("奖励不存在");
        }
        rService.save(re);
        return Result.success();
    }
    @PutMapping("/UpdateComment")
    public Result UpdateComment(@Valid @RequestBody Comment comment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        String commentId = comment.getId();
        if (commentId == null || commentId.isEmpty()) {
            return Result.error("评论ID不能为空");
        }
        Comment existingComment = commentService.findById(commentId);
        if (existingComment == null) {
            return Result.error("评论不存在");
        }
        // 保留关联ID和时间戳
        String originalPostId = existingComment.getPostId();
        String originalAuthorId = existingComment.getAuthorId();
        Date originalTimestamp = existingComment.getTimestamp();
        // 动态更新
        updateNonNullFields(comment, existingComment, Comment.class);
        // 保护字段
        existingComment.setId(commentId);
        existingComment.setPostId(originalPostId);
        existingComment.setAuthorId(originalAuthorId);
        existingComment.setTimestamp(originalTimestamp);
        commentService.save(existingComment);
        return Result.success();
    }
    private <T> void updateNonNullFields(T source, T target, Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(source);
                // 跳过保护字段
                if (field.getName().equals("id") ||
                        field.getName().equals("cve_Id") ||
                        field.getName().equals("postId") ||
                        field.getName().equals("authorId") ||
                        field.getName().equals("timestamp") ||
                        field.getName().equals("created_at")) {
                    continue;
                }
                // 处理基本类型默认值问题
                if (field.getType().isPrimitive()) {
                    if (value != null && ((Number) value).doubleValue() != 0) {
                        field.set(target, value);
                    }
                } else if (value != null) {
                    // 处理字符串空值
                    if (value instanceof String && ((String) value).isEmpty()) {
                        continue;
                    }
                    // 处理集合空值
                    if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                        continue;
                    }
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                log.error("字段更新失败：{}", field.getName(), e);
            }
        }
    }
    @PutMapping("/deleteHole")
    public Result deleteHole(@RequestBody Hole ho) {
        holeService.delete(ho.getId());
        return Result.success();
    }
    @PutMapping("/deleteQuestion")
    public Result deleteQuestion(@RequestBody Question ques) {
        questionService.deleteQuestion(ques.getId());
        return Result.success();
    }
    @PutMapping("/deletePost")
    public Result deletePost(@RequestBody Post post) {
        postService.deletePost(post);
        return Result.success();
    }
    @PutMapping("/deleteUser")
    public Result deleteUser(@RequestBody User user) {
        userRepository.delete(user);
        return Result.success();
    }
    @PutMapping("/deleteComment")
    public Result deleteUser(@RequestBody Comment com) {
        String postId=commentService.findById(com.getId()).getPostId();
        Post post = postService.findById(postId);
        post.setReplyCount(post.getReplyCount()-1);
        postService.savePost(post);
        commentService.deleteCommentById(com.getId());

        return Result.success();
    }
    @PutMapping("/deleteVideo")
    public Result deleteVideo(@RequestBody VideoItem vio) {
        videoService.deleteVideo(vio.getId());
        return Result.success();
    }
    @GetMapping("/questionCount")
    public Result questionCount() {
        return Result.success(questionService.countQuestionsByCategory());
    }
    @GetMapping("/VideoCount")
    public Result VideoCount() {
        return Result.success(videoService.countVideoByCategory());
    }
    @GetMapping("/PostCount")
    public Result PostCount() {
        return Result.success(postService.countPostBySection());
    }
    @GetMapping("/score")
    public List<UserAccuracy> getAllUserAccuracy() {
        return userAccuracyService.calculateAllUserAccuracy();
    }
    /**
     * 视频上传接口
     * @param videoFile       视频文件（multipart/form-data格式）
     * @param uploadRequest  其他表单参数（标题、描述、分类）
     * @return 视频上传结果响应实体
     */
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            @RequestPart("video") MultipartFile videoFile,
            @ModelAttribute VideoUploadRequest uploadRequest) {
        // 验证文件是否为空
        if (videoFile.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new VideoUploadResponse("请选择要上传的视频文件"));
        }
        // 验证文件类型是否合法
        if (!isValidVideoType(videoFile.getContentType())) {
            return ResponseEntity.badRequest().body(
                    new VideoUploadResponse("仅支持MP4、AVI、MOV格式视频"));
        }
        try {
            // 1. 存储视频文件到服务器
            String storedFileName = videoStorageService.storeVideo(videoFile);
            // 2. 创建视频条目对象
            VideoItem videoItem = new VideoItem();
            videoItem.setTitle(uploadRequest.getTitle());
            videoItem.setDescription(uploadRequest.getDescription());
            videoItem.setCategories(uploadRequest.getCategories());
            videoItem.setUrl("http://47.117.70.79/www/wwwroot/uploads/videos/" + storedFileName); // 组合访问URL
            videoItem.setCreateTime(new Date());
            // 3. 保存到数据库
            VideoItem savedItem = videoRepository.save(videoItem);
            // 返回成功响应
            return ResponseEntity.ok().body(new VideoUploadResponse(
                    "上传成功",
                    savedItem.getId(),
                    savedItem.getUrl(),
                    savedItem.getTitle()
            ));
        } catch (Exception e) {
            // 处理异常情况
            return ResponseEntity.internalServerError()
                    .body(new VideoUploadResponse("上传失败: " + e.getMessage()));
        }
    }
    /**
     * 验证视频文件类型是否合法
     * @param contentType 文件MIME类型
     * @return 是否合法视频类型
     */
    private boolean isValidVideoType(String contentType) {
        return contentType != null &&
                (contentType.equals("video/mp4") ||      // MP4格式
                        contentType.equals("video/avi") ||        // AVI格式
                        contentType.equals("video/quicktime"));   // MOV格式
    }
    // 内部类：视频上传请求参数封装
    @Data
    public static class VideoUploadRequest {
        private String title;       // 视频标题
        private String description; // 视频描述
        private String categories;  // 视频分类（逗号分隔）
    }
    // 内部类：视频上传响应结构
    @Data
    public static class VideoUploadResponse {
        private String message;    // 操作结果消息
        private String videoId;    // 视频数据库ID
        private String videoUrl;    // 视频访问URL
        private String title;       // 视频标题

        public VideoUploadResponse(String message) {
            this.message = message;
        }
        public VideoUploadResponse(String message, String videoId,
                                   String videoUrl, String title) {
            this.message = message;
            this.videoId = videoId;
            this.videoUrl = videoUrl;
            this.title = title;
        }
    }
}