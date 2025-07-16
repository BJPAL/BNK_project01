package com.example.fund.fund.repository;

import com.example.fund.fund.dto.FundReturnDTO;
import com.example.fund.fund.entity.FundDailyPrice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundDailyPriceRepository extends JpaRepository<FundDailyPrice, Long> {

    /** 특정 펀드 상품의 1개월, 3개월, 6개월, 12개울 수익률 계산 */
    @Query(value = """
        WITH latest_price AS (
            SELECT base_date, base_price
            FROM fund_daily_price
            WHERE fund_id = :fundId
            ORDER BY base_date DESC
            LIMIT 1
        ),
        month_1_price AS (
            SELECT base_price
            FROM fund_daily_price
            WHERE fund_id = :fundId
              AND base_date <= (SELECT base_date FROM latest_price) - INTERVAL 1 MONTH
            ORDER BY base_date DESC
            LIMIT 1
        ),
        month_3_price AS (
            SELECT base_price
            FROM fund_daily_price
            WHERE fund_id = :fundId
              AND base_date <= (SELECT base_date FROM latest_price) - INTERVAL 3 MONTH
            ORDER BY base_date DESC
            LIMIT 1
        ),
        month_6_price AS (
            SELECT base_price
            FROM fund_daily_price
            WHERE fund_id = :fundId
              AND base_date <= (SELECT base_date FROM latest_price) - INTERVAL 6 MONTH
            ORDER BY base_date DESC
            LIMIT 1
        ),
        month_12_price AS (
            SELECT base_price
            FROM fund_daily_price
            WHERE fund_id = :fundId
              AND base_date <= (SELECT base_date FROM latest_price) - INTERVAL 12 MONTH
            ORDER BY base_date DESC
            LIMIT 1
        )
        SELECT
            ROUND((latest.base_price / m1.base_price - 1) * 100, 2) AS month1Return,
            ROUND((latest.base_price / m3.base_price - 1) * 100, 2) AS month3Return,
            ROUND((latest.base_price / m6.base_price - 1) * 100, 2) AS month6Return,
            ROUND((latest.base_price / m12.base_price - 1) * 100, 2) AS month12Return
        FROM latest_price latest
        JOIN month_1_price m1
        JOIN month_3_price m3
        JOIN month_6_price m6
        JOIN month_12_price m12
        """, nativeQuery = true)
    FundReturnDTO getFundReturnByFundId(@Param("fundId") Long fundId);


    /** 최신 기준가 데이터 조회 */
    Optional<FundDailyPrice> findTopByFundFundIdOrderByBaseDateDesc(Long fundId);
}
