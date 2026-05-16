package com.staffs.leaveservice.service;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.request.ChangePasswordRequestDto;
import com.staffs.leaveservice.dto.request.CreateUserRequestDto;
import com.staffs.leaveservice.dto.request.LoginRequestDto;
import com.staffs.leaveservice.dto.response.ChangePasswordResponseDto;
import com.staffs.leaveservice.dto.response.LoginResponseDto;
import com.staffs.leaveservice.dto.response.ResponseDto;
import com.staffs.leaveservice.entity.EmployeeEntity;
import com.staffs.leaveservice.exception.AuthenticationException;
import com.staffs.leaveservice.exception.ResourceNotFoundException;
import com.staffs.leaveservice.exception.UnauthorizedException;
import com.staffs.leaveservice.repository.EmployeeRepository;
import com.staffs.leaveservice.service.helper.SecurityServiceHelper;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SecurityService extends SecurityServiceHelper {

    @Value("${Hmac-Key}")
    private String Hmac;

    @Value("${Default-Password}")
    private String DEFAULT_PASSWORD;

    public ResponseEntity<ResponseDto<LoginResponseDto>> handleLogin(LoginRequestDto request) {
        Optional<EmployeeEntity> employeeEntity = employeeRepository.findByEmployeeId(request.getEmployeeId());

        if (employeeEntity.isEmpty() || !employeeEntity.get().doHashesMatch(request.getHash(), Hmac)){
            log.error(constantsProvider.getERROR_LOGIN_FAILURE());
            throw new AuthenticationException(constantsProvider.getERROR_LOGIN_FAILURE());
        }

        LoginResponseDto loginResponseDto = new LoginResponseDto(request, this);
        ResponseDto<LoginResponseDto> responseDto = new ResponseDto<>(constantsProvider.getSUCCESS_LOGIN(), loginResponseDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDto<ChangePasswordResponseDto>> handleChangePassword(ChangePasswordRequestDto request, String jwt) {

        if (!isJwtValid(jwt)){
            throw new AuthenticationException(constantsProvider.getERROR_JWT_FAILURE());
        }

        EmployeeEntity employeeEntity = employeeRepository.findByEmployeeId(extractId(jwt))
                .orElseThrow(() -> new ResourceNotFoundException(constantsProvider.getERROR_USER_FAILURE()));

        employeeEntity.setHash(employeeEntity.generateHash(request.getHash(), Hmac));

        employeeRepository.save(employeeEntity);

        ChangePasswordResponseDto changePasswordResponseDto = new ChangePasswordResponseDto(employeeEntity);
        ResponseDto<ChangePasswordResponseDto> responseDto = new ResponseDto<>(constantsProvider.getSUCCESS_CHANGED_PASSWORD(), changePasswordResponseDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);

    }

    public ResponseEntity<ResponseDto<LoginResponseDto>> handleCreateUser(CreateUserRequestDto request, String jwt) {

        if (!isJwtValid(jwt)){
            throw new AuthenticationException(constantsProvider.getERROR_JWT_FAILURE());
        }

        if (isDefault(extractId(jwt))){
            throw new UnauthorizedException(constantsProvider.getERROR_DEFAULT_PASS());
        }

        if (!isAdmin(extractId(jwt))){
            throw new UnauthorizedException(constantsProvider.getERROR_ROLE_FAILURE());
        }

        String salt = UUID.randomUUID().toString().replace("-", "");

        EmployeeEntity employee = new EmployeeEntity(request, salt, DEFAULT_PASSWORD, Hmac);

        employeeRepository.save(employee);

        ResponseDto<LoginResponseDto> responseDto = new ResponseDto<>(constantsProvider.getSUCCESS_CREATED_USER(), null);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}
