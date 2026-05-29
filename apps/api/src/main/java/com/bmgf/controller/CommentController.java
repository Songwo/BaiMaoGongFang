package com.bmgf.controller;
import com.bmgf.po.Comment;
import com.bmgf.po.Result;
import com.bmgf.service.impl.StatsService;
import com.bmgf.service.impl.UserService;
import com.bmgf.service.impl.VulnContainerService;
import com.bmgf.service.impl.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin(origins = "http://54.193.5.242", methods = {RequestMethod.GET, RequestMethod.POST,RequestMethod.PUT}, allowCredentials = "true")
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;
    // 2. 根据postId查询最新评论
    @GetMapping("/find/{postId}")
    public Result findLatestComment(@Valid @PathVariable String postId) {
        return Result.success(commentService.getCommentsByPostId(postId));
    }
    @Autowired
    private VulnContainerService containerService;
    @Autowired
    private UserService userService;
    @Autowired
    private StatsService statsService;
    // 3. 插入评论
    @PostMapping("/insert")
    public Result insertComment(@Valid @RequestBody Comment comment, @RequestHeader("Authorization") String authHeader) {
        // 从安全上下文获取当前用户ID
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = containerService.getUsernameFromToken(token);
        comment.setAuthorId(userService.findByUsername(username).getId());
        Comment com=commentService.addComment(comment);
        statsService.incrementUserCommentCount(username);
        return Result.success(com);
    }
}

