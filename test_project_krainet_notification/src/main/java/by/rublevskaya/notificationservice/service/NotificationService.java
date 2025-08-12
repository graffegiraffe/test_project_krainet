package by.rublevskaya.notificationservice.service;

import by.rublevskaya.notificationservice.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendNotification(NotificationRequest request) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(request.getEmail());
            helper.setSubject(request.getSubject());
            helper.setText(request.getMessage(), true);
            mailSender.send(mimeMessage);
            log.info("Email sent to {}", request.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }
}

