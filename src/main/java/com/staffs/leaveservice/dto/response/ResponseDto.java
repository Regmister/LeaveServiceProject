package com.staffs.leaveservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {

    public ResponseDto(String _message, T _data){
        this.setStatus(Status.SUCCESS);
        this.setMessage(_message);
        this.setData(_data);
    }

    public ResponseDto(String _message){
        this.setStatus(Status.FAILURE);
        this.setMessage(_message);
    }

    public enum Status {
        SUCCESS,
        FAILURE
    }

    private String message;
    private Status status;
    private T data;
}
