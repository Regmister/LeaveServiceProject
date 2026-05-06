package com.staffs.leaveservice.service.helper;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.entity.EmployeeEntity;
import com.staffs.leaveservice.exception.AuthenticationException;
import com.staffs.leaveservice.exception.ResourceNotFoundException;
import com.staffs.leaveservice.exception.UnauthorizedException;
import com.staffs.leaveservice.repository.EmployeeRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class SecurityServiceHelper {

    @Autowired
    protected EmployeeRepository employeeRepository;

    @Autowired
    protected ConstantsProvider constantsProvider;

    @Value("${jwt-secret}")
    private String SECRET;

    public Boolean isUser(Long id){
        return compareRoles(id, EmployeeEntity.Role.USER);
    }

    public Boolean isManager(Long id){
        return compareRoles(id, EmployeeEntity.Role.MANAGER);
    }

    public Boolean isAdmin(Long id){
        return compareRoles(id, EmployeeEntity.Role.ADMIN);
    }

    private Boolean compareRoles(Long id, EmployeeEntity.Role role){
        try {
            Optional<EmployeeEntity> employeeEntity = employeeRepository.findByEmployeeId(id);
            return employeeEntity.get().getRole() == role;
        } catch (Exception e) {
            throw new AuthenticationException(constantsProvider.getERROR_JWT_FAILURE());
        }
    }

    public Boolean isDefault(Long employeeId){
        EmployeeEntity employeeEntity = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(constantsProvider.getERROR_USER_FAILURE()));
        return employeeEntity.getIsDefault();
    }

    // Used for validating a token + checking if it belongs to passed ID
    public Boolean isJwtValid(String token, Long id) {
        try {
            Long extractedId = extractId(token);
            if (!extractedId.equals(id)){
                return false;
            }
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Used for validating a token
    public Boolean isJwtValid(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public String generateJwt(Long username) {
        return Jwts.builder()
                .setSubject(username.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
}
