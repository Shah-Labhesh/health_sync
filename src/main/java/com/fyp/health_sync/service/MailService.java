package com.fyp.health_sync.service;


import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String text) {


        JavaMailSenderImpl senderImpl = (JavaMailSenderImpl) mailSender;
        Properties properties = senderImpl.getJavaMailProperties();

        try {
            Session session = Session.getInstance(properties);
            MimeMessage email = new MimeMessage(session);
            email.setFrom(from);
            email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
            email.setSubject(subject);
            MimeMultipart multipart = new MimeMultipart("related");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(text, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            email.setContent(multipart);
            Transport transport = session.getTransport(senderImpl.getProtocol());
            transport.connect(senderImpl.getHost(), senderImpl.getPort(), senderImpl.getUsername(), senderImpl.getPassword());
            transport.sendMessage(email, email.getAllRecipients());
            transport.close();
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
            throw new RuntimeException(e);
        }


    }

    @Async
    public void sendEmail(String name, String email, String otp, String title, String request) {
        String subject = "Health sync - "+ title;
        String message = generateEmailTemplate(title,request, name, otp);
        sendMail(email, subject, message);
    }

    private String generateEmailTemplate(String title, String request, String name, String otp) {
        return "<div style='background-color: #f5f5f5; padding: 30px 0;'>\n" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px;'>\n" +
                "    <div style='padding: 40px 30px; border-bottom: 1px solid #dddddd; text-align: center;'>\n" +
                "      <h2 style='margin: 0; font-size: 28px; font-weight: 700; color: #333333;'>Hello " + name + "</h2>\n" +
                "      <p style='margin: 0; font-size: 18px; line-height: 1.5; color: #555555;'>"+ title +"</p>\n" +
                "      <p style='margin: 20px 0 40px 0; font-size: 18px; line-height: 1.5; color: #555555;'>Please use the following OTP to "+request+":</p>\n" +
                "      <div style='background-color: #007bff; border-radius: 5px; display: inline-block; padding: 10px 20px; font-size: 24px; font-weight: bold; color: #ffffff;'>" + otp + "</div>\n" +
                "    </div>\n" +
                "    <div style='padding: 40px 30px; text-align: center;'>\n" +
                "      <p style='margin: 0; font-size: 16px; line-height: 1.5; color: #555555;'>If you have any questions or need further assistance, please feel free to contact our customer service.</p>\n" +
                "    </div>\n" +
                "    <div style='padding: 30px; background-color: #f0f0f0; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;'>\n" +
                "      <p style='margin: 0; font-size: 16px; line-height: 1.5; color: #888888; text-align: center;'>Thank you for using HealthSync</p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n";
    }
}
