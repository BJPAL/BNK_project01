package com.example.fund.fund.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fund.fund.entity.InvestProfileQuestion;
import com.example.fund.fund.entity.InvestProfileResult;
import com.example.fund.fund.service.InvestProfileService;
import com.example.fund.user.entity.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class InvestProfileController {
	
	@Autowired
	InvestProfileService investProfileService;
	
	@GetMapping("/profile")
	public String investProfile(HttpSession session, Model model) {
		User loginUser = (User)session.getAttribute("user");
		if (loginUser == null) return "redirect:/auth/login";
		model.addAttribute("user", loginUser);
		Optional<InvestProfileResult> optional = investProfileService.getLatestResult(loginUser);
		
	    if (optional.isPresent()) {
	        InvestProfileResult result = optional.get();
	        model.addAttribute("result", result);
	        model.addAttribute("riskType", result.getType().getTypeName());
	        model.addAttribute("lastChanged", result.getAnalysisDate().toLocalDate());
	        model.addAttribute("canInvestTerm", investProfileService.extractAnswerText(result.getAnswerSnapshot(), "투자가능기간"));
	        model.addAttribute("experienceTerm", investProfileService.extractAnswerText(result.getAnswerSnapshot(), "투자경험기간"));
	    } else {
	        model.addAttribute("riskType", "설문대상자");
	        model.addAttribute("lastChanged", "&nbsp;");
	        model.addAttribute("canInvestTerm", "&nbsp;");
	        model.addAttribute("experienceTerm", "&nbsp;");
	    }
		return "investprofile";
	}
	
	 @GetMapping("/terms")
	 public String showInvestProfileForm(Model model, HttpSession session) {
		User loginUser = (User)session.getAttribute("user");
	    List<InvestProfileQuestion> questions = investProfileService.findAllWithOptions();
	    model.addAttribute("questions", questions);
	    return "terms"; // templates/investProfile.html
	 }
	 
	 @PostMapping("/analyze")
	 public String analyze(@RequestParam Map<String, String> paramMap, HttpSession session) {
		 User loginUser = (User)session.getAttribute("user");
		 if (loginUser == null) {
	            return "redirect:/auth/login";
	        }
		 // 분석 및 저장
		 investProfileService.analyzeAndSave(loginUser.getUserId(), paramMap);
		 return "redirect:/profile";
	 }

}
