package com.staffs.leaveservice.service.helper;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.entity.EmployeeEntity;
import com.staffs.leaveservice.exception.InvalidLeaveException;
import com.staffs.leaveservice.exception.ResourceNotFoundException;
import com.staffs.leaveservice.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class LeaveServiceHelper {

    @Autowired
    private ConstantsProvider constantsProvider;

    @Autowired
    private EmployeeRepository employeeRepository;

    protected ConstantsProvider getConstantsProvider() { return constantsProvider; }

    public long countWorkingDays(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    public long validateDates(LocalDate startDate, LocalDate endDate, Long employeeId) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidLeaveException(constantsProvider.getERROR_LEAVE_INVALID_DATES());
        }

        long workingDays = countWorkingDays(startDate, endDate);

        EmployeeEntity employeeEntity = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(constantsProvider.getERROR_USER_FAILURE()));

        if (employeeEntity.getLeaveBalance().compareTo(BigDecimal.valueOf(workingDays)) < 0) {
            throw new InvalidLeaveException(constantsProvider.getERROR_LEAVE_INVALID_LEAVE_BALANCE());
        }

        return workingDays;
    }

    public void deductLeaveBalance(Long employeeId, long workingDays) {
        EmployeeEntity employeeEntity = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(constantsProvider.getERROR_USER_FAILURE()));

        employeeEntity.setLeaveBalance(employeeEntity.getLeaveBalance().subtract(BigDecimal.valueOf(workingDays)));
        employeeRepository.save(employeeEntity);
    }

    public void addLeaveBalance(Long employeeId, long workingDays) {
        EmployeeEntity employeeEntity = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(constantsProvider.getERROR_USER_FAILURE()));

        employeeEntity.setLeaveBalance(employeeEntity.getLeaveBalance().add(BigDecimal.valueOf(workingDays)));
        employeeRepository.save(employeeEntity);
    }
}
