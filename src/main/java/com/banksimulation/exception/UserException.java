package com.banksimulation.exception;

/**
 * 用户相关业务异常
 * Custom exception for user-related business logic errors.
 */
public class UserException extends Exception {
    public UserException(String message) {
        super(message);
    }
}
