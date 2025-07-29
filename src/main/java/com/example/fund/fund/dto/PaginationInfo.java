package com.example.fund.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationInfo {
    private int page;
    private int limit;
    private long total;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrev;
    private int currentItems;

    public static PaginationInfo from(Page<?> page, int requestPage) {
        return PaginationInfo.builder()
                .page(requestPage)
                .limit(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(requestPage < page.getTotalPages())
                .hasPrev(requestPage > 1)
                .currentItems(page.getNumberOfElements())
                .build();
    }
}