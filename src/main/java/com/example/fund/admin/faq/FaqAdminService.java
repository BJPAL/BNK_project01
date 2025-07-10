package com.example.fund.admin.serviceTest;

import com.example.fund.faq.entity.Faq;
import com.example.fund.faq.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqAdminService {

    private final FaqRepository faqRepository;

    public List<Faq> findAll() {
        return faqRepository.findAll();
    }

    public void save(Faq faq) {
        faqRepository.save(faq);
    }

    public Faq findById(Integer id) {
        return faqRepository.findById(id).orElse(null);
    }

    public void update(Integer id, Faq newFaq) {
        Faq existing = faqRepository.findById(id).orElseThrow();
        existing.setQuestion(newFaq.getQuestion());
        existing.setAnswer(newFaq.getAnswer());
        faqRepository.save(existing);
    }

    public void delete(Integer id) {
        faqRepository.deleteById(id);
    }

    public List<Faq> findActiveFaqs() {
        return faqRepository.findByActiveTrue();
    }

    public Page<Faq> search(String keyword, Pageable pageable) {
        return faqRepository.findByQuestionContainingOrAnswerContaining(keyword, keyword, pageable);
    }
}
