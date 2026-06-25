package com.project.anhgagifcode.domain.exception;

public abstract class BaseDomainException extends RuntimeException {
    public BaseDomainException(String message) {
        super(message);
    }
}