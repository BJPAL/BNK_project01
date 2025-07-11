package com.example.fund.qna.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.fund.qna.entity.Qna;
import com.example.fund.qna.repository.QnaRepository;
import com.example.fund.user.entity.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class QnaController {
	
	@Autowired
	QnaRepository qnaRepository;

	@GetMapping("/qna")
	public String qnaPage(HttpSession session, Model model) {
		User loginUser = (User)session.getAttribute("user");
		if(loginUser == null) {
			return "redirect:/auth/login";
		}
		model.addAttribute("user", loginUser);
		return "qna";
	}
	
	@PostMapping("/regQna")
	@ResponseBody
	public Map<String, Object> regQna(@RequestBody Qna qna, HttpSession session) {
		User loginUser = (User)session.getAttribute("user");
	    qna.setStatus("대기");
	    qna.setUser(loginUser);
	    qnaRepository.save(qna);
	    
	    Map<String, Object> result = new HashMap<>();
	    result.put("result", "success");
	    result.put("redirectUrl", "/qnaSuccess");
	    return result;
	}
	
	@GetMapping("qnaSuccess")
	public String qnaSuccess(HttpSession session) {
		User loginUser = (User) session.getAttribute("user");
		if(loginUser == null) {
			return "redirect:/auth/login";
		}
		return "qna_success";
	}
}
