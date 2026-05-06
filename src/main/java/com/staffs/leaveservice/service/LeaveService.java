package com.staffs.leaveservice.service;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.request.ChangeLeaveRequestStatusDto;
import com.staffs.leaveservice.dto.request.CreateLeaveRequestDto;
import com.staffs.leaveservice.dto.request.LeaveHistoryRequestDto;
import com.staffs.leaveservice.dto.response.LeaveDaysResponseDto;
import com.staffs.leaveservice.dto.response.LeaveResponseDto;
import com.staffs.leaveservice.dto.response.ResponseDto;
import com.staffs.leaveservice.entity.LeaveEntity;
import com.staffs.leaveservice.exception.AuthenticationException;
import com.staffs.leaveservice.exception.ResourceNotFoundException;
import com.staffs.leaveservice.exception.UnauthorizedException;
import com.staffs.leaveservice.repository.LeaveRepository;
import com.staffs.leaveservice.service.helper.LeaveServiceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;

@Slf4j
@Service
public class LeaveService extends LeaveServiceHelper {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ConstantsProvider constantsProvider;

    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleLeaveRequest(CreateLeaveRequestDto request){
        log.debug("Entering handleLeaveRequest: " + request);

        if(!securityService.isJwtValid(request.getJwtToken(), request.getEmployeeId())){
            log.error(constantsProvider.getERROR_JWT_FAILURE());
            throw new AuthenticationException(constantsProvider.getERROR_JWT_FAILURE());
        }

        if (securityService.isDefault(securityService.extractId(request.getJwtToken()))){
            throw new UnauthorizedException(constantsProvider.getERROR_DEFAULT_PASS());
        }

        deductLeaveBalance(request.getEmployeeId(), validateDates(request.getStartDate(), request.getEndDate(), request.getEmployeeId()));
        long workingDays = countWorkingDays(request.getStartDate(), request.getEndDate());

        LeaveEntity leaveEntity = new LeaveEntity(request, constantsProvider.getLEAVE_REQUEST_PENDING());
        leaveRepository.save(leaveEntity);

        deductLeaveBalance(request.getEmployeeId(), workingDays);

        LeaveResponseDto leaveResponseDto = new LeaveResponseDto(leaveEntity);
        leaveResponseDto.setId(leaveEntity.getId());
        ResponseDto<LeaveResponseDto> responseDto = new ResponseDto<>(constantsProvider.getSUCCESS_LEAVE_REQUEST(), leaveResponseDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDto<LeaveResponseDto>> handleChangeLeaveRequestStatus(ChangeLeaveRequestStatusDto request, String status){
        log.debug("Entering handleChangeLeaveRequestStatus: " + request);

        if (!securityService.isJwtValid(request.getJwtToken())){
            throw new AuthenticationException(constantsProvider.getERROR_JWT_FAILURE());
        }

        if (securityService.isDefault(securityService.extractId(request.getJwtToken()))){
            throw new UnauthorizedException(constantsProvider.getERROR_DEFAULT_PASS());
        }

        Long userId = securityService.extractId(request.getJwtToken());
        boolean isUser = securityService.isUser(userId);
        boolean isAdmin = securityService.isAdmin(userId);
        boolean isCancellation = status.equals(constantsProvider.getLEAVE_REQUEST_CANCELLED());

        if (isCancellation) {
            if (isUser && !securityService.isJwtValid(request.getJwtToken(), userId)) {
                log.error(constantsProvider.getERROR_USER_FAILURE());
                throw new UnauthorizedException(constantsProvider.getERROR_USER_FAILURE());
            } else if (!isUser && !isAdmin) {
                log.error(constantsProvider.getERROR_USER_FAILURE());
                throw new UnauthorizedException(constantsProvider.getERROR_USER_FAILURE());
            }
        } else {
            if (isUser) {
                log.error(constantsProvider.getERROR_USER_FAILURE());
                throw new UnauthorizedException(constantsProvider.getERROR_USER_FAILURE());
            }
        }

        LeaveEntity leaveEntity = leaveRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException(constantsProvider.getERROR_LEAVE_REQUEST_NOT_FOUND()));

        leaveEntity.setStatus(status);
        leaveRepository.save(leaveEntity);

        if (status.equals(constantsProvider.getLEAVE_REQUEST_DECLINED()) || status.equals(constantsProvider.getLEAVE_REQUEST_CANCELLED())){
            addLeaveBalance(request.getId(), validateDates(leaveEntity.getStartDate(), leaveEntity.getEndDate(), request.getId()));
        }

        LeaveResponseDto leaveResponseDto = new LeaveResponseDto(leaveEntity);
        leaveResponseDto.setId(leaveEntity.getId());
        ResponseDto<LeaveResponseDto> responseDto = new ResponseDto<>(constantsProvider.getSUCCESS_APPROVAL_LEAVE_REQUEST(), leaveResponseDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDto<List<LeaveResponseDto>>> handleGetEmployeesLeaves(LeaveHistoryRequestDto request){
        log.debug("Entering handleGetEmployeesLeaves: " + request);

        if (!securityService.isJwtValid(request.getJwtToken())){
            throw new AuthenticationException(constantsProvider.getERROR_JWT_FAILURE());
        }

        if (securityService.isDefault(securityService.extractId(request.getJwtToken()))){
            throw new UnauthorizedException(constantsProvider.getERROR_DEFAULT_PASS());
        }

        if (securityService.isUser(securityService.extractId(request.getJwtToken()))){
            if (!securityService.isJwtValid(request.getJwtToken(), request.getEmployeeId())){
                throw new UnauthorizedException(constantsProvider.getERROR_USER_FAILURE());
            }
        }

        List<LeaveEntity> leaveEntityList = leaveRepository.findByEmployeeId(request.getEmployeeId());

        List<LeaveResponseDto> leaveResponsesListDto = new ArrayList<>();

        for (LeaveEntity leaveEntity : leaveEntityList) {
            leaveResponsesListDto.add(new LeaveResponseDto(leaveEntity));
        }

        ResponseDto<List<LeaveResponseDto>> responseDto = new ResponseDto<>(constantsProvider.getSUCCESS_GET_ALL_EMPLOYEE_LEAVE(), leaveResponsesListDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDto<LeaveDaysResponseDto>> handleGetEmployeeDaysUsed(LeaveHistoryRequestDto request){
        log.debug("Entering handleGetEmployeeDaysUsed: " + request);

        if (!securityService.isJwtValid(request.getJwtToken())){
            throw new AuthenticationException(constantsProvider.getERROR_JWT_FAILURE());
        }

        if (securityService.isDefault(securityService.extractId(request.getJwtToken()))){
            throw new UnauthorizedException(constantsProvider.getERROR_DEFAULT_PASS());
        }

        if (securityService.isUser(securityService.extractId(request.getJwtToken()))){
            if (!securityService.isJwtValid(request.getJwtToken(), request.getEmployeeId())){
                throw new UnauthorizedException(constantsProvider.getERROR_USER_FAILURE());
            }
        }

        List<LeaveEntity> leaveEntityList = leaveRepository.findByEmployeeId(request.getEmployeeId());

        long totalWorkingDays = 0;

        for (LeaveEntity leaveEntity : leaveEntityList) {
            totalWorkingDays += leaveEntity.getStartDate().datesUntil(leaveEntity.getEndDate().plusDays(1))
                    .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                    .count();
        }

        LeaveDaysResponseDto leaveDaysResponseDto = new LeaveDaysResponseDto(totalWorkingDays);
        ResponseDto<LeaveDaysResponseDto> responseDto = new ResponseDto<>(constantsProvider.getSUCCESS_GET_TOTAL_LEAVE_DAYS(), leaveDaysResponseDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
