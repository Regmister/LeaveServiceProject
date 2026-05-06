package com.staffs.leaveservice.constants;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:text.properties")
public class ConstantsProvider {

    // // Error Messages

    // Security Service

    @Getter
    @Value("${error.login.failure}")
    private String ERROR_LOGIN_FAILURE;

    @Getter
    @Value("${error.jwt.failure}")
    private String ERROR_JWT_FAILURE;

    @Getter
    @Value("${error.role.failure}")
    private String ERROR_ROLE_FAILURE;

    @Getter
    @Value("${error.user.failure}")
    private String ERROR_USER_FAILURE;

    @Getter
    @Value("${error.default.pass}")
    private String ERROR_DEFAULT_PASS;

    // Leave Service

    @Getter
    @Value("${error.leave.request.not.found}")
    private String ERROR_LEAVE_REQUEST_NOT_FOUND;

    @Getter
    @Value("${error.leave.invalid.dates}")
    private String ERROR_LEAVE_INVALID_DATES;

    @Getter
    @Value("${error.leave.invalid.leave.balance}")
    private String ERROR_LEAVE_INVALID_LEAVE_BALANCE;

    // Error Service

    @Getter
    @Value("${error.service.invalid.body}")
    private String ERROR_SERVICE_INVALID_BODY;

    @Getter
    @Value("${error.service.invalid.method}")
    private String ERROR_SERVICE_INVALID_METHOD;

    // // Success Messages

    // Security Service

    @Getter
    @Value("${success.login}")
    private String SUCCESS_LOGIN;

    @Getter
    @Value("${success.created.user}")
    private String SUCCESS_CREATED_USER;

    @Getter
    @Value("${success.changed.pass}")
    private String SUCCESS_CHANGED_PASSWORD;

    // Leave Service

    @Getter
    @Value("${success.leave.request}")
    private String SUCCESS_LEAVE_REQUEST;

    @Getter
    @Value("${success.approval.leave.request}")
    private String SUCCESS_APPROVAL_LEAVE_REQUEST;

    @Getter
    @Value("${success.get.all.employee.leaves}")
    private String SUCCESS_GET_ALL_EMPLOYEE_LEAVE;

    @Getter
    @Value("${success.get.total.leave.days}")
    private String SUCCESS_GET_TOTAL_LEAVE_DAYS;

    // // Database Messages

    // Leave Table

    @Getter
    @Value("${leave.service.leave.status.pending}")
    private String LEAVE_REQUEST_PENDING;

    @Getter
    @Value("${leave.service.leave.status.declined}")
    private String LEAVE_REQUEST_DECLINED;

    @Getter
    @Value("${leave.service.leave.status.approved}")
    private String LEAVE_REQUEST_APPROVED;

    @Getter
    @Value("${leave.service.leave.status.cancelled}")
    private String LEAVE_REQUEST_CANCELLED;
}

