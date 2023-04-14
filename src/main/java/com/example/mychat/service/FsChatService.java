package com.example.mychat.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.example.mychat.common.BaseConstants;
import com.example.mychat.pojo.fs.FsTokenResp;
import com.example.mychat.util.CacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FsChatService {

    /**
     * 将消息发送到飞书用户
     *
     * @param result chatgpt的返回结果
     * @param toUser 飞书用户
     * @return
     */
    public String sendMsgToFs(String result, String toUser) {
        String accessToken = CacheHelper.getToken("FsAccessToken");
        if (StringUtils.isEmpty(accessToken)) {
            accessToken = getFsAccessToken();
        }

        if ("获取token失败".equals(accessToken)) {
            return "获取飞书调用接口凭证失败";
        }

        //请求头
        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=utf-8");
        mapHeaders.put("Authorization", "Bearer " + accessToken);
        //url
        String url = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=user_id";
        //body
        JSONObject params = new JSONObject();
        params.put("uuid", UUID.randomUUID().toString());
        params.put("msg_type", "text");
        params.put("receive_id", toUser);
        JSONObject content = new JSONObject();
        content.put("text", result);
        params.put("content", content.toJSONString());


        HttpResponse response = HttpRequest.post(url)
                .headerMap(mapHeaders, true)
                .body(params.toJSONString())
                .execute();

        JSONObject jsonObject = JSONObject.parseObject(response.body());

        Integer code = jsonObject.getInteger("code");
        if (code != 0) {
            log.info("发送消息失败，code:{},msg:{}", code, jsonObject.getString("msg"));
            return "发送消息失败";
        }

        return "success";
    }


    /**
     * 获取飞书调用接口凭证
     *
     * @return accessToken
     */
    public String getFsAccessToken() {
        String appid = BaseConstants.FsAppID;
        String appsecret = BaseConstants.FsAppSecret;
        //请求头
        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=utf-8");

        //body
        JSONObject params = new JSONObject();
        params.put("app_id", appid);
        params.put("app_secret", appsecret);

        String url = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

        HttpResponse response = HttpRequest.post(url)
                .headerMap(mapHeaders, true)
                .body(params.toJSONString())
                .execute();

        FsTokenResp fsTokenResp = JSONObject.parseObject(response.body(), FsTokenResp.class);

        Integer code = fsTokenResp.getCode();
        if (code != 0) {
            log.info("获取token失败，code:{},msg:{}", code, fsTokenResp.getMsg());
            return "获取token失败";
        }

        String accessToken = fsTokenResp.getTenant_access_token();
        CacheHelper.setToken("FsAccessToken", accessToken);
        return accessToken;
    }
}
