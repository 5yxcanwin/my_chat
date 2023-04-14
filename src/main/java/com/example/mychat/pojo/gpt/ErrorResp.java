package com.example.mychat.pojo.gpt;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@Builder
public class ErrorResp implements Serializable {

    private Error error;

    @Data
    @ToString
    public static class Error implements Serializable
    {
        private String message;
        private String type;
        private String param;
        private String code;
    }

}