package com.library.notificationservice.consumer;

import com.library.notificationservice.config.RabbitMQConfig;
import com.library.notificationservice.dto.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleEvent(LibraryEvent event) {
        log.info("Received event: {}", event.getEventType());

        String subject;
        String body;
        String to = event.getMemberEmail();

        switch (event.getEventType()) {
            case "LOAN_CREATED" -> {
                subject = "Book Borrowed Successfully";
                body = String.format(
                        "Hello,\n\nYou have borrowed '%s'.\nDue date: %s\n\nLibrary Management System",
                        event.getBookTitle(), event.getDueDate());
            }
            case "BOOK_RETURNED" -> {
                subject = "Book Returned Successfully";
                body = String.format(
                        "Hello,\n\nYou have returned '%s'.\nThank you!\n\nLibrary Management System",
                        event.getBookTitle());
            }
            case "FINE_GENERATED" -> {
                subject = "Overdue Fine Alert";
                body = String.format(
                        "Hello,\n\nA fine of $%s has been generated for loan #%s.\n\nLibrary Management System",
                        event.getAmount(), event.getLoanId());
            }
            default -> {
                log.warn("Unknown event type: {}", event.getEventType());
                return;
            }
        }

        if (mailSender != null && to != null && !to.isEmpty()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("library@example.com");
            mailSender.send(message);
            log.info("Email sent to {} — Subject: {}", to, subject);
        } else {
            log.info("========== EMAIL NOTIFICATION (LOGGED) ==========");
            log.info("TO: {}, SUBJECT: {}, BODY: {}", to, subject, body);
            log.info("=================================================");
        }
    }
}