package com.example.fund.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fund.ai.service.CompareAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/ai")
public class CompareAiController {

    @Autowired
    private CompareAiService compareAiService;

    @GetMapping("/talk")
    public String getMethodName(@RequestParam("message") String message) {
        System.out.println("recieved message param : " + message);

        return compareAiService.talk(message);
    }

}
