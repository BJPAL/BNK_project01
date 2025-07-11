package com.example.fund.fund.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InvestProfileController {
	
	@GetMapping("/profile")
	public String investProfile() {
		return "investProfile/investprofile";
	}
}
