package com.example.mychat.pojo.gpt;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@Builder
public class CompletionResp implements Serializable
{
    private String id;

    private String object;

    private Long created;

    private List<Choices> choices;

    private Usage usage;

}
