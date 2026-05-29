package com.bmgf.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${frontend.url}")
    private String frontend;

    @Value("${bmgf.logo.url}")
    private String logoUrl;

    public void sendVerificationEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置发件人信息
            helper.setFrom("3381485209@qq.com", "白帽工坊安全团队");
            helper.setTo(to);
            helper.setSubject("【白帽工坊】邮箱验证 - 安全确认");

            // 构建HTML格式的邮件内容
            String verificationLink = frontend + token;
            String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { text-align: center; padding: 20px 0; }
                    .logo { max-width: 150px; height: auto; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 5px; }
                    .button { 
                        display: inline-block;
                        padding: 12px 24px;
                        background-color: #4CAF50;
                        color: white;
                        text-decoration: none;
                        border-radius: 4px;
                        margin: 20px 0;
                    }
                    .footer { 
                        text-align: center;
                        margin-top: 20px;
                        padding-top: 20px;
                        border-top: 1px solid #eee;
                        font-size: 12px;
                        color: #666;
                    }
                    .warning { 
                        background-color: #fff3cd;
                        border: 1px solid #ffeeba;
                        color: #856404;
                        padding: 10px;
                        border-radius: 4px;
                        margin: 10px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="https://www.wacyg.fun/avatar/6a103249-10fc-422d-883a-c5cf4bca4364_%E4%BF%A1%E6%81%AF.png" alt="白帽工坊" class="logo">
                        <h2>白帽工坊 - 网络安全学习平台</h2>
                    </div>
                    
                    <div class="content">
                        <h3>尊敬的会员：</h3>
                        <p>感谢您注册白帽工坊！为了确保您的账户安全，请验证您的邮箱地址。</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">验证邮箱地址</a>
                        </div>
                        
                        <p>或者复制以下链接到浏览器地址栏：</p>
                        <p style="word-break: break-all;">%s</p>
                        
                        <div class="warning">
                            <strong>安全提示：</strong>
                            <ul>
                                <li>此链接有效期为24小时</li>
                                <li>请勿将验证链接分享给他人</li>
                                <li>如非本人操作，请忽略此邮件</li>
                            </ul>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿直接回复</p>
                        <p>© 2025 白帽工坊 版权所有</p>
                        <p>ICP备2025024694号</p>
                    </div>
                </div>
            </body>
            </html>
            """.replace("%s", verificationLink);

            helper.setText(htmlContent, true);

            // 设置邮件优先级
            message.setHeader("X-Priority", "1");
            message.setHeader("X-MSMail-Priority", "High");
            message.setHeader("X-Mailer", "白帽工坊邮件系统");

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("发送验证邮件失败:"+e.getMessage());
//            log.error("发送验证邮件失败: {}", e.getMessage());
            throw new RuntimeException("发送验证邮件失败", e);
        }
    }
}
