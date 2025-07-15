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
@Table(name = "fund_daily_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundDailyPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "base_date")
    private LocalDate baseDate;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    // 추가됨
    @Column(name = "change_amount", precision = 10, scale = 2)
    private BigDecimal changeAmount;

    @Column(name = "aum")
    private Integer aum;

    // 추가됨
    @Column(name = "fund_scale")
    private Integer fundScale;
}