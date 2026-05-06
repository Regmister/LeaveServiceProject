package com.staffs.leaveservice;

import com.staffs.leaveservice.dto.response.ResponseDto;
import com.staffs.leaveservice.service.ErrorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ErrorServiceTests {

    private ErrorService errorService;

    @BeforeEach
    void setUp() {
        errorService = new ErrorService();
    }

    @Test
    void handleUnauthorized_returns401() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleUnauthorized(new Exception("Unauthorized"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleUnauthorized_bodyContainsMessage() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleUnauthorized(new Exception("Unauthorized"));

        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().getMessage());
        assertEquals(ResponseDto.Status.FAILURE, response.getBody().getStatus());
    }

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleNotFound(new Exception("Not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleNotFound_bodyContainsMessage() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleNotFound(new Exception("Not found"));

        assertNotNull(response.getBody());
        assertEquals("Not found", response.getBody().getMessage());
        assertEquals(ResponseDto.Status.FAILURE, response.getBody().getStatus());
    }

    @Test
    void handleBadRequest_exception_returns400() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleBadRequest(new Exception("Bad request"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleBadRequest_exception_bodyContainsMessage() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleBadRequest(new Exception("Bad request"));

        assertNotNull(response.getBody());
        assertEquals("Bad request", response.getBody().getMessage());
        assertEquals(ResponseDto.Status.FAILURE, response.getBody().getStatus());
    }

    @Test
    void handleBadRequest_string_returns400() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleBadRequest("Invalid input");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleBadRequest_string_bodyContainsMessage() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleBadRequest("Invalid input");

        assertNotNull(response.getBody());
        assertEquals("Invalid input", response.getBody().getMessage());
        assertEquals(ResponseDto.Status.FAILURE, response.getBody().getStatus());
    }

    @Test
    void handleGenericError_returns500() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleGenericError(new Exception("Something went wrong"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleGenericError_bodyContainsMessage() {
        ResponseEntity<ResponseDto<?>> response = errorService.handleGenericError(new Exception("Something went wrong"));

        assertNotNull(response.getBody());
        assertEquals("Something went wrong", response.getBody().getMessage());
        assertEquals(ResponseDto.Status.FAILURE, response.getBody().getStatus());
    }
}
