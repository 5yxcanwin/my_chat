package com.example.mychat.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.mychat.common.BaseConstants;
import com.example.mychat.pojo.gpt.*;
import com.example.mychat.util.CacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ChatGPTService {

    /**
     * 将消息发送到chatgpt
     *
     * @param fromUser 用户
     * @param content  消息
     * @return
     */
    public String sendMsgToGPT(String fromUser, String content) {
        //请求头
        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put("Content-Type", "application/json; charset=UTF-8");
        mapHeaders.put("Authorization", "Bearer " + BaseConstants.api_key);
        //请求体
        CompletionReq completionReq = new CompletionReq();
        //消息列表
        List<Message> Messages = CacheHelper.getGPTCache(fromUser);

        Message Message = new Message();
        Message.setRole("user");
        Message.setContent(content);
        Messages.add(Message);



        completionReq.setMessages(Messages);
        //模型
        completionReq.setModel(BaseConstants.model);
        //最大token
        completionReq.setMax_tokens(BaseConstants.max_tokens);


        //url
        String url = "https://api.openai.com/v1/chat/completions";

        HttpResponse response = HttpRequest.post(url)
                .headerMap(mapHeaders, true)
                .body(JSONObject.toJSONString(completionReq))
                .execute();

        int code = response.getStatus();

        if (code == 401) {
            log.error(">>>>completions.response:{}", response.body());
            return "未授权的操作!ApiKey错误";

        }


        if (response.getStatus() != 200) {
            log.error(">>>>completions.response:{}", response.body());

            ErrorResp error = JSON.parseObject(response.body(), ErrorResp.class);
            return error.getError().getMessage();
        }


        try {
            CompletionResp completionResp = JSON.parseObject(response.body(), CompletionResp.class);

            List<Choices> choices = completionResp.getChoices();
            String result = choices.stream().map(Choices::getContent).collect(Collectors.joining(""));

            if (Messages.size() >= BaseConstants.max_chat_count) {
                CacheHelper.setUserChatFlowClose(fromUser);
                log.info(">>>>用户{}连续对话超过{}次，自动关闭", fromUser, BaseConstants.max_chat_count);
                return result+ "\n\n连续对话次过多，已刷新对话";
            } else {
                Message asistantMsg = new Message();
                asistantMsg.setRole("assistant");
                asistantMsg.setContent(result);
                Messages.add(asistantMsg);
                CacheHelper.setGPTCache(fromUser, Messages);
            }
            return result;
        } catch (Exception e) {
            return "JSON格式解析失败";
        }
    }


}
