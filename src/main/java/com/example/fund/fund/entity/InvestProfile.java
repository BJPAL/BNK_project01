package com.example.fund.fund.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invest_survey")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InvestProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String question;

    @Column(length = 255, nullable = false)
    private String option;

    @Column(nullable = false)
    private Integer score;
}