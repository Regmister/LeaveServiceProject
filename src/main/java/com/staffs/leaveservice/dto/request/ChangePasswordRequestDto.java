package com.staffs.leaveservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangePasswordRequestDto {

    @NotNull
    @JsonProperty("hash")
    private String hash;

}
