package com.banksimulation.exception;

/**
 * 权限不足异常
 * Custom exception thrown when a user does not have sufficient permissions to perform an action.
 */
public class PermissionDeniedException extends Exception {
    public PermissionDeniedException(String message) {
        super(message);
    }
}
