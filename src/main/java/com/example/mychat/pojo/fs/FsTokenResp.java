package com.example.mychat.pojo.fs;

import lombok.Data;

@Data
public class FsTokenResp {

    public Integer code;

    public String msg;

    public String tenant_access_token;

    public Integer expire;
}
