package com.staffs.leaveservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LeaveHistoryRequestDto {

    @NotNull
    @JsonProperty("jwt_token")
    private String jwtToken;

    @NotNull
    @JsonProperty("employee_id")
    private Long employeeId;

}
