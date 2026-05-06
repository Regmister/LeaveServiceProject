package com.staffs.leaveservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

import com.staffs.leaveservice.dto.request.CreateUserRequestDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_info")
public class EmployeeEntity {

    public EmployeeEntity(CreateUserRequestDto request, String salt, String defaultPassword, String Hmac) {
        this.employeeId = request.getEmployeeId();
        this.firstName = request.getFirstName();
        this.lastName = request.getLastName();
        this.salt = salt;
        this.hash = generateHash(defaultPassword, Hmac);
        this.role = Role.valueOf(request.getRole());
        this.leaveBalance = request.getLeaveRefreshAmount();
        this.leaveRefreshDate = request.getLeaveRefreshDate();
        this.leaveRefreshAmount = request.getLeaveRefreshAmount();
        this.isDefault = true;
    }

    public enum Role {
        ADMIN,
        MANAGER,
        USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password_hash")
    private String hash;

    @Column(name = "salting_value")
    private String salt;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "leave_balance")
    private BigDecimal leaveBalance;

    @Column(name = "leave_refresh_date")
    private LocalDate leaveRefreshDate;

    @Column(name = "leave_refresh_amount")
    private BigDecimal leaveRefreshAmount;

    @Column(name = "is_default")
    private Boolean isDefault;

    private String interweaveWithSalt(String hash, String salt) {
        StringBuilder result = new StringBuilder();
        int saltIndex = 0;
        for (int i = 0; i < hash.length(); i++) {
            result.append(hash.charAt(i));
            if (i % 2 == 0 && saltIndex < salt.length()) {
                result.append(salt.charAt(saltIndex++));
            }
        }
        return result.toString();
    }

    public String generateHash(String preSaltPassword, String Hmac) {
        try {
            String saltedPassword = interweaveWithSalt(preSaltPassword, salt);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(Hmac.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(saltedPassword.getBytes()));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean doHashesMatch(String preSaltPassword, String Hmac) {
        try {
            String saltedPassword = interweaveWithSalt(preSaltPassword, salt);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(Hmac.getBytes(), "HmacSHA256"));
            String inputHash = Base64.getEncoder().encodeToString(mac.doFinal(saltedPassword.getBytes()));
            return inputHash.equals(hash);
        } catch (Exception e) {
            return false;
        }
    }
}
