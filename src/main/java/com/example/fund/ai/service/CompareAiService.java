package com.example.fund.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompareAiService {

    @Autowired
    private ChatClient chatClient;

    public String talk(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}