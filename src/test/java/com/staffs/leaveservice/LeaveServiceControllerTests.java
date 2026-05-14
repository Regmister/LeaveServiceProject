package com.staffs.leaveservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.staffs.leaveservice.controller.LeaveServiceController;
import com.staffs.leaveservice.dto.request.*;
import com.staffs.leaveservice.dto.response.*;
import com.staffs.leaveservice.exception.AuthenticationException;
import com.staffs.leaveservice.exception.ResourceNotFoundException;
import com.staffs.leaveservice.exception.UnauthorizedException;
import com.staffs.leaveservice.service.LeaveService;
import com.staffs.leaveservice.service.SecurityService;
import com.staffs.leaveservice.constants.ConstantsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveServiceController.class)
class LeaveServiceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private ConstantsProvider constantsProvider;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        when(constantsProvider.getLEAVE_REQUEST_CANCELLED()).thenReturn("CANCELLED");
        when(constantsProvider.getLEAVE_REQUEST_APPROVED()).thenReturn("APPROVED");
        when(constantsProvider.getLEAVE_REQUEST_DECLINED()).thenReturn("DECLINED");
    }

    @Test
    void login_success() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmployeeId(1001L);
        request.setHash("someHash");

        ResponseDto<LoginResponseDto> response = new ResponseDto<>("Login successful", null);

        when(securityService.handleLogin(any())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/leave-service/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmployeeId(1001L);
        request.setHash("wrongHash");

        when(securityService.handleLogin(any())).thenThrow(new AuthenticationException("Invalid credentials"));

        mockMvc.perform(post("/leave-service/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/leave-service/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_success() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setEmployeeId(2001L);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setLeaveRefreshDate(LocalDate.of(2025, 1, 1));
        request.setLeaveRefreshAmount(BigDecimal.valueOf(20));

        ResponseDto<LoginResponseDto> response = new ResponseDto<>("User created", null);

        when(securityService.handleCreateUser(any(CreateUserRequestDto.class), anyString())).thenReturn(new ResponseEntity<>(response, HttpStatus.CREATED));

        mockMvc.perform(post("/leave-service/create-user")
                        .header("Authorisation", "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_notAdmin_returns401() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setEmployeeId(2001L);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setLeaveRefreshDate(LocalDate.of(2025, 1, 1));
        request.setLeaveRefreshAmount(BigDecimal.valueOf(20));

        when(securityService.handleCreateUser(any(CreateUserRequestDto.class), anyString())).thenThrow(new UnauthorizedException("Insufficient role"));

        mockMvc.perform(post("/leave-service/create-user")
                        .header("Authorisation", "Bearer userToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_success() throws Exception {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setHash("newHash");

        ResponseDto<ChangePasswordResponseDto> response = new ResponseDto<>("Password changed", null);

        when(securityService.handleChangePassword(any(ChangePasswordRequestDto.class), anyString())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(patch("/leave-service/change-password")
                        .header("Authorisation", "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_invalidToken_returns401() throws Exception {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setHash("newHash");

        when(securityService.handleChangePassword(any(ChangePasswordRequestDto.class), anyString())).thenThrow(new AuthenticationException("Invalid token"));

        mockMvc.perform(patch("/leave-service/change-password")
                        .header("Authorisation", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createLeaveRequest_success() throws Exception {
        CreateLeaveRequestDto request = new CreateLeaveRequestDto();
        request.setEmployeeId(1001L);
        request.setStartDate(LocalDate.of(2025, 6, 2));
        request.setEndDate(LocalDate.of(2025, 6, 6));

        ResponseDto<LeaveResponseDto> response = new ResponseDto<>("Leave request created", null);

        when(leaveService.handleLeaveRequest(any(CreateLeaveRequestDto.class), anyString())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/leave-service/leave-request")
                        .header("Authorisation", "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createLeaveRequest_invalidToken_returns401() throws Exception {
        CreateLeaveRequestDto request = new CreateLeaveRequestDto();
        request.setEmployeeId(1001L);
        request.setStartDate(LocalDate.of(2025, 6, 2));
        request.setEndDate(LocalDate.of(2025, 6, 6));

        when(leaveService.handleLeaveRequest(any(CreateLeaveRequestDto.class), anyString())).thenThrow(new AuthenticationException("Invalid token"));

        mockMvc.perform(post("/leave-service/leave-request")
                        .header("Authorisation", "Bearer badToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelLeaveRequest_success() throws Exception {
        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setId(1L);

        ResponseDto<LeaveResponseDto> response = new ResponseDto<>("Leave cancelled", null);

        when(leaveService.handleChangeLeaveRequestStatus(any(ChangeLeaveRequestStatusDto.class), eq("CANCELLED"), anyString()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(delete("/leave-service/leave-request")
                        .header("Authorisation", "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelLeaveRequest_notFound_returns400() throws Exception {
        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setId(999L);

        when(leaveService.handleChangeLeaveRequestStatus(any(ChangeLeaveRequestStatusDto.class), eq("CANCELLED"), anyString()))
                .thenThrow(new ResourceNotFoundException("Leave request not found"));

        mockMvc.perform(delete("/leave-service/leave-request")
                        .header("Authorisation", "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveLeaveRequest_success() throws Exception {
        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setId(1L);

        ResponseDto<LeaveResponseDto> response = new ResponseDto<>("Leave approved", null);

        when(leaveService.handleChangeLeaveRequestStatus(any(ChangeLeaveRequestStatusDto.class), eq("APPROVED"), anyString()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(patch("/leave-service/leave-request/approve")
                        .header("Authorisation", "Bearer managerToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void approveLeaveRequest_userRole_returns401() throws Exception {
        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setId(1L);

        when(leaveService.handleChangeLeaveRequestStatus(any(ChangeLeaveRequestStatusDto.class), eq("APPROVED"), anyString()))
                .thenThrow(new UnauthorizedException("Insufficient role"));

        mockMvc.perform(patch("/leave-service/leave-request/approve")
                        .header("Authorisation", "Bearer userToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void declineLeaveRequest_success() throws Exception {
        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setId(1L);

        ResponseDto<LeaveResponseDto> response = new ResponseDto<>("Leave declined", null);

        when(leaveService.handleChangeLeaveRequestStatus(any(ChangeLeaveRequestStatusDto.class), eq("DECLINED"), anyString()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(patch("/leave-service/leave-request/decline")
                        .header("Authorisation", "Bearer managerToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void declineLeaveRequest_userRole_returns401() throws Exception {
        ChangeLeaveRequestStatusDto request = new ChangeLeaveRequestStatusDto();
        request.setId(1L);

        when(leaveService.handleChangeLeaveRequestStatus(any(ChangeLeaveRequestStatusDto.class), eq("DECLINED"), anyString()))
                .thenThrow(new UnauthorizedException("Insufficient role"));

        mockMvc.perform(patch("/leave-service/leave-request/decline")
                        .header("Authorisation", "Bearer userToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLeaveStatus_success() throws Exception {
        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setEmployeeId(1001L);

        ResponseDto<List<LeaveResponseDto>> response = new ResponseDto<>("Success", List.of());

        when(leaveService.handleGetEmployeesLeaves(any(LeaveHistoryRequestDto.class), anyString())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(get("/leave-service/leave-request/status")
                        .header("Authorisation", "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getLeaveStatus_invalidToken_returns401() throws Exception {
        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setEmployeeId(1001L);

        when(leaveService.handleGetEmployeesLeaves(any(LeaveHistoryRequestDto.class), anyString())).thenThrow(new AuthenticationException("Invalid token"));

        mockMvc.perform(get("/leave-service/leave-request/status")
                        .header("Authorisation", "Bearer badToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTotalDays_success() throws Exception {
        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setEmployeeId(1001L);

        LeaveDaysResponseDto leaveDaysResponse = new LeaveDaysResponseDto(5L);
        ResponseDto<LeaveDaysResponseDto> response = new ResponseDto<>("Success", leaveDaysResponse);

        when(leaveService.handleGetEmployeeDaysUsed(any(LeaveHistoryRequestDto.class), anyString())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(get("/leave-service/leave-request/total-days")
                        .header("Authorisation", "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getTotalDays_invalidToken_returns401() throws Exception {
        LeaveHistoryRequestDto request = new LeaveHistoryRequestDto();
        request.setEmployeeId(1001L);

        when(leaveService.handleGetEmployeeDaysUsed(any(LeaveHistoryRequestDto.class), anyString())).thenThrow(new AuthenticationException("Invalid token"));

        mockMvc.perform(get("/leave-service/leave-request/total-days")
                        .header("Authorisation", "Bearer badToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
