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
@Table(name = "fund_asset_allocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundAssetAllocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long allocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "domestic_stock", precision = 5, scale = 2)
    private BigDecimal domesticStock;

    @Column(name = "overseas_stock", precision = 5, scale = 2)
    private BigDecimal overseasStock;

    @Column(name = "domestic_bond", precision = 5, scale = 2)
    private BigDecimal domesticBond;

    @Column(name = "overseas_bond", precision = 5, scale = 2)
    private BigDecimal overseasBond;

    @Column(name = "fund_asset", precision = 5, scale = 2)
    private BigDecimal fundAsset;

    @Column(name = "liquidity", precision = 5, scale = 2)
    private BigDecimal liquidity;

    @Column(name = "updated_date")
    private LocalDate updatedDate;
}
