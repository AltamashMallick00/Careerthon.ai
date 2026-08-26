package com.careerthon.service;

import com.careerthon.model.LmsNotification;
import com.careerthon.model.User;
import com.careerthon.repository.LmsNotificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class NotificationService {

    private final LmsNotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public NotificationService(LmsNotificationRepository notificationRepository, JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    public void sendNotification(User user, String title, String message, boolean sendEmail, String userEmail) {
        // Save to DB
        LmsNotification notification = new LmsNotification(user, title, message, "COURSE_UPDATE");
        notificationRepository.save(notification);

        // Send Email
        if (sendEmail && userEmail != null && !userEmail.isEmpty()) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(userEmail);
                mailMessage.setSubject("Careerthon.AI Update: " + title);
                mailMessage.setText(message);
                mailSender.send(mailMessage);
            } catch (Exception e) {
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
    }

    public List<LmsNotification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByTargetUserIdOrTargetUserIsNullOrderByCreatedAtDesc(userId).stream()
                .filter(n -> !n.isRead())
                .toList();
    }
    
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
