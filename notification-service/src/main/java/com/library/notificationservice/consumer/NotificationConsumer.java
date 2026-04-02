package com.library.notificationservice.consumer;

import com.library.notificationservice.config.RabbitMQConfig;
import com.library.notificationservice.dto.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleEvent(LibraryEvent event) {
        log.info("========== EMAIL NOTIFICATION ==========");
        log.info("Received event: {}", event.getEventType());

        switch (event.getEventType()) {
            case "LOAN_CREATED" -> {
                log.info("TO: {}", event.getMemberEmail());
                log.info("SUBJECT: Book Borrowed Successfully");
                log.info("BODY: You have borrowed '{}'. Due date: {}",
                        event.getBookTitle(), event.getDueDate());
            }
            case "BOOK_RETURNED" -> {
                log.info("TO: {}", event.getMemberEmail());
                log.info("SUBJECT: Book Returned");
                log.info("BODY: You have returned '{}'. Thank you!",
                        event.getBookTitle());
            }
            case "FINE_GENERATED" -> {
                log.info("TO: {}", event.getMemberEmail());
                log.info("SUBJECT: Overdue Fine Alert");
                log.info("BODY: A fine of ${} has been generated for loan #{}",
                        event.getAmount(), event.getLoanId());
            }
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }

        log.info("=========================================");
    }
}