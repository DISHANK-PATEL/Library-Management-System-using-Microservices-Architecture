package com.library.fineservice.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, HttpServletRequest request) {
        log.error("Feign call failed: status={}, message={}", ex.status(), ex.getMessage());

        if (ex.status() == 404) {
            return buildResponse(HttpStatus.NOT_FOUND, "Not Found",
                    "Requested resource not found in downstream service.", request);
        }
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "Downstream service is unavailable. Please try again later.", request);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(
            WebClientResponseException ex, HttpServletRequest request) {
        log.error("WebClient response error: status={}, body={}",
                ex.getStatusCode(), ex.getResponseBodyAsString());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "Failed to fetch data from downstream service: " + ex.getStatusCode(), request);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequest(
            WebClientRequestException ex, HttpServletRequest request) {
        log.error("WebClient connection failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "Cannot connect to downstream service. Please try again later.", request);
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ErrorResponse> handleCompletionException(
            CompletionException ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        log.error("CompletableFuture failed: {}", cause != null ? cause.getMessage() : ex.getMessage());

        if (cause instanceof FeignException feignEx) {
            return handleFeignException(feignEx, request);
        }
        if (cause instanceof ResourceNotFoundException notFoundEx) {
            return handleNotFound(notFoundEx, request);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Parallel service call failed: " + (cause != null ? cause.getMessage() : "Unknown"),
                request);
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ErrorResponse> handleRabbitException(
            AmqpException ex, HttpServletRequest request) {
        log.error("RabbitMQ error: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Messaging Error",
                "Fine created but notification could not be sent.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}