package com.staffs.leaveservice.service.helper;

import com.staffs.leaveservice.constants.ConstantsProvider;
import com.staffs.leaveservice.dto.response.ResponseDto;
import com.staffs.leaveservice.exception.AuthenticationException;
import com.staffs.leaveservice.exception.InvalidLeaveException;
import com.staffs.leaveservice.exception.ResourceNotFoundException;
import com.staffs.leaveservice.exception.UnauthorizedException;
import com.staffs.leaveservice.service.ErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler extends ErrorService {

    @Autowired
    private ConstantsProvider constantsProvider;

    // Controller level exceptions

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ResponseDto<?>> handleInvalidRequestBody(Exception e){
        log.error(constantsProvider.getERROR_SERVICE_INVALID_BODY());
        log.trace(constantsProvider.getERROR_SERVICE_INVALID_BODY(), e);
        return handleBadRequest(constantsProvider.getERROR_SERVICE_INVALID_BODY());
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    ResponseEntity<ResponseDto<?>> handleInvalidRequestMethod(Exception e){
        log.error(constantsProvider.getERROR_SERVICE_INVALID_METHOD());
        log.trace(constantsProvider.getERROR_SERVICE_INVALID_BODY(), e);
        return handleBadRequest(constantsProvider.getERROR_SERVICE_INVALID_METHOD());
    }

    // Service level exceptions

    @ExceptionHandler({AuthenticationException.class, UnauthorizedException.class})
    ResponseEntity<ResponseDto<?>> handleUnauthorizedException(Exception e){
        log.trace(e.getMessage(), e);
        return handleUnauthorized(e);
    }

    @ExceptionHandler({ResourceNotFoundException.class, IllegalArgumentException.class, InvalidLeaveException.class})
    ResponseEntity<ResponseDto<?>> handleBadRequestException(Exception e){
        log.trace(e.getMessage(), e);
        return handleBadRequest(e);
    }

    // Everything else

    @ExceptionHandler(Exception.class)
    ResponseEntity<ResponseDto<?>> handleControllerError(Exception e){
        log.trace(e.getMessage(), e);
        return handleGenericError(e);
    }
}
