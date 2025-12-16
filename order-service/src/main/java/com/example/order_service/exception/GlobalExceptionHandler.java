package com.example.order_service.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeignException(FeignException ex) {
        log.error("External Service Error: ", ex);

        if (ex.status() == 404) {
            log.error("Product not found in Product Service");
            return new ResponseEntity<>("Order Failed: Product Not Found", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Order Failed: Downstream Service Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}