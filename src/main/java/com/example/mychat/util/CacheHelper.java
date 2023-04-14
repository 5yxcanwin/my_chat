package com.example.mychat.util;

import com.example.mychat.pojo.gpt.Message;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class CacheHelper {

    //接口调用凭证缓存
    private static Cache<String, String> tokenCache;

    //用户连续对话缓存
    private static Cache<String, List<Message>> chatGPTCache;

    //保存飞书请求的唯一标识
    private static Cache<String,String> requestFsCache;

    private static Cache<String,String> requestWxCache;



    static {
        tokenCache = CacheBuilder.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .build();

        chatGPTCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        //飞书的请求间隔为7.1小时，即426分钟，烦
        requestFsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(427, TimeUnit.MINUTES)
                .build();


        requestWxCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public static void setToken(String key, String value) {
        tokenCache.put(key, value);
    }

    public static String getToken(String key) {
        return tokenCache.getIfPresent(key);
    }

    public static void setRequestFsCache(String key, String value) {requestFsCache.put(key, value);}

    public static String getRequestFsCache(String key) {return requestFsCache.getIfPresent(key);}

    public static void setRequestWxCache(String key, String value) {requestWxCache.put(key, value);}

    public static String getRequestWxCache(String key) {return requestWxCache.getIfPresent(key);}

    public static void setGPTCache(String username, List<Message> gptMessages) {
        chatGPTCache.put(username, gptMessages);
    }

    public static List<Message> getGPTCache(String username) {
        List<Message> gptMessages = chatGPTCache.getIfPresent(username);
        if (CollectionUtils.isEmpty(gptMessages)) {
            return Lists.newArrayList();
        }
        return gptMessages;
    }


    public static void setUserChatFlowClose(String username) {
        chatGPTCache.invalidate(username);
    }


}
