package com.staffs.leaveservice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreateUserRequestDto {

    @NotNull
    @JsonProperty("jwt_token")
    private String jwtToken;

    @NotNull
    @JsonProperty("employee_id")
    private Long employeeId;

    @NotNull
    @JsonProperty("first_name")
    private String firstName;

    @NotNull
    @JsonProperty("last_name")
    private String lastName;

    @NotNull
    @JsonProperty("role")
    private String role;

    @NotNull
    @JsonProperty("leave_refresh_date")
    private LocalDate leaveRefreshDate;

    @NotNull
    @JsonProperty("leave_refresh_amount")
    private BigDecimal leaveRefreshAmount;
    
}
