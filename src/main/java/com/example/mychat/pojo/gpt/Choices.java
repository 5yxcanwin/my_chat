package com.example.mychat.pojo.gpt;


import lombok.Data;

@Data
public class Choices {

    private Integer index;

    private Message Message;

    private String logprobs;

    private String finish_reason;

    public String getContent() {
        if(Message == null) {
            return "";
        }
        return Message.getContent();
    }
}
