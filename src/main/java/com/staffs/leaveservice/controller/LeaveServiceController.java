package com.staffs.leaveservice.controller;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.request.*;
import com.staffs.leaveservice.dto.response.*;
import com.staffs.leaveservice.exception.RateLimitException;
import com.staffs.leaveservice.service.LeaveService;
import com.staffs.leaveservice.service.RateLimiterService;
import com.staffs.leaveservice.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RequestMapping("/leave-service")
@Controller
public class LeaveServiceController {

    @Autowired
    private ConstantsProvider constantsProvider;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponseDto>> handleLogin(@Valid @RequestBody LoginRequestDto request, HttpServletRequest httpRequest){
        String clientIp = httpRequest.getRemoteAddr();
        if (!rateLimiterService.isAllowed(clientIp)) {
            throw new RateLimitException(constantsProvider.getERROR_TOO_MANY_REQUESTS());
        }
        return securityService.handleLogin(request);
    }

    @PostMapping("/create-user")
    public ResponseEntity<ResponseDto<LoginResponseDto>> handleCreateUser(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody CreateUserRequestDto request){
        return securityService.handleCreateUser(request, authHeader);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ResponseDto<ChangePasswordResponseDto>> handleChangePassword(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody ChangePasswordRequestDto request){
        return securityService.handleChangePassword(request, authHeader);
    }

    @PostMapping("/leave-request")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveRequest(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody CreateLeaveRequestDto request){
        return leaveService.handleLeaveRequest(request, authHeader);
    }

    @DeleteMapping("/leave-request")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveRequestDeletion(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody ChangeLeaveRequestStatusDto request){
        return leaveService.handleChangeLeaveRequestStatus(request, constantsProvider.getLEAVE_REQUEST_CANCELLED(), authHeader);
    }

    @PatchMapping("/leave-request/approve")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveApprove(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody ChangeLeaveRequestStatusDto request) {
        return leaveService.handleChangeLeaveRequestStatus(request, constantsProvider.getLEAVE_REQUEST_APPROVED(), authHeader);
    }

    @PatchMapping("/leave-request/decline")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveDecline(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody ChangeLeaveRequestStatusDto request) {
        return leaveService.handleChangeLeaveRequestStatus(request, constantsProvider.getLEAVE_REQUEST_DECLINED(), authHeader);
    }

    @GetMapping("/leave-request/status/{id}")
    public ResponseEntity<ResponseDto<List<LeaveResponseDto>>> handleGetLeaveStatus(
            @RequestHeader("Authorisation") String authHeader,
            @PathVariable("id") Long employeeId) {
        return leaveService.handleGetEmployeesLeaves(employeeId, authHeader);
    }

    @GetMapping("/leave-request/total-days/{id}")
    public ResponseEntity<ResponseDto<LeaveDaysResponseDto>> handleGetEmployeeDaysUsed(
            @RequestHeader("Authorisation") String authHeader,
            @PathVariable("id") Long employeeId) {
        return leaveService.handleGetEmployeeDaysUsed(employeeId, authHeader);
    }

}