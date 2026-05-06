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
    public ResponseEntity<ResponseDto<LoginResponseDto>> handleCreateUser(@Valid @RequestBody CreateUserRequestDto request){
        return securityService.handleCreateUser(request);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ResponseDto<ChangePasswordResponseDto>> handleChangePassword(@Valid @RequestBody ChangePasswordRequestDto request){
        return securityService.handleChangePassword(request);
    }

    @PostMapping("/leave-request")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveRequest(@Valid @RequestBody CreateLeaveRequestDto request){
        return leaveService.handleLeaveRequest(request);
    }

    @DeleteMapping("/leave-request")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveRequestDeletion(@Valid @RequestBody ChangeLeaveRequestStatusDto request){
        return leaveService.handleChangeLeaveRequestStatus(request, constantsProvider.getLEAVE_REQUEST_CANCELLED());
    }

    @PatchMapping("/leave-request/approve")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveApprove(@Valid @RequestBody ChangeLeaveRequestStatusDto request) {
        return leaveService.handleChangeLeaveRequestStatus(request, constantsProvider.getLEAVE_REQUEST_APPROVED());
    }

    @PatchMapping("/leave-request/decline")
    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveDecline(@Valid @RequestBody ChangeLeaveRequestStatusDto request) {
        return leaveService.handleChangeLeaveRequestStatus(request, constantsProvider.getLEAVE_REQUEST_DECLINED());
    }

    @GetMapping("/leave-request/status")
    public ResponseEntity<ResponseDto<List<LeaveResponseDto>>> handleGetLeaveStatus(@Valid @RequestBody LeaveHistoryRequestDto request) {
        return leaveService.handleGetEmployeesLeaves(request);
    }

    @GetMapping("/leave-request/total-days")
    public ResponseEntity<ResponseDto<LeaveDaysResponseDto>> handleGetEmployeeDaysUsed(@Valid @RequestBody LeaveHistoryRequestDto request) {
        return leaveService.handleGetEmployeeDaysUsed(request);
    }

}