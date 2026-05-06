package com.staffs.leaveservice;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.request.ChangeLeaveRequestStatusDto;
import com.staffs.leaveservice.dto.request.CreateLeaveRequestDto;
import com.staffs.leaveservice.dto.request.LeaveHistoryRequestDto;
import com.staffs.leaveservice.dto.response.LeaveDaysResponseDto;
import com.staffs.leaveservice.dto.response.LeaveResponseDto;
import com.staffs.leaveservice.dto.response.ResponseDto;
import com.staffs.leaveservice.entity.EmployeeEntity;
import com.staffs.leaveservice.entity.LeaveEntity;
import com.staffs.leaveservice.exception.AuthenticationException;
import com.staffs.leaveservice.exception.InvalidLeaveException;
import com.staffs.leaveservice.exception.ResourceNotFoundException;
import com.staffs.leaveservice.exception.UnauthorizedException;
import com.staffs.leaveservice.repository.EmployeeRepository;
import com.staffs.leaveservice.repository.LeaveRepository;
import com.staffs.leaveservice.service.LeaveService;
import com.staffs.leaveservice.service.SecurityService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTests {

    @InjectMocks
    private LeaveService leaveService;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private ConstantsProvider constantsProvider;

    private static final String JWT_SECRET = "3K8mP2nQ5rS8tU1vW4xY7zA0bC3dE6fG9hJ2kL5mN9pQ1rS4tU7vO0xY6zA6bC9d";

    private EmployeeEntity adminEmployee;
    private EmployeeEntity userEmployee;
    private LeaveEntity leaveEntity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(leaveService, "constantsProvider", constantsProvider);

        adminEmployee = new EmployeeEntity();
        adminEmployee.setEmployeeId(1001L);
        adminEmployee.setRole(EmployeeEntity.Role.ADMIN);
        adminEmployee.setIsDefault(false);
        adminEmployee.setLeaveBalance(BigDecimal.valueOf(20));

        userEmployee = new EmployeeEntity();
        userEmployee.setEmployeeId(1003L);
        userEmployee.setRole(EmployeeEntity.Role.USER);
        userEmployee.setIsDefault(false);
        userEmployee.setLeaveBalance(BigDecimal.valueOf(20));

        leaveEntity = new LeaveEntity();
        leaveEntity.setId(1L);
        leaveEntity.setEmployeeId(1001L);
        leaveEntity.setStartDate(LocalDate.of(2025, 6, 2));
        leaveEntity.setEndDate(LocalDate.of(2025, 6, 6));
        leaveEntity.setStatus("PENDING");
    }

    private String generateToken(Long employeeId) {
        return Jwts.builder()
                .setSubject(employeeId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();
    }

    @Test
    void handleLeaveRequest_success() {
        String token = generateToken(1001L);

        CreateLeaveRequestDto request = new CreateLeaveRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);
        request.setStartDate(LocalDate.of(2025, 6, 2));
        request.setEndDate(LocalDate.of(2025, 6, 6));

        when(securityService.isJwtValid(token, 1001L)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(employeeRepository.findByEmployeeId(1001L)).thenReturn(Optional.of(adminEmployee));
        when(leaveRepository.save(any())).thenReturn(leaveEntity);
        when(constantsProvider.getLEAVE_REQUEST_PENDING()).thenReturn("PENDING");
        when(constantsProvider.getSUCCESS_LEAVE_REQUEST()).thenReturn("Leave request created");

        ResponseEntity<ResponseDto<LeaveResponseDto>> response = leaveService.handleLeaveRequest(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(leaveRepository, atLeastOnce()).save(any(LeaveEntity.class));
    }

    @Test
    void handleLeaveRequest_invalidToken_throwsAuthenticationException() {
        CreateLeaveRequestDto request = new CreateLeaveRequestDto();
        request.setJwtToken("bad.token");
        request.setEmployeeId(1001L);
        request.setStartDate(LocalDate.of(2025, 6, 2));
        request.setEndDate(LocalDate.of(2025, 6, 6));

        when(securityService.isJwtValid("bad.token", 1001L)).thenReturn(false);
        when(constantsProvider.getERROR_JWT_FAILURE()).thenReturn("JWT invalid");

        assertThrows(AuthenticationException.class, () -> leaveService.handleLeaveRequest(request));
    }

    @Test
    void handleLeaveRequest_defaultPassword_throwsUnauthorizedException() {
        String token = generateToken(1001L);

        CreateLeaveRequestDto request = new CreateLeaveRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);
        request.setStartDate(LocalDate.of(2025, 6, 2));
        request.setEndDate(LocalDate.of(2025, 6, 6));

        when(securityService.isJwtValid(token, 1001L)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(true);
        when(constantsProvider.getERROR_DEFAULT_PASS()).thenReturn("Default password detected");

        assertThrows(UnauthorizedException.class, () -> leaveService.handleLeaveRequest(request));
    }

    @Test
    void handleLeaveRequest_endDateBeforeStartDate_throwsInvalidLeaveException() {
        String token = generateToken(1001L);

        CreateLeaveRequestDto request = new CreateLeaveRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);
        request.setStartDate(LocalDate.of(2025, 6, 6));
        request.setEndDate(LocalDate.of(2025, 6, 2));

        when(securityService.isJwtValid(token, 1001L)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(constantsProvider.getERROR_LEAVE_INVALID_DATES()).thenReturn("Invalid dates");

        assertThrows(InvalidLeaveException.class, () -> leaveService.handleLeaveRequest(request));
    }

    @Test
    void handleLeaveRequest_insufficientLeaveBalance_throwsInvalidLeaveException() {
        String token = generateToken(1001L);

        EmployeeEntity lowBalanceEmployee = new EmployeeEntity();
        lowBalanceEmployee.setEmployeeId(1001L);
        lowBalanceEmployee.setLeaveBalance(BigDecimal.valueOf(1));

        CreateLeaveRequestDto request = new CreateLeaveRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);
        request.setStartDate(LocalDate.of(2025, 6, 2));
        request.setEndDate(LocalDate.of(2025, 6, 6));

        when(securityService.isJwtValid(token, 1001L)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(employeeRepository.findByEmployeeId(1001L)).thenReturn(Optional.of(lowBalanceEmployee));
        when(constantsProvider.getERROR_LEAVE_INVALID_LEAVE_BALANCE()).thenReturn("Insufficient leave balance");

        assertThrows(InvalidLeaveException.class, () -> leaveService.handleLeaveRequest(request));
    }

    @Test
    void handleChangeLeaveRequestStatus_approve_success() {
        String token = generateToken(1001L);

        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setJwtToken(token);
        request.setId(1L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(securityService.isUser(1001L)).thenReturn(false);
        when(securityService.isAdmin(1001L)).thenReturn(true);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leaveEntity));
        when(leaveRepository.save(any())).thenReturn(leaveEntity);
        when(constantsProvider.getLEAVE_REQUEST_CANCELLED()).thenReturn("CANCELLED");
        when(constantsProvider.getSUCCESS_APPROVAL_LEAVE_REQUEST()).thenReturn("Status updated");

        ResponseEntity<ResponseDto<LeaveResponseDto>> response =
                leaveService.handleChangeLeaveRequestStatus(request, "APPROVED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(leaveRepository).save(any(LeaveEntity.class));
    }

    @Test
    void handleChangeLeaveRequestStatus_cancel_asUser_success() {
        String token = generateToken(1003L);

        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setJwtToken(token);
        request.setId(1L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1003L);
        when(securityService.isDefault(1003L)).thenReturn(false);
        when(securityService.isUser(1003L)).thenReturn(true);
        when(securityService.isAdmin(1003L)).thenReturn(false);
        when(securityService.isJwtValid(token, 1003L)).thenReturn(true);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leaveEntity));
        when(leaveRepository.save(any())).thenReturn(leaveEntity);
        when(constantsProvider.getLEAVE_REQUEST_CANCELLED()).thenReturn("CANCELLED");
        when(constantsProvider.getSUCCESS_APPROVAL_LEAVE_REQUEST()).thenReturn("Status updated");

        ResponseEntity<ResponseDto<LeaveResponseDto>> response =
                leaveService.handleChangeLeaveRequestStatus(request, "CANCELLED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void handleChangeLeaveRequestStatus_approve_asUser_throwsUnauthorizedException() {
        String token = generateToken(1003L);

        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setJwtToken(token);
        request.setId(1L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1003L);
        when(securityService.isDefault(1003L)).thenReturn(false);
        when(securityService.isUser(1003L)).thenReturn(true);
        when(constantsProvider.getLEAVE_REQUEST_CANCELLED()).thenReturn("CANCELLED");
        when(constantsProvider.getERROR_USER_FAILURE()).thenReturn("Insufficient role");

        assertThrows(UnauthorizedException.class,
                () -> leaveService.handleChangeLeaveRequestStatus(request, "APPROVED"));
    }

    @Test
    void handleChangeLeaveRequestStatus_invalidToken_throwsAuthenticationException() {
        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setJwtToken("bad.token");
        request.setId(1L);

        when(securityService.isJwtValid("bad.token")).thenReturn(false);
        when(constantsProvider.getERROR_JWT_FAILURE()).thenReturn("JWT invalid");

        assertThrows(AuthenticationException.class,
                () -> leaveService.handleChangeLeaveRequestStatus(request, "APPROVED"));
    }

    @Test
    void handleChangeLeaveRequestStatus_leaveNotFound_throwsResourceNotFoundException() {
        String token = generateToken(1001L);

        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setJwtToken(token);
        request.setId(999L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(securityService.isUser(1001L)).thenReturn(false);
        when(securityService.isAdmin(1001L)).thenReturn(true);
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());
        when(constantsProvider.getLEAVE_REQUEST_CANCELLED()).thenReturn("CANCELLED");
        when(constantsProvider.getERROR_LEAVE_REQUEST_NOT_FOUND()).thenReturn("Leave not found");

        assertThrows(ResourceNotFoundException.class,
                () -> leaveService.handleChangeLeaveRequestStatus(request, "APPROVED"));
    }

    @Test
    void handleGetEmployeesLeaves_success_asAdmin() {
        String token = generateToken(1001L);

        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1003L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(securityService.isUser(1001L)).thenReturn(false);
        when(leaveRepository.findByEmployeeId(1003L)).thenReturn(List.of(leaveEntity));
        when(constantsProvider.getSUCCESS_GET_ALL_EMPLOYEE_LEAVE()).thenReturn("Success");

        ResponseEntity<ResponseDto<List<LeaveResponseDto>>> response =
                leaveService.handleGetEmployeesLeaves(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void handleGetEmployeesLeaves_success_asUser_ownLeaves() {
        String token = generateToken(1003L);

        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1003L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1003L);
        when(securityService.isDefault(1003L)).thenReturn(false);
        when(securityService.isUser(1003L)).thenReturn(true);
        when(securityService.isJwtValid(token, 1003L)).thenReturn(true);
        when(leaveRepository.findByEmployeeId(1003L)).thenReturn(List.of());
        when(constantsProvider.getSUCCESS_GET_ALL_EMPLOYEE_LEAVE()).thenReturn("Success");

        ResponseEntity<ResponseDto<List<LeaveResponseDto>>> response =
                leaveService.handleGetEmployeesLeaves(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void handleGetEmployeesLeaves_asUser_otherEmployee_throwsUnauthorizedException() {
        String token = generateToken(1003L);

        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1003L);
        when(securityService.isDefault(1003L)).thenReturn(false);
        when(securityService.isUser(1003L)).thenReturn(true);
        when(securityService.isJwtValid(token, 1001L)).thenReturn(false);
        when(constantsProvider.getERROR_USER_FAILURE()).thenReturn("Unauthorized");

        assertThrows(UnauthorizedException.class, () -> leaveService.handleGetEmployeesLeaves(request));
    }

    @Test
    void handleGetEmployeesLeaves_invalidToken_throwsAuthenticationException() {
        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken("bad.token");
        request.setEmployeeId(1001L);

        when(securityService.isJwtValid("bad.token")).thenReturn(false);
        when(constantsProvider.getERROR_JWT_FAILURE()).thenReturn("JWT invalid");

        assertThrows(AuthenticationException.class, () -> leaveService.handleGetEmployeesLeaves(request));
    }

    @Test
    void handleGetEmployeeDaysUsed_success() {
        String token = generateToken(1001L);

        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(securityService.isUser(1001L)).thenReturn(false);
        when(leaveRepository.findByEmployeeId(1001L)).thenReturn(List.of(leaveEntity));
        when(constantsProvider.getSUCCESS_GET_TOTAL_LEAVE_DAYS()).thenReturn("Success");

        ResponseEntity<ResponseDto<LeaveDaysResponseDto>> response =
                leaveService.handleGetEmployeeDaysUsed(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getData().getDays());
    }

    @Test
    void handleGetEmployeeDaysUsed_noLeaves_returnsZeroDays() {
        String token = generateToken(1001L);

        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1001L);
        when(securityService.isDefault(1001L)).thenReturn(false);
        when(securityService.isUser(1001L)).thenReturn(false);
        when(leaveRepository.findByEmployeeId(1001L)).thenReturn(List.of());
        when(constantsProvider.getSUCCESS_GET_TOTAL_LEAVE_DAYS()).thenReturn("Success");

        ResponseEntity<ResponseDto<LeaveDaysResponseDto>> response =
                leaveService.handleGetEmployeeDaysUsed(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0L, response.getBody().getData().getDays());
    }

    @Test
    void handleGetEmployeeDaysUsed_asUser_otherEmployee_throwsUnauthorizedException() {
        String token = generateToken(1003L);

        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(1001L);

        when(securityService.isJwtValid(token)).thenReturn(true);
        when(securityService.extractId(token)).thenReturn(1003L);
        when(securityService.isDefault(1003L)).thenReturn(false);
        when(securityService.isUser(1003L)).thenReturn(true);
        when(securityService.isJwtValid(token, 1001L)).thenReturn(false);
        when(constantsProvider.getERROR_USER_FAILURE()).thenReturn("Unauthorized");

        assertThrows(UnauthorizedException.class, () -> leaveService.handleGetEmployeeDaysUsed(request));
    }

    @Test
    void handleGetEmployeeDaysUsed_invalidToken_throwsAuthenticationException() {
        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setJwtToken("bad.token");
        request.setEmployeeId(1001L);

        when(securityService.isJwtValid("bad.token")).thenReturn(false);
        when(constantsProvider.getERROR_JWT_FAILURE()).thenReturn("JWT invalid");

        assertThrows(AuthenticationException.class, () -> leaveService.handleGetEmployeeDaysUsed(request));
    }
}
