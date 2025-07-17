package com.example.fund.fund.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fund.fund.entity.FundStatus;
import com.example.fund.fund.service.FundStatusService;

@Controller
public class FundStatusController {
	
	@Autowired
	FundStatusService statusService;
	
	@GetMapping("/fund_status")
	public String fundStatusPage(@RequestParam(name="page", defaultValue="0") int page, Model model) {
		Page<FundStatus> fundPage = statusService.getPagedStatusList(page, 10);
		model.addAttribute("statusList", fundPage.getContent());
		model.addAttribute("totalCount", statusService.getTotalCount());
		model.addAttribute("totalPages", fundPage.getTotalPages()); // 전체 페이지 수
	    model.addAttribute("currentPage", page); // 현재 페이지 번호
		return "fund_status";
	}
	
	@GetMapping("/fund_status_detail/{id}")
	public String detailPage(@PathVariable("id") Integer id, Model model) {
		
		FundStatus fund = statusService.incrementViewCount(id);

		model.addAttribute("fund", fund);
		model.addAttribute("prev", statusService.getPrevStatus(id));
        model.addAttribute("next", statusService.getNextStatus(id));
		return "status_detail";
	}
}
