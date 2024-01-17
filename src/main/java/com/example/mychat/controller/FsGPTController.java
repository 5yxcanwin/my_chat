package com.example.mychat.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.mychat.common.BaseConstants;
import com.example.mychat.pojo.fs.FsUserSendMsg;
import com.example.mychat.service.ChatGPTService;
import com.example.mychat.service.FsChatService;
import com.example.mychat.util.CacheHelper;
import com.example.mychat.util.fs.FsMsgcrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;


@RestController
@RequestMapping("/FeiShu")
@Slf4j
public class FsGPTController {

    @Resource
    private ChatGPTService chatGPTService;

    @Resource
    private FsChatService fsChatService;

    //配置url时临时使用，后续删除或注释掉
    // @PostMapping("/chat")
    private JSONObject verify(@RequestBody String encryptString) throws Exception {
        JSONObject object = JSONObject.parseObject(encryptString);
        String encrypt = object.getString("encrypt");
        //解密
        FsMsgcrypt decrypt = new FsMsgcrypt(BaseConstants.FsEncryptKey);
        String result = decrypt.decrypt(encrypt);
        JSONObject jsonObject = JSON.parseObject(result);
        // 123
        //验证token
        String token = jsonObject.getString("token");
        if (!BaseConstants.FsVerificationToken.equals(token)) {
            log.error("FeiShu verify error,token invalid,token={}", token);
            return null;
        }

        String challengeString = jsonObject.getString("challenge");
        log.info("verifyurl challenge: " + challengeString);
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("challenge", challengeString);
        return jsonResult;

    }

    //配置url时需要注释掉
    @PostMapping("/chat")
    private String chat(@RequestBody String encryptString,
                        HttpServletRequest request) throws Exception {
        log.info("------飞书------:chat方法被调用");
        String requestId = request.getHeader("x-request-id");
        String nonce = request.getHeader("x-lark-request-nonce");
        String timestamp = request.getHeader("x-lark-request-timestamp");
        String signature = request.getHeader("x-lark-signature");

        //判断是否重复请求
        String requestIdCache = CacheHelper.getRequestFsCache(requestId);

        if (StringUtils.isNotEmpty(requestIdCache)) {
            log.error("------飞书------:重复请求");
            return "fail";
        } else {
            CacheHelper.setRequestFsCache(requestId, requestId);
        }

        //验证签名
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append(nonce).append(BaseConstants.FsEncryptKey).append(encryptString);
        MessageDigest alg = MessageDigest.getInstance("SHA-256");
        String sign = Hex.encodeHexString(alg.digest(sb.toString().getBytes()));
        if (!sign.equals(signature)) {
            log.error("------飞书------:签名验证失败");
            return "fail";
        }

        //解密
        JSONObject object = JSONObject.parseObject(encryptString);
        String encrypt = object.getString("encrypt");
        FsMsgcrypt decrypt = new FsMsgcrypt(BaseConstants.FsEncryptKey);
        String decryptResult = decrypt.decrypt(encrypt);


        FsUserSendMsg fsUserSendMsg = JSON.parseObject(decryptResult, FsUserSendMsg.class);
        String content = fsUserSendMsg.getEvent().getMessage().getContent();
        String userId = fsUserSendMsg.getEvent().getSender().getSender_id().getUser_id();

        String fromUser = "FeiShu" + userId;

        log.info("------飞书------:调用openai");
        // 调openai
        String result = chatGPTService.sendMsgToGPT(fromUser, content);
        log.info("------飞书------:result: " + result);
        //给微信发消息
        log.info("------飞书------:调用飞书api");
        String send = fsChatService.sendMsgToFs(result, userId);
        log.info("------飞书------:send: " + send);

        return "success";
    }

}
