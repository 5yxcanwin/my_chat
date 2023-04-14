package com.example.mychat.pojo.gpt;

import lombok.Data;

import java.util.List;

/**
 * 调用chatgpt-api的请求参数
 */
@Data
public class CompletionReq {

    private String content;

    private String model;

    private List<Message> Messages;

    private Integer max_tokens;

}
