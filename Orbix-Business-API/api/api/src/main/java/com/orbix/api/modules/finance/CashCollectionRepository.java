package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashCollectionRepository extends JpaRepository<CashCollection, Long> {
	
	
	@Query(value = "SELECT " +
            "SUM(c.amount) AS amount, " +
            "c.reason AS reason, " +
            "c.payment_type AS paymentType, " +
            "u.nickname AS cashierName " +
            "FROM cash_collections c " +
            "JOIN users u ON u.id = c.collected_by_user_id " +
            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate " +
            "AND u.nickname = :nickname " +
            "GROUP BY c.reason, c.payment_type", 
    nativeQuery = true)
	List<ICashCollection> findTotalCollectionByDateRangeAndCashier(
     @Param("startDate") LocalDateTime startDate, 
     @Param("endDate") LocalDateTime endDate, 
     @Param("nickname") String nickname);


@Query(value = "SELECT " +
        "SUM(c.amount) AS amount, " +
        "c.reason AS reason, " +
        "c.payment_type AS paymentType, " +
        "u.nickname AS cashierName " +
        "FROM cash_collections c " +
        "JOIN users u ON u.id = c.collected_by_user_id " +
        "WHERE c.collection_date_time BETWEEN :startDate AND :endDate " +
        "GROUP BY c.reason, c.payment_type", 
nativeQuery = true)
List<ICashCollection> findTotalCollectionByDateRange(
 @Param("startDate") LocalDateTime startDate, 
 @Param("endDate") LocalDateTime endDate);

}
