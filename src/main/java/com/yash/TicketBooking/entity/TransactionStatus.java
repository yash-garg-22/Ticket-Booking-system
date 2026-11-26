package com.yash.TicketBooking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class TransactionStatus {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String username;
    String seatId;
    String status;
    String transactionId;
    LocalDateTime localDateTime;
}
