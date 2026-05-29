package com.bmgf.controller;
import com.bmgf.DTO.ExamRecordDTO;
import com.bmgf.dao.*;
import com.bmgf.exception.ResourceNotFoundException;
import com.bmgf.po.*;
import com.bmgf.service.impl.VulnContainerService;
import com.bmgf.util.JwtUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.bmgf.po.ExamRecord;
@RestController
@RequestMapping("/api")
public class QuestionController {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnswerRecordRepository answerRecordRepository;
    @Autowired
    private ExamRecordRepository examRecordRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private JwtUtil jwtUtil;

@GetMapping("/questions")
public Result getRandomQuestions(@RequestHeader("Authorization") String authHeader) {
    // 增强类型安全校验
    String token = authHeader.substring(7); // 去掉"Bearer "前缀
    String username = jwtUtil.getUsernameFromToken(token);
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    String userId =user.getId();
    // 获取用户已答过的题目ID
    User user1 = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    Set<String> completedQuestions = new HashSet<>(user.getCompletedQuestions());
    // 排除已答题目，随机抽取10道题
    List<Question> questions = questionRepository.findRandomQuestionsExcluding(completedQuestions, 10);
    System.out.println("1111111111"+questions.toString());
    return Result.success(questions);
}
@GetMapping("/more_questions")
    public Result getRandomMoreQuestions(@RequestHeader("Authorization") String authHeader) {
        // 增强类型安全校验
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        String userId =user.getId();
        // 获取用户已答过的题目ID
        User user1 = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        Set<String> completedQuestions = new HashSet<>(user.getCompletedQuestions());
        // 排除已答题目，随机抽取100道题
        List<Question> questions = questionRepository.findRandomQuestionsExcluding(completedQuestions, 100);
        System.out.println("1111111111"+questions.toString());
        return Result.success(questions);
    }
    @GetMapping("/questions_category")
    public Result getRandomQuestionsCategory(@RequestParam String category) {
        List<Question>questions = questionRepository.findByCategory(category);
        if (questions.isEmpty()) {
            return Result.error("题库咱时没有该类别");
        }else {
            return Result.success(questions);
        }
    }

    @GetMapping("/get_challenge")
    public Result getRandomChallenge() {
        Category category = categoryRepository.findAll().get(0);
        return Result.success(category);
    }
    @PostMapping("/submit")
    public Result submitExam(@RequestBody ExamSubmissionRequest request,
                             @RequestHeader("Authorization") String authHeader) {
        // 1. 获取用户信息
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        String userId = user.getId();

        // 2. 初始化响应结构
        List<QuestionResult> questionResults = new ArrayList<>();
        int totalScoreDelta = 0;
        Set<String> newCompletedQuestions = new HashSet<>();

        // 3. 创建考试记录
        ExamRecord examRecord = new ExamRecord();
        examRecord.setUserId(userId);
        examRecord.setExamId(request.getExamId());
        examRecord.setSubmitTime(LocalDateTime.now());
        examRecord.setCostTime(request.getCostTime());
        examRecord.setQuestionResults(new ArrayList<>());
        // 4. 遍历所有题目答案
        for (Map.Entry<String, String> entry : request.getAnswers().entrySet()) {
            String questionId = entry.getKey();
            String userAnswer = entry.getValue();

            // 4.1 检查是否重复答题
            if (user.getCompletedQuestions().contains(questionId)) {
                QuestionResult result = new QuestionResult(
                        questionId,
                        "重复提交的题目",  // 题目标题
                        userAnswer,      // 用户答案
                        "重复提交",       // 正确答案
                        false,          // 是否正确
                        0,             // 得分
                        "该题目已经完成"  // 解析
                );
                questionResults.add(result);
                examRecord.getQuestionResults().add(result);
                continue;
            }

            // 4.2 验证题目存在性
            Question question = questionRepository.findById(questionId)
                    .orElseGet(() -> {
                        QuestionResult result = new QuestionResult(
                                questionId,
                                "不存在的题目",  // 题目标题
                                userAnswer,      // 用户答案
                                "题目不存在",     // 正确答案
                                false,          // 是否正确
                                0,             // 得分
                                "该题目不存在"    // 解析
                        );
                        questionResults.add(result);
                        examRecord.getQuestionResults().add(result);
                        return null;
                    });
            if (question == null) continue;

            // 4.3 校验答案
            boolean isCorrect = question.getAnswer().equalsIgnoreCase(userAnswer);
            int scoreDelta = isCorrect ? question.getScore() : 0;

            // 4.4 记录结果
            QuestionResult result = new QuestionResult(
                    questionId,
                    question.getTitle(),           // 题目标题
                    userAnswer,                    // 用户答案
                    question.getAnswer(),          // 正确答案
                    isCorrect,                     // 是否正确
                    scoreDelta,                    // 得分
                    isCorrect ? "回答正确" : "回答错误，请查看正确答案"  // 解析
            );
            questionResults.add(result);
            examRecord.getQuestionResults().add(result);

            // 4.5 更新统计
            totalScoreDelta += scoreDelta;
            newCompletedQuestions.add(questionId);
        }

        // 5. 设置考试记录的总分
        examRecord.setTotalScore(totalScoreDelta);
        examRecord.setCorrectCount((int) questionResults.stream().filter(QuestionResult::isCorrect).count());

        // 6. 保存考试记录
        examRecordRepository.save(examRecord);

        // 7. 更新用户数据
        if (!newCompletedQuestions.isEmpty()) {
            userRepository.updateUserStats(
                    userId,
                    totalScoreDelta,
                    (int) questionResults.stream().filter(QuestionResult::isCorrect).count(),
                    newCompletedQuestions
            );
        }

        // 8. 构建响应
        return Result.success(new ExamResult(
                totalScoreDelta,
                user.getTotalScore() + totalScoreDelta,
                questionResults
        ));
    }
    // 将 QuestionResult 改为静态内部类，并添加序列化注解
    @Data
    @NoArgsConstructor  // 添加无参构造函数
    @AllArgsConstructor // 保留全参构造函数
    public static class QuestionResult {
        private String questionId;
        private String questionTitle;
        private String userAnswer;
        private String correctAnswer;
        private boolean correct;
        private int score;
        private String explanation;
    }

    // 添加 ExamRecordDTO 类
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamRecordDTO {
        private String id;
        private String examId;
        private LocalDateTime submitTime;
        private int totalScore;
        private int correctCount;
        private int totalQuestions;
        private double accuracy;
        private String examTitle;
    }

    @GetMapping("/exam-records")
    public Result getUserExamRecords(@RequestHeader("Authorization") String authHeader) {
        try {
            // 1. 获取用户信息
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
            String userId = user.getId();

            // 2. 查询用户的考试记录
            List<ExamRecord> records = examRecordRepository.findByUserIdOrderBySubmitTimeDesc(userId);

            // 3. 处理返回数据
            List<ExamRecordDTO> recordDTOs = records.stream()
                    .map(record -> new ExamRecordDTO(
                            record.getId(),
                            record.getExamId(),
                            record.getSubmitTime(),
                            record.getTotalScore(),
                            record.getCorrectCount(),
                            record.getQuestionResults().size(),
                            calculateAccuracy(record),
                            formatExamTitle(record.getExamId())
                    ))
                    .collect(Collectors.toList());

            return Result.success(recordDTOs);
        } catch (Exception e) {
            e.printStackTrace(); // 添加错误堆栈打印
            return Result.error("获取考试记录失败：" + e.getMessage());
        }
    }
    // 添加新的获取考试详情接口
    @GetMapping("/exam-records/{recordId}")
    public Result getExamRecordDetail(
            @PathVariable String recordId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

            ExamRecord record = examRecordRepository.findById(recordId)
                    .orElseThrow(() -> new ResourceNotFoundException("考试记录不存在"));

            // 验证记录是否属于当前用户
            if (!record.getUserId().equals(user.getId())) {
                return Result.error("无权访问此考试记录");
            }

            ExamRecordDetailDTO detailDTO = new ExamRecordDetailDTO(
                    record.getId(),
                    formatExamTitle(record.getExamId()),
                    record.getSubmitTime(),
                    record.getTotalScore(),
                    calculateAccuracy(record),
                    record.getQuestionResults().stream()
                            .map(result -> new QuestionResultDTO(
                                    result.getQuestionId(),
                                    result.getQuestionTitle(),
                                    result.getUserAnswer(),
                                    result.getCorrectAnswer(),
                                    result.isCorrect(),
                                    result.getScore(),
                                    result.getExplanation()
                            ))
                            .collect(Collectors.toList())
            );

            return Result.success(detailDTO);
        } catch (Exception e) {
            return Result.error("获取考试详情失败：" + e.getMessage());
        }
    }

    // 计算正确率
    private double calculateAccuracy(ExamRecord record) {
        if (record.getQuestionResults() == null || record.getQuestionResults().isEmpty()) {
            return 0.0;
        }
        return (double) record.getCorrectCount() / record.getQuestionResults().size() * 100;
    }

    // 格式化考试标题
    private String formatExamTitle(String examId) {
        // 示例：将 "challengel1-basic-card-1" 转换为 "挑战1-基础测试-第1套"
        String[] parts = examId.split("-");
        if (parts.length >= 3) {
            String challenge = parts[0].replace("challengel", "挑战");
            String type = parts[1].equals("basic") ? "基础测试" : "进阶测试";
            String card = "第" + parts[2].replace("card-", "") + "套";
            return String.format("%s-%s-%s", challenge, type, card);
        }
        return examId;
    }

    // 添加新的详情DTO类
    @Data
    @AllArgsConstructor
    class ExamRecordDetailDTO {
        private String id;
        private String examTitle;
        private LocalDateTime submitTime;
        private int totalScore;
        private double accuracy;
        private List<QuestionResultDTO> questionResults;
    }

    @Data
    @AllArgsConstructor
    class QuestionResultDTO {
        private String questionId;
        private String questionTitle;
        private String userAnswer;
        private String correctAnswer;
        private boolean correct;
        private int score;
        private String explanation;
    }
    // 修改请求类
    @Data
    static class ExamSubmissionRequest {
        @NotBlank(message = "考试ID不能为空")
        private String examId;
        @NotEmpty(message = "至少需要一道题的答案")
        private Map<String, String> answers; // questionId -> userAnswer
        private Integer costTime;
    }
    // 响应结构保持不变
    @Data
    @AllArgsConstructor
    static class ExamResult {
        private int scoreDelta;
        private int totalScore;
        private List<QuestionResult> details;
    }
}
