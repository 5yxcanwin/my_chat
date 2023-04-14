package com.example.mychat.controller;


import com.example.mychat.common.BaseConstants;
import com.example.mychat.service.ChatGPTService;
import com.example.mychat.service.WxChatService;
import com.example.mychat.util.CacheHelper;
import com.example.mychat.util.wx.WXBizMsgCrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/WxChat")
@Slf4j
public class WxGPTController {

    @Resource
    private ChatGPTService chatGPTService;

    @Resource
    private WxChatService wxChatService;

    @RequestMapping("/test")
    private String test() {
        log.error("-----------------------------test-----------------------");
        return "test";
    }

    @GetMapping("/chat")
    private String verify(@RequestParam("msg_signature") String msg_signature,
                          @RequestParam("timestamp") String timestamp,
                          @RequestParam("nonce") String nonce,
                          @RequestParam("echostr") String echostr) throws Exception {
        log.info("chat, msg_signature:{}, timestamp:{}, nonce:{}, echostr:{}",
                msg_signature, timestamp, nonce, echostr);
        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(BaseConstants.WxToken, BaseConstants.WxEncodingAESKey, BaseConstants.WxCorpID);
        try {
            String sEchoStr = wxcpt.VerifyURL(msg_signature, timestamp,
                    nonce, echostr);
            log.info("verifyurl echostr: " + sEchoStr);

            return sEchoStr;
        } catch (Exception e) {
            //验证URL失败，错误原因请查看异常
            log.error("verifyurl error,e={}", e);
            return "";
        }
    }


    @PostMapping(value = "/chat",
            consumes = {"application/xml", "text/xml"},
            produces = "application/xml;charset=utf-8")
    private String chat(@RequestParam("msg_signature") String msg_signature,
                        @RequestParam("timestamp") String timestamp,
                        @RequestParam("nonce") String nonce,
                        @RequestBody String WxUserSendMsg) throws Exception {

        log.info("------企微------:chat方法被调用");

        //签名验证、解密
        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(BaseConstants.WxToken, BaseConstants.WxEncodingAESKey, BaseConstants.WxCorpID);
        String xmlcontent = wxcpt.DecryptMsg(msg_signature, timestamp, nonce, WxUserSendMsg);

        String content = StringUtils.substringBetween(xmlcontent, "<Content><![CDATA[", "]]></Content>");
        String user = StringUtils.substringBetween(xmlcontent, "<FromUserName><![CDATA[", "]]></FromUserName>");

        //防止重复请求
        String fromUser = "WxChat" + user;
        String createTime = StringUtils.substringBetween(xmlcontent, "<CreateTime>", "</CreateTime>");
        String requestId = fromUser + createTime;
        String requestIdCache = CacheHelper.getRequestWxCache(requestId);
        if (StringUtils.isNotEmpty(requestIdCache)) {
            log.error("------企微------:重复请求");
            return "fail";
        } else {
            CacheHelper.setRequestWxCache(requestId, requestId);
        }

        log.info("------企微------:调用openai");
        // 调openai
        String result = chatGPTService.sendMsgToGPT(fromUser, content);
        log.info("------企微------:result: " + result);

        //给微信发消息
        log.info("------企微------:调用微信api");
        String send = wxChatService.sendMsgToWx(result, user);
        log.info("------企微------:send" + send);
        return "success";
    }

}
