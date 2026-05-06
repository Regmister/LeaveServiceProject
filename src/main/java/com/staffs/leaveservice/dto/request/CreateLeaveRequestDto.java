package com.staffs.leaveservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class CreateLeaveRequestDto {

    @NotNull
    @JsonProperty("jwt_token")
    private String jwtToken;

    @NotNull
    @JsonProperty("employee_id")
    private Long employeeId;

    @NotNull
    @JsonProperty("start_date")
    private LocalDate startDate;

    @NotNull
    @JsonProperty("end_date")
    private LocalDate endDate;

}
