package com.example.fund.fund.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invest_profile_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InvestProfileResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resultId;

    @Column(nullable = false)
    private Integer userId;

    // 선택한 옵션 정보: { "1": 2, "2": 3, "3": [1, 4] } 등 JSON 형태
    @Lob
    @Column(nullable = false)
    private String questionOption;

    @Column(nullable = false)
    private int totalScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private InvestProfileType type;

    @Column(nullable = false)
    private LocalDateTime analysisDate;

    private LocalDateTime signedAt;
}
