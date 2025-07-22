package com.example.fund.fund.entity;

<<<<<<< Updated upstream
import com.example.fund.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
=======
import java.time.LocalDate;

import com.example.fund.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
>>>>>>> Stashed changes
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
<<<<<<< Updated upstream
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
}
=======
    private Long statusId;

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
>>>>>>> Stashed changes
