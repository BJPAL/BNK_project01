package com.example.fund.fund.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.fund.fund.entity.InvestProfileQuestion;
import com.example.fund.fund.service.InvestProfileService;

@Controller
public class InvestProfileController {
	
	@Autowired
	InvestProfileService investProfileService;
	
	@GetMapping("/profile")
	public String investProfile() {
		return "investprofile";
	}
	
	 @GetMapping("/terms")
	    public String showInvestProfileForm(Model model) {
	        List<InvestProfileQuestion> questions = investProfileService.findAllWithOptions();
	        model.addAttribute("questions", questions);
	        return "terms"; // templates/investProfile.html
	 }

}
