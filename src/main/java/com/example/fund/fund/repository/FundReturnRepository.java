package com.example.fund.fund.repository;

import com.example.fund.fund.entity.FundReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FundReturnRepository extends JpaRepository<FundReturn, Long> {
    /** 펀드 ID로 수익률 조회 */
    FundReturn findByFund_FundId(Long fundId);

    /** 펀드 ID로 수익률 조회 (Optional) */
    Optional<FundReturn> findOptionalByFund_FundId(Long fundId);

    /**
     * 여러 fundId에 대한 FundReturn을 배치로 조회 (N+1 문제 해결)
     * @param fundIds 조회할 펀드 ID 리스트
     * @return 해당 펀드들의 수익률 정보 리스트
     */
    @Query("SELECT fr FROM FundReturn fr WHERE fr.fund.fundId IN :fundIds")
    List<FundReturn> findByFund_FundIdIn(@Param("fundIds") List<Long> fundIds);

    /**
     * 각 fund_id에 대해 가장 최신(return_id가 가장 큰) FundReturn을 가져온다.
     * nativeQuery로 서브쿼리 조인하여 최신 것만 필터링.
     */
    @Query(value = """
        SELECT fr.*
        FROM fund_return fr
        INNER JOIN (
            SELECT fund_id, MAX(return_id) AS max_return_id
            FROM fund_return
            WHERE fund_id IN (:fundIds)
            GROUP BY fund_id
        ) latest ON fr.fund_id = latest.fund_id AND fr.return_id = latest.max_return_id
        """, nativeQuery = true)
    List<FundReturn> findLatestByFundIds(@Param("fundIds") List<Long> fundIds);
}
