package com.bmgf.controller;
import com.bmgf.po.Result;
import com.bmgf.po.Post;
import com.bmgf.service.impl.PostService;
import com.bmgf.service.impl.StatsService;
import com.bmgf.service.impl.UserService;
import com.bmgf.service.impl.VulnContainerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/post")
@CrossOrigin
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private VulnContainerService containerService;

    @Autowired
    private UserService userService;

    @Autowired
    private StatsService statsService;

    // 1. 查询全部帖子
    @GetMapping("/findAll")
    public Result findAllPosts() {
        List<Post> posts = postService.findAllPosts();
        return Result.success(posts);
    }

    // 2. 根据ID查询帖子
    @GetMapping("/findById")
    public Result findPostById(@RequestParam String id) {
        return Result.success(postService.findById(id));
    }

    // 3. 按板块查询帖子
    @GetMapping("/findBySection")
    public Result findBySection(@RequestParam String section) {
        List<Post> posts = postService.findBySection(section);
        return Result.success(posts);
    }

    // 4. 搜索帖子
    @GetMapping("/search")
    public Result searchPosts(@RequestParam String keyword) {
        List<Post> posts = postService.searchPosts(keyword);
        return Result.success(posts);
    }

    // 5. 获取热门帖子
    @GetMapping("/hot")
    public Result getHotPosts(@RequestParam(defaultValue = "5") int limit) {
        List<Post> posts = postService.findHotPosts(limit);
        return Result.success(posts);
    }

    // 6. 统计各板块帖子数量
    @GetMapping("/countBySection")
    public Result countBySection() {
        return Result.success(postService.countPostBySection());
    }

    // 7. 创建帖子
    @PostMapping("/insertPost")
    public Result insertPost(@Valid @RequestBody Post post, @RequestHeader("Authorization") String authHeader) {
        // 强制使用当前用户ID
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = containerService.getUsernameFromToken(token);
        if (userService.findByUsername(username)!=null){
            post.setAuthorId(userService.findByUsername(username).getId());
        }else {
            return Result.error("没有查到此用户id");
        }
        Post pos= postService.createPost(post);
        statsService.incrementUserPostCount(username);
        return Result.success(pos);
    }

    // 8. 删除帖子
    @DeleteMapping("/delete/{id}")
    public Result deletePost(@PathVariable String id, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = containerService.getUsernameFromToken(token);
        Post post = postService.findById(id);

        if (post == null) {
            return Result.error("帖子不存在");
        }
        // 检查是否是帖子作者
        if (!post.getAuthorId().equals(userService.findByUsername(username).getId())) {
            return Result.error("无权删除此帖子");
        }

        postService.deletePost(post);
        return Result.success();
    }

    // 9. 更新帖子
    @PutMapping("/update")
    public Result updatePost(@Valid @RequestBody Post post, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = containerService.getUsernameFromToken(token);
        // 检查是否是帖子作者
        if (!post.getAuthorId().equals(userService.findByUsername(username).getId())) {
            return Result.error("无权修改此帖子");
        }
        postService.savePost(post);
        return Result.success();
    }
}