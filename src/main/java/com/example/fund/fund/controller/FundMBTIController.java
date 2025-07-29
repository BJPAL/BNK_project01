package com.example.fund.fund.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FundMBTIController {
	
	@GetMapping("/mbti")
	public String fundMbti() {
		return "mbti";
	}
	

}
