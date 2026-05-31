package com.campus.campus_life.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender sender;
    public EmailService(JavaMailSender sender) { this.sender = sender; }

    public void sendCode(String to, String code) throws MessagingException {
        MimeMessage msg = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setFrom("3435999576@qq.com");
        helper.setTo(to);
        helper.setSubject("校园助手 - 验证码");
        helper.setText("您的验证码：" + code + "，5分钟内有效", true);
        sender.send(msg);
    }
}