package com.staffs.leaveservice.controller;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.request.*;
import com.staffs.leaveservice.dto.response.*;
import com.staffs.leaveservice.service.LeaveService;
import com.staffs.leaveservice.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponseDto>> handleLogin(@Valid @RequestBody LoginRequestDto request){
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

    @GetMapping("/leave-request/status")
    public ResponseEntity<ResponseDto<List<LeaveResponseDto>>> handleGetLeaveStatus(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody LeaveHistoryRequestDto request) {
        return leaveService.handleGetEmployeesLeaves(request, authHeader);
    }

    @GetMapping("/leave-request/total-days")
    public ResponseEntity<ResponseDto<LeaveDaysResponseDto>> handleGetEmployeeDaysUsed(
            @RequestHeader("Authorisation") String authHeader,
            @Valid @RequestBody LeaveHistoryRequestDto request) {
        return leaveService.handleGetEmployeeDaysUsed(request, authHeader);
    }

}