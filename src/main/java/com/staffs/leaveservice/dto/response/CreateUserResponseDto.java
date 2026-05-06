package com.staffs.leaveservice.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateUserResponseDto {

    public enum Role {
        ADMIN,
        MANAGER,
        USER
    }

    private Long id;
    private Long employeeId;
    private String firstName;
    private String lastName;
    private Role role;
    private BigDecimal leaveBalance;
    private BigDecimal leaveRefreshDate;
    private BigDecimal leaveRefreshAmount;
    
}
