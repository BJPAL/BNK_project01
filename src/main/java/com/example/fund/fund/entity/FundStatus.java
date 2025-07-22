package com.example.fund.fund.entity;

import com.example.fund.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fund_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "category", length = 10, nullable = false)
    private String category; // 국내 or 해외

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Column(length = 3000)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;
    // private Integer viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "aum")
    private Integer aum; // 억원 단위

    @Column(name = "fund_scale")
    private Integer fundScale; // 설정액

    @Column(name = "fund_grade", length = 10)
    private String fundGrade;

    @Column(name = "updated_date")
    private LocalDate updatedDate;
}
