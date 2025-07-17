package com.example.fund.fund.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.fund.fund.entity.FundStatus;
import com.example.fund.fund.repository.FundStatusRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class FundStatusService {
	
	@Autowired
	FundStatusRepository fundStatusRepository;
	
	public List<FundStatus> statusList(){
		return fundStatusRepository.findAll();
	}
	
	public long  getTotalCount() {
		return fundStatusRepository.count();
	}
	
	public Page<FundStatus> getPagedStatusList(int page, int size){
		Pageable pageable = PageRequest.of(page, size, Sort.by("regDate").descending());
		return fundStatusRepository.findAll(pageable);
	}

    public FundStatus getPrevStatus(Integer id) {
        return fundStatusRepository.findTopByStatusIdLessThanOrderByStatusIdDesc(id);
    }

    public FundStatus getNextStatus(Integer id) {
        return fundStatusRepository.findTopByStatusIdGreaterThanOrderByStatusIdAsc(id);
    }
    
    @Transactional
    public void incrementViewCount(Integer id) {
		FundStatus fund = fundStatusRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("해당 글이 존재하지 않습니다."));
		fund.setViewCount(fund.getViewCount() + 1);
	}
	
	public FundStatus getDetail(Integer id) {
	    return fundStatusRepository.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
	}
}
