package com.example.fund.fund.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "fund_portfolio_country")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPortfolioCountry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "country_name", length = 50)
    private String countryName;

    @Column(name = "stock_ratio", precision = 5, scale = 2)
    private BigDecimal stockRatio;

    @Column(name = "updated_date")
    private LocalDate updatedDate;
}
