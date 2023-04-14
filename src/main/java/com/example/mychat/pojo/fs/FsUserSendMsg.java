package com.example.mychat.pojo.fs;

import lombok.Data;

/**
 * 用户发出的消息
 */
@Data
public class FsUserSendMsg {
    private String schema;

    private Header header;

    private Event event;
}