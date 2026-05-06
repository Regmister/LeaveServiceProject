package com.staffs.leaveservice.repository;

import java.util.List;
import com.staffs.leaveservice.entity.LeaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveEntity, Long> {
    List<LeaveEntity> findByEmployeeId(Long employeeId);
}
