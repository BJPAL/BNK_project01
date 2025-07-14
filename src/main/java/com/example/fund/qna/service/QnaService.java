package com.example.fund.qna.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.fund.qna.entity.Qna;
import com.example.fund.qna.repository.QnaRepository;

@Service
public class QnaService {

    @Autowired
    private QnaRepository qnaRepository;
    
    public int countUnanwseQna(){
        return qnaRepository.countByStatus("대기");
    }

    public List<Qna> getQnaList(String status){
        return qnaRepository.findByStatus(status);
    }

    public Qna getQna(Integer id){
        Optional<Qna> optionalQna = qnaRepository.findById(id);
        Qna qna = optionalQna.get();

        return qna;
    }

    public void SubmitAnswer(Integer qnaId, String answer){
		Qna qna = qnaRepository.findById(qnaId).orElseThrow();

		qna.setAnswer(answer);
		qna.setStatus("완료");

		qnaRepository.save(qna);
	}


    public List<Qna> getQnaListByUser(int userId) {
        return qnaRepository.findByUser_UserIdOrderByRegDateDesc(userId);
    }
    public Qna getQnaById(Long qnaId) {
        return qnaRepository.findById(Math.toIntExact(qnaId))
                .orElseThrow(() -> new NoSuchElementException("문의가 존재하지 않습니다."));
    }

}