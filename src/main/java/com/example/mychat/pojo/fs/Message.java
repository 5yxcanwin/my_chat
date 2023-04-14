package com.example.mychat.pojo.fs;

import lombok.Data;


@Data
public class Message {
    private String message_id;

    private String chat_id;

    private String chat_type;

    private String message_type;

    private String content;

    private String create_time;
}