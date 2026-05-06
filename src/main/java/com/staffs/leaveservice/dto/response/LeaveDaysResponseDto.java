package com.staffs.leaveservice.dto.response;

import lombok.Data;

@Data
public class LeaveDaysResponseDto {

    public LeaveDaysResponseDto(long _days){
        this.setDays(_days);
    }

    private long days;

}
