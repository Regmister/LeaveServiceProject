package com.staffs.leaveservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginRequestDto {

    @NotNull
    @JsonProperty("employee_id")
    private Long employeeId;

    @NotNull
    @JsonProperty("hash")
    private String hash;

}
