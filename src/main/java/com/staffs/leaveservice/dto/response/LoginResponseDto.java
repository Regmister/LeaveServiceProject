package com.staffs.leaveservice.dto.response;

import com.staffs.leaveservice.dto.request.LoginRequestDto;
import com.staffs.leaveservice.service.SecurityService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class LoginResponseDto {

    public LoginResponseDto(LoginRequestDto loginRequestDto, SecurityService securityService){
        this.setJwt(securityService.generateJwt(loginRequestDto.getEmployeeId()));
    }

    private String jwt;

}
