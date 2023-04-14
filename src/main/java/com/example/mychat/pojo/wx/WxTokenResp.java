package com.example.mychat.pojo.wx;

import lombok.Data;

@Data
public class WxTokenResp {

    public Integer errcode;

    public String errmsg;

    public String access_token;

    public Integer expires_in;
}
