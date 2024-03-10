package com.fyp.health_sync.service;


import com.fyp.health_sync.exception.InternalServerErrorException;
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

    public void sendMail(String to, String subject, String text) throws InternalServerErrorException {


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
            throw new InternalServerErrorException(e.getMessage());
        }


    }

    @Async
    public void sendEmail(String name, String email, String otp, String title, String request) throws InternalServerErrorException {
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

    @Async
    public void sendRequestEmail(String type, String reason, String name) throws InternalServerErrorException {
        String subject = "Health sync - "+ type;
        String message = generateRequestEmailTemplate(reason,name);
        sendMail(from, subject, message);
    }

    private String generateRequestEmailTemplate(String reason, String name) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>User Message Request</title>\n" +
                "<style>\n" +
                "  /* Reset styles */\n" +
                "  body, html {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    font-family: Arial, sans-serif;\n" +
                "  }\n" +
                "  \n" +
                "  /* Container styles */\n" +
                "  .container {\n" +
                "    max-width: 600px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 20px;\n" +
                "    border: 1px solid #ccc;\n" +
                "    border-radius: 5px;\n" +
                "  }\n" +
                "\n" +
                "  /* Heading styles */\n" +
                "  h1 {\n" +
                "    text-align: center;\n" +
                "    color: #092C4C;\n" +
                "  }\n" +
                "\n" +
                "  /* Message styles */\n" +
                "  .message {\n" +
                "    padding: 20px;\n" +
                "    background-color: #f2f2f2;\n" +
                "    border-radius: 5px;\n" +
                "    margin-top: 20px;\n" +
                "  }\n" +
                "\n" +
                "  /* Footer styles */\n" +
                "  .footer {\n" +
                "    margin-top: 20px;\n" +
                "    text-align: center;\n" +
                "  }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h1>User Request</h1>\n" +
                "    <p>Dear Admin,</p>\n" +
                "    <div class=\"message\">\n" +
                "      <p><strong>User Name:</strong> "+ name +"</p> " +
                "      <p><strong>Reason:</strong></p>\n" +
                "      <p>"+reason+"</p>\n" +
                "    </div>\n" +

                "  </div>\n" +
                "</body>\n" +
                "</html>\n";
    }

    @Async
    public void sendApprovalRequestEmail(String name) throws InternalServerErrorException {
        String subject = "Health sync - Request Account Approval";
        String message = generateRequestApprovalEmailTemplate(name);
        sendMail(from, subject, message);
    }

    private String generateApprovalEmailTemplate(String name) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>Account Approval</title>\n" +
                "<style>\n" +
                "  /* Reset styles */\n" +
                "  body, html {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    font-family: Arial, sans-serif;\n" +
                "  }\n" +
                "  \n" +
                "  /* Container styles */\n" +
                "  .container {\n" +
                "    max-width: 600px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 20px;\n" +
                "    border: 1px solid #ccc;\n" +
                "    border-radius: 5px;\n" +
                "  }\n" +
                "\n" +
                "  /* Heading styles */\n" +
                "  h1 {\n" +
                "    text-align: center;\n" +
                "    color: #092C4C;\n" +
                "  }\n" +
                "\n" +
                "  /* Message styles */\n" +
                "  .message {\n" +
                "    padding: 20px;\n" +
                "    background-color: #f2f2f2;\n" +
                "    border-radius: 5px;\n" +
                "    margin-top: 20px;\n" +
                "  }\n" +
                "\n" +
                "  /* Footer styles */\n" +
                "  .footer {\n" +
                "    margin-top: 20px;\n" +
                "    text-align: center;\n" +
                "  }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container   \">\n" +
                "    <h1>Account Approval</h1>\n" +
                "    <p>Dear " + name + ",</p>\n" +
                "    <div class=\"message\">\n" +
                "      <p>Your account has been approved by the admin. You can now login to your account and start using our services.</p>\n" +
                "    </div>\n" +

                "  </div>\n" +
                "</body>\n" +
                "</html>\n";
    }

    private String generateRequestApprovalEmailTemplate(String name) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>Request Account Approval</title>\n" +
                "<style>\n" +
                "  /* Reset styles */\n" +
                "  body, html {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    font-family: Arial, sans-serif;\n" +
                "  }\n" +
                "  \n" +
                "  /* Container styles */\n" +
                "  .container {\n" +
                "    max-width: 600px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 20px;\n" +
                "    border: 1px solid #ccc;\n" +
                "    border-radius: 5px;\n" +
                "  }\n" +
                "\n" +
                "  /* Heading styles */\n" +
                "  h1 {\n" +
                "    text-align: center;\n" +
                "    color: #092C4C;\n" +
                "  }\n" +
                "\n" +
                "  /* Message styles */\n" +
                "  .message {\n" +
                "    padding: 20px;\n" +
                "    background-color: #f2f2f2;\n" +
                "    border-radius: 5px;\n" +
                "    margin-top: 20px;\n" +
                "  }\n" +
                "\n" +
                "  /* Footer styles */\n" +
                "  .footer {\n" +
                "    margin-top: 20px;\n" +
                "    text-align: center;\n" +
                "  }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container   \">\n" +
                "    <h1>Account Approval</h1>\n" +
                "    <p>Dear Admin,</p>\n" +
                "    <div class=\"message\">\n" +
                "      <p> User name: "+ name+" </p>\n" +
                "      <p> Approve my account i have made changes in my account. </p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>\n";
    }
}
