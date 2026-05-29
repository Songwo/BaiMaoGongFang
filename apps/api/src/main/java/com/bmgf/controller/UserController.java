package com.bmgf.controller;
import com.bmgf.DTO.AnalysisDTO;
import com.bmgf.DTO.ChangePasswordDTO;
import com.bmgf.DTO.EmailVerificationDTO;
import com.bmgf.DTO.LoginRequest;
import com.bmgf.po.*;
import com.bmgf.service.impl.*;
import com.bmgf.util.JwtUtil;
import com.bmgf.util.LearningAdviceUtil;
import com.bmgf.util.RegisterRequest;
import com.bmgf.dao.AnswerRecordRepository;
import com.bmgf.dao.RewardRepository;
import com.bmgf.dao.video_CategoryRepository;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Path;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.bmgf.util.PasswordUtil.encode;
import static com.bmgf.util.PasswordUtil.matches;
@RestController
@RequestMapping("/user")
public class UserController {
    @Value("${storage.media.avatar.location}")
    private String uploadDir;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService1;

    @Autowired
    private UserService userService;

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationEmail(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String userId = jwtUtil.extractUserId(token); //获取用户ID的方法
        userService.sendVerificationEmail(userId);
        return ResponseEntity.ok().body("Verification email sent");
    }
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerificationDTO dto) {
        boolean verified = userService.verifyEmail(dto.getToken());
        return ResponseEntity.ok().body(verified ? "Email verified successfully" : "Verification failed");
    }
    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader,
                               HttpServletRequest request) {
        // 从安全上下文获取当前用户ID
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = jwtUtil.getUsernameFromToken(token);
        String currentUserId=userService1.findByUsername(username).getId();
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
            User currentUser = userService1.findByIdINt(currentUserId);
            if (currentUser != null) {
                currentUser.setAvatar(avatarUrl);
                userService1.save(currentUser);
            }
            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("上传失败");
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService imUserService;
    @Autowired
    private AnswerRecordRepository answerRecordRepository;
    @GetMapping("/mes/{username}")
    public Result findIdAll(@RequestHeader("Authorization") String authHeader) {
        // 从安全上下文获取当前用户ID
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = jwtUtil.getUsernameFromToken(token);
        String currentUserId=userService1.findByUsername(username).getId();
        User UserNumber = imUserService.findByIdINt(currentUserId);
        Map<String,Integer>map = UserNumber.getHonoraryTitle();
//        System.out.println(map.toString());
        if (map != null && !map.isEmpty()) {
            String selectedKey = null;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 1) {
                    selectedKey = entry.getKey();
                    break;
                }
            }
            if (selectedKey != null) {
                map.put(selectedKey, 1);
            }
        }
        UserNumber.setHonoraryTitle(map);
        if (UserNumber == null) {
            return Result.error("用户不存在");
        }
        return Result.success(UserNumber);
    }
    @PutMapping("/changepwd")
    public Result changePassword(@RequestHeader("Authorization")String authHeader,@Valid @RequestBody ChangePasswordDTO dto) {
        // 获取当前登录用户ID
        // 从安全上下文获取当前用户ID
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = jwtUtil.getUsernameFromToken(token);
        String currentUserId=userService1.findByUsername(username).getId();
        // 校验请求用户是否操作自己
        logger.info("更改请求: token用户id={}", currentUserId);
        logger.info("参数中用户ID={}", dto.getId());
        if (!currentUserId.equals(dto.getId())) {
            return Result.error("无权限操作");
        }
        User user = imUserService.findByIdINt(dto.getId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!matches(dto.getCurrentPassword(), user.getPassword())) {
            return Result.error("当前密码错误");
        }
        user.setPassword(encode(dto.getNewPassword()));

        imUserService.save(user);
        return Result.success("密码修改成功");
    }
    @Autowired
    private HoleService holeService;
    @GetMapping("/findAllHole")
    public Result fidAllHole(){
        return Result.success(holeService.findHole());
    }
    @PutMapping("/update")
    public Result update(@Valid @RequestHeader("Authorization")String authHeader,@Valid @RequestBody User user) {
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = jwtUtil.getUsernameFromToken(token);
        String currentUserId=userService1.findByUsername(username).getId();
        // 确保只能修改自己信息
        if (!currentUserId.equals(user.getId())) {
            return Result.error("无权限操作");
        }
        User existingUser = imUserService.findByIdINt(currentUserId);
        existingUser.setEmail(user.getEmail());
        User existingUser1 = userService1.findByUsername(user.getUsername());
        if (existingUser1 != null) {
            // 用户名已存在，返回错误或者其他逻辑处理
            return Result.error("用户名已经存在");
        }
        existingUser.setUsername(user.getUsername());
        imUserService.save(existingUser);
        return Result.success("用户信息更新成功");
    }
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest request) {

        // 检查用户名是否已存在
        User user1 = imUserService.findByUsername(request.getUsername());
        if (user1 != null) {
            return Result.error("该用户名已存在");
        }
        // 密码加密
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(new HashSet<>(Arrays.asList("ROLE_USER")));
        imUserService.save(user);
        return Result.success();
    }
@Autowired
private CustomUserDetailsService customUserDetailsService;
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        logger.info("登录请求: 用户名={}", request.getUsername());
        User user = imUserService.findByUsername(request.getUsername());
        if (user == null) {
            logger.error("用户不存在: {}", request.getUsername());
            return Result.error("用户不存在，请注册");
        }
        if (!matches(request.getPassword(), user.getPassword())) {
            logger.error("密码错误: 用户={}", request.getUsername());
            return Result.error("密码错误");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                userDetails.getAuthorities() // 注入权限列表
        );
        return Result.success(token);
    }

    @GetMapping("/rank")
    public Result rank() {
        return Result.success(imUserService.getTop100UsersByScore());
    }

    @Autowired
    private VideoService videoService;

    @GetMapping("/videos")
    public ResponseEntity<Page<VideoItem>> getVideos(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        // 参数校验（与之前相同）
        if (page < 1) throw new IllegalArgumentException("页码不能小于1");
        if (size > 100) throw new IllegalArgumentException("单页数据量不能超过100");
        // 转换为 MongoDB 分页参数（页码从0开始）
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(videoService.getVideo(pageable));
    }

    @GetMapping("/video_Category")
    public ResponseEntity<Page<VideoItem>> getVideoCategory(@RequestParam(name = "page", defaultValue = "1") int page,
                                                            @RequestParam(name = "size", defaultValue = "10") int size,  @RequestParam(value = "category", required = false) String key) {
        // 参数校验（与之前相同）
        if (page < 1) throw new IllegalArgumentException("页码不能小于1");
        if (size > 100) throw new IllegalArgumentException("单页数据量不能超过100");
        // 转换为 MongoDB 分页参数（页码从0开始）
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(videoService.getVideos(pageable, key));
    }

    @Autowired
    private RewardRepository rewardRepository;
    @GetMapping("/reward")
    public Result getUserRewards( @RequestHeader("Authorization") String authHeader) {
        // 1. 获取当前用户信息
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        User user = imUserService.findByUsername(username);
        // 2. 获取奖励配置
        List<Reward> rewards = rewardRepository.findAll();
        if (rewards.isEmpty()) {
            return Result.error("奖励配置未初始化");
        }
        Reward rewardConfig = rewards.get(0);
        List<String> allRewards = Arrays.asList(rewardConfig.getContext().split("#+"));
        // 3. 计算用户已解锁的奖励
        int totalScore = user.getTotalScore();
        int unlockedLevels = totalScore / 100; // 每100分解锁一级
        unlockedLevels = Math.min(unlockedLevels, allRewards.size()); // 不超过配置的最大等级
        // 4. 构建已解锁奖励列表
        List<String> unlockedRewards = new ArrayList<>();
        for (int i = 0; i < unlockedLevels; i++) {
            unlockedRewards.add(allRewards.get(i));
        }
        // 5. 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("totalScore", totalScore);
        response.put("unlockedRewards", unlockedRewards);
        response.put("lockedRewardsCount", allRewards.size() - unlockedLevels);
        return Result.success(response);
    }
    @Autowired
    private video_CategoryRepository videoCategoryRepository;
    @GetMapping("/get_category")
    public Result getUserCategory(){
        return Result.success(videoCategoryRepository.findAll());
    }
    @Autowired
    private HonoraryTitleService honoraryTitleService;
    @GetMapping("/get_honoraryTitle1")
    public Result getUserHonoraryTitle(@Valid @RequestHeader("Authorization")String authHeader){
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        System.out.println("username验证过了:"+username);
        return Result.success(honoraryTitleService.findAll());
    }
    @GetMapping("/selectHonoraryTitle")
    public Result selectUserHonoraryTitle(@Valid @RequestHeader("Authorization")String authHeader, String HonoraryTitle){
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if(userService1.selectUserHonoraryTitle(HonoraryTitle,username)){
            return Result.success();
        }else {
            return Result.error("选中失败");
        }
    }
    @GetMapping("/insertHonoraryTitle1")
    public Result insertHonoraryTitle(@Valid @RequestHeader("Authorization")String authHeader, String HonoraryTitle,String points){
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        if(userService1.insertUserHonoraryTitle(HonoraryTitle,username,points)){
            return Result.success();
        }else {
            return Result.error("添加失败，用户积分不足");
        }
    }
    @GetMapping("/checkIn/{username}")
    public ResponseEntity<?> checkIn(@PathVariable String username) {
        boolean success = userService1.checkIn(username);
        if (success) {
            return ResponseEntity.ok("签到成功！");
        } else {
            return ResponseEntity.status(409).body("今日已签到！");
        }
    }
    @Autowired
    private ExamRecordService examRecordService;
    @GetMapping("/Analysis")
    public Result getUserAnalysis(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        User user = imUserService.findByUsername(username);
        if (user == null) {
            return Result.error("该用户不存在");
        }else {
            double a = userService1.currect(username);
            String b = examRecordService.totalTime(username);
            int c = user.getConsecutiveCheckInDays();
            int d = userService1.intotalPass(username);
            String advice = LearningAdviceUtil.generateAdvice(a, b, c, d);
            AnalysisDTO analysisDTO = new AnalysisDTO();
            analysisDTO.setCurrent(a);
            analysisDTO.setTotalTime(b);
            analysisDTO.setDay(c);
            analysisDTO.setIntotalPass(d);
            analysisDTO.setAdvice(advice);
            return Result.success(analysisDTO);
        }
    }
}