package com.example.mychat.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.example.mychat.pojo.wx.WxTokenResp;
import lombok.extern.slf4j.Slf4j;
import com.example.mychat.common.BaseConstants;
import com.example.mychat.util.CacheHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WxChatService {

    /**
     * 将消息发送到微信用户
     *
     * @param result chatgpt的返回结果
     * @param toUser 微信用户
     * @return
     */
    public String sendMsgToWx(String result, String toUser) {
        String accessToken = CacheHelper.getToken("WxAccessToken");
        if (StringUtils.isEmpty(accessToken)) {
            accessToken = getWxAccessToken();
        }

        if ("获取token失败".equals(accessToken)) {
            return "获取微信调用接口凭证失败";
        }

        //请求头
        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=UTF-8");

        //url
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
        //body
        JSONObject params = new JSONObject();
        params.put("touser", toUser);
        params.put("msgtype", "text");
        JSONObject content = new JSONObject();
        content.put("content", result);
        params.put("text", content);
        params.put("agentid", BaseConstants.WxAgentID);


        HttpResponse response = HttpRequest.post(url)
                .headerMap(mapHeaders, true)
                .body(params.toJSONString())
                .execute();

        JSONObject jsonObject = JSONObject.parseObject(response.body());

        Integer errcode = jsonObject.getInteger("errcode");
        if (errcode != 0) {
            log.info("发送消息失败，errcode:{},errmsg:{}", errcode, jsonObject.getString("errmsg"));
            return "发送消息失败";
        }

        return "success";
    }

    /**
     * 获取微信调用接口凭证
     *
     * @return accessToken
     */
    public String getWxAccessToken() {
        String corpid = BaseConstants.WxCorpID;
        String corpsecret = BaseConstants.WxCorpSecret;
        //请求头
        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=UTF-8");

        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpid + "&corpsecret=" + corpsecret;

        HttpResponse response = HttpRequest.get(url)
                .headerMap(mapHeaders, true)
                .execute();

        WxTokenResp wxTokenResp = JSONObject.parseObject(response.body(), WxTokenResp.class);

        Integer errcode = wxTokenResp.getErrcode();
        if (errcode != 0) {
            log.info("获取token失败，errcode:{},errmsg:{}", errcode, wxTokenResp.getErrmsg());
            return "获取token失败";
        }

        String accessToken = wxTokenResp.getAccess_token();
        CacheHelper.setToken("WxAccessToken", accessToken);
        return accessToken;
    }
}
