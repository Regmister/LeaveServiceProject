package com.staffs.leaveservice.entity;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.request.CreateLeaveRequestDto;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "leave_requests")
public class LeaveEntity {

    public LeaveEntity(){}

    public LeaveEntity(CreateLeaveRequestDto request, String status){
        this.setEmployeeId(request.getEmployeeId());
        this.setStartDate(request.getStartDate());
        this.setEndDate(request.getEndDate());
        this.setStatus(status);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    private String status;

}
