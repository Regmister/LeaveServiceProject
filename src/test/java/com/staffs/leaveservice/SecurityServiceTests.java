package com.staffs.leaveservice;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.request.ChangePasswordRequestDto;
import com.staffs.leaveservice.dto.request.CreateUserRequestDto;
import com.staffs.leaveservice.dto.request.LoginRequestDto;
import com.staffs.leaveservice.entity.EmployeeEntity;
import com.staffs.leaveservice.exception.AuthenticationException;
import com.staffs.leaveservice.exception.UnauthorizedException;
import com.staffs.leaveservice.repository.EmployeeRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTests {

    @InjectMocks
    private SecurityService securityService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ConstantsProvider constantsProvider;

    private static final String JWT_SECRET = "3K8mP2nQ5rS8tU1vW4xY7zA0bC3dE6fG9hJ2kL5mN9pQ1rS4tU7vO0xY6zA6bC9d";
    private static final String HMAC_KEY = "0+lXy9v7dMsE5dfqW1Edab0v5ViEfWtzTBsEuQw3aN8=";
    private static final String DEFAULT_PASSWORD = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";

    private EmployeeEntity adminEmployee;
    private EmployeeEntity userEmployee;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(securityService, "SECRET", JWT_SECRET);
        ReflectionTestUtils.setField(securityService, "Hmac", HMAC_KEY);
        ReflectionTestUtils.setField(securityService, "DEFAULT_PASSWORD", DEFAULT_PASSWORD);

        adminEmployee = new EmployeeEntity();
        adminEmployee.setEmployeeId(1001L);
        adminEmployee.setRole(EmployeeEntity.Role.ADMIN);
        adminEmployee.setIsDefault(false);
        adminEmployee.setSalt("testsalt123");
        adminEmployee.setHash(adminEmployee.generateHash("password", HMAC_KEY));

        userEmployee = new EmployeeEntity();
        userEmployee.setEmployeeId(1003L);
        userEmployee.setRole(EmployeeEntity.Role.USER);
        userEmployee.setIsDefault(false);
        userEmployee.setSalt("usersalt456");
        userEmployee.setHash(userEmployee.generateHash("password", HMAC_KEY));
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
    void handleLogin_success() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmployeeId(1001L);
        request.setHash("password");

        when(employeeRepository.findByEmployeeId(1001L)).thenReturn(Optional.of(adminEmployee));
        when(constantsProvider.getSUCCESS_LOGIN()).thenReturn("Login successful");

        ResponseEntity<?> response = securityService.handleLogin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void handleLogin_userNotFound_throwsAuthenticationException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmployeeId(9999L);
        request.setHash("password");

        when(employeeRepository.findByEmployeeId(9999L)).thenReturn(Optional.empty());
        when(constantsProvider.getERROR_LOGIN_FAILURE()).thenReturn("Login failed");

        assertThrows(AuthenticationException.class, () -> securityService.handleLogin(request));
    }

    @Test
    void handleLogin_wrongPassword_throwsAuthenticationException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmployeeId(1001L);
        request.setHash("wrongpassword");

        when(employeeRepository.findByEmployeeId(1001L)).thenReturn(Optional.of(adminEmployee));
        when(constantsProvider.getERROR_LOGIN_FAILURE()).thenReturn("Login failed");

        assertThrows(AuthenticationException.class, () -> securityService.handleLogin(request));
    }

    @Test
    void handleLogin_defaultPassword_throwsUnauthorizedException() {
        EmployeeEntity defaultEmployee = new EmployeeEntity();
        defaultEmployee.setEmployeeId(1002L);
        defaultEmployee.setIsDefault(true);
        defaultEmployee.setSalt("salt");
        defaultEmployee.setHash(defaultEmployee.generateHash("password", HMAC_KEY));

        LoginRequestDto request = new LoginRequestDto();
        request.setEmployeeId(1002L);
        request.setHash("password");

        when(employeeRepository.findByEmployeeId(1002L)).thenReturn(Optional.of(defaultEmployee));
        when(constantsProvider.getERROR_DEFAULT_PASS()).thenReturn("Default password detected");

        assertThrows(UnauthorizedException.class, () -> securityService.handleLogin(request));
    }

    @Test
    void handleChangePassword_success() {
        String token = generateToken(1001L);

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setJwtToken(token);
        request.setHash("newpassword");

        when(employeeRepository.findByEmployeeId(1001L)).thenReturn(Optional.of(adminEmployee));
        when(employeeRepository.save(any())).thenReturn(adminEmployee);
        when(constantsProvider.getSUCCESS_CHANGED_PASSWORD()).thenReturn("Password changed");

        ResponseEntity<?> response = securityService.handleChangePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(employeeRepository).save(adminEmployee);
    }

    @Test
    void handleChangePassword_invalidToken_throwsAuthenticationException() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setJwtToken("invalid.token.here");
        request.setHash("newpassword");

        when(constantsProvider.getERROR_JWT_FAILURE()).thenReturn("JWT invalid");

        assertThrows(AuthenticationException.class, () -> securityService.handleChangePassword(request));
    }

    @Test
    void handleChangePassword_expiredToken_throwsAuthenticationException() {
        String expiredToken = Jwts.builder()
                .setSubject("1001")
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setJwtToken(expiredToken);
        request.setHash("newpassword");

        when(constantsProvider.getERROR_JWT_FAILURE()).thenReturn("JWT invalid");

        assertThrows(AuthenticationException.class, () -> securityService.handleChangePassword(request));
    }

    @Test
    void handleCreateUser_success() {
        String token = generateToken(1001L);

        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(2001L);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setLeaveRefreshDate(LocalDate.of(2025, 1, 1));
        request.setLeaveRefreshAmount(BigDecimal.valueOf(20));

        when(employeeRepository.findByEmployeeId(1001L)).thenReturn(Optional.of(adminEmployee));
        when(employeeRepository.save(any())).thenReturn(new EmployeeEntity());
        when(constantsProvider.getSUCCESS_CREATED_USER()).thenReturn("User created");

        ResponseEntity<?> response = securityService.handleCreateUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(employeeRepository).save(any(EmployeeEntity.class));
    }

    @Test
    void handleCreateUser_invalidToken_throwsAuthenticationException() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setJwtToken("bad.token");
        request.setEmployeeId(2001L);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setLeaveRefreshDate(LocalDate.of(2025, 1, 1));
        request.setLeaveRefreshAmount(BigDecimal.valueOf(20));

        when(constantsProvider.getERROR_JWT_FAILURE()).thenReturn("JWT invalid");

        assertThrows(AuthenticationException.class, () -> securityService.handleCreateUser(request));
    }

    @Test
    void handleCreateUser_defaultPassword_throwsUnauthorizedException() {
        String token = generateToken(1002L);

        EmployeeEntity defaultAdmin = new EmployeeEntity();
        defaultAdmin.setEmployeeId(1002L);
        defaultAdmin.setRole(EmployeeEntity.Role.ADMIN);
        defaultAdmin.setIsDefault(true);

        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(2001L);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setLeaveRefreshDate(LocalDate.of(2025, 1, 1));
        request.setLeaveRefreshAmount(BigDecimal.valueOf(20));

        when(employeeRepository.findByEmployeeId(1002L)).thenReturn(Optional.of(defaultAdmin));
        when(constantsProvider.getERROR_DEFAULT_PASS()).thenReturn("Default password detected");

        assertThrows(UnauthorizedException.class, () -> securityService.handleCreateUser(request));
    }

    @Test
    void handleCreateUser_notAdmin_throwsUnauthorizedException() {
        String token = generateToken(1003L);

        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setJwtToken(token);
        request.setEmployeeId(2001L);
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setLeaveRefreshDate(LocalDate.of(2025, 1, 1));
        request.setLeaveRefreshAmount(BigDecimal.valueOf(20));

        when(employeeRepository.findByEmployeeId(1003L)).thenReturn(Optional.of(userEmployee));
        when(constantsProvider.getERROR_ROLE_FAILURE()).thenReturn("Insufficient role");

        assertThrows(UnauthorizedException.class, () -> securityService.handleCreateUser(request));
    }
}
