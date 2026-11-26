package com.yash.TicketBooking.repository;

import com.yash.TicketBooking.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransRepo extends JpaRepository<TransactionStatus,Long> {
    Optional<TransactionStatus> findByTransactionId(String transactionId);
    Optional<List<TransactionStatus>> findByStatusAndLocalDateTimeBefore(
            String status, LocalDateTime localDateTime);

    @Modifying
    @Query("""
        UPDATE TransactionStatus t
        SET t.status = 'EXPIRED'
        WHERE t.transactionId = :transactionId
        AND t.status = 'PENDING'
""")
    int expiry(@Param("transactionId") String transactionId);

    @Modifying
    @Query("""
        UPDATE TransactionStatus t
        SET t.status = 'SUCCESS'
        WHERE t.transactionId = :transactionId
        AND t.status = 'PENDING'
    """)
    int success(@Param("transactionId") String transactionId);

}
