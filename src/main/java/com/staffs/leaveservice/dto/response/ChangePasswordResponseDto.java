package com.staffs.leaveservice.dto.response;

import com.staffs.leaveservice.dto.request.ChangePasswordRequestDto;
import com.staffs.leaveservice.entity.EmployeeEntity;
import lombok.Data;

@Data
public class ChangePasswordResponseDto {

    public ChangePasswordResponseDto(EmployeeEntity employeeEntity){
        this.setEmployeeId(employeeEntity.getEmployeeId());
    }

    private Long employeeId;

}
