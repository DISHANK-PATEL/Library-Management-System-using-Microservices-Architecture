package com.library.loanservice.service;

import com.library.loanservice.config.RabbitMQConfig;
import com.library.loanservice.dto.LoanEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishLoanCreated(LoanEventMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.LOAN_CREATED_KEY,
                message
        );
        log.info("Published LOAN_CREATED event: loanId={}", message.getLoanId());
    }

    public void publishLoanReturned(LoanEventMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.LOAN_RETURNED_KEY,
                message
        );
        log.info("Published LOAN_RETURNED event: loanId={}", message.getLoanId());
    }
}