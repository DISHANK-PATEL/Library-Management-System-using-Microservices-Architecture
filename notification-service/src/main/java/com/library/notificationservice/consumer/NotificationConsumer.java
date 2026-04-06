package com.library.notificationservice.consumer;

import com.library.notificationservice.config.RabbitMQConfig;
import com.library.notificationservice.dto.LibraryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleEvent(LibraryEvent event) {
        log.info("Received event: {}", event.getEventType());

        String subject;
        String body;

        switch (event.getEventType()) {
            case "LOAN_CREATED" -> {
                subject = "Book Borrowed Successfully";
                body = String.format(
                        "Hello,\n\nYou have successfully borrowed '%s'.\nDue date: %s\n\nPlease return it on time to avoid fines.\n\nLibrary Management System",
                        event.getBookTitle(), event.getDueDate());
            }
            case "BOOK_RETURNED" -> {
                subject = "Book Returned Successfully";
                body = String.format(
                        "Hello,\n\nYou have successfully returned '%s'.\nThank you for returning on time!\n\nLibrary Management System",
                        event.getBookTitle());
            }
            case "FINE_GENERATED" -> {
                subject = "Overdue Fine Alert";
                body = String.format(
                        "Hello,\n\nA fine of $%s has been generated for loan #%s.\nPlease pay at the earliest.\n\nLibrary Management System",
                        event.getAmount(), event.getLoanId());
            }
            default -> {
                log.warn("Unknown event type: {}", event.getEventType());
                return;
            }
        }

        String to = event.getMemberEmail();
        if (to == null || to.isEmpty()) {
            log.warn("No email address for event: {}", event.getEventType());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Email sent to {} — Subject: {}", to, subject);

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}