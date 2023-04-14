package com.example.mychat.pojo.fs;

import lombok.Data;


@Data
public class Header {
    private String event_id;

    private String token;

    private String create_time;

    private String event_type;

    private String tenant_key;

    private String app_id;
}
