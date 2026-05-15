package com.staffs.leaveservice.service;

import com.staffs.leaveservice.dto.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ErrorService {
    public ResponseEntity<ResponseDto<?>> handleUnauthorized(Exception e) {
        ResponseDto<?> responseDto = new ResponseDto<>(e.getMessage());
        return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<ResponseDto<?>> handleNotFound(Exception e) {
        ResponseDto<?> responseDto = new ResponseDto<>(e.getMessage());
        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<ResponseDto<?>> handleBadRequest(Exception e) {
        ResponseDto<?> responseDto = new ResponseDto<>(e.getMessage());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ResponseDto<?>> handleBadRequest(String message) {
        ResponseDto<?> responseDto = new ResponseDto<>(message);
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ResponseDto<?>> handleGenericError(Exception e) {
        ResponseDto<?> responseDto = new ResponseDto<>(e.getMessage());
        return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<ResponseDto<?>> handleTooManyRequests(Exception e) {
        ResponseDto<?> responseDto = new ResponseDto<>(e.getMessage());
        return new ResponseEntity<>(responseDto, HttpStatus.TOO_MANY_REQUESTS);
    }
}