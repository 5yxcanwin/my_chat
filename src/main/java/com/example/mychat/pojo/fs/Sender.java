package com.example.mychat.pojo.fs;

import lombok.Data;

//创建Sender内部类
@Data
public class Sender {
    private SenderId sender_id;

    private String sender_type;

    private String tenant_key;


}