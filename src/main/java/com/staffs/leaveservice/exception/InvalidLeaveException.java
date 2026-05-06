package com.staffs.leaveservice.exception;

public class InvalidLeaveException extends RuntimeException {
    public InvalidLeaveException(String message) {
        super(message);
    }
}
