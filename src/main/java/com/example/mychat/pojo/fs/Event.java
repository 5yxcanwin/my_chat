package com.example.mychat.pojo.fs;

import lombok.Data;


@Data
public class Event {
    private Message message;

    private Sender sender;
}