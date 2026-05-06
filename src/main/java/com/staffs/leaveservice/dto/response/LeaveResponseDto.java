package com.staffs.leaveservice.dto.response;

import com.staffs.leaveservice.entity.LeaveEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveResponseDto {

    public LeaveResponseDto(LeaveEntity leaveEntity){
        this.setId(leaveEntity.getId());
        this.setEmployeeId(leaveEntity.getEmployeeId());
        this.setStartDate(leaveEntity.getStartDate());
        this.setEndDate(leaveEntity.getEndDate());
        this.setStatus(leaveEntity.getStatus());
    }

    private Long id;
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}