package com.example.fund.fund.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FundMainController {
	
	@GetMapping("/fund")
	public String fundMain() {
		return "fundMain";
	}
}
