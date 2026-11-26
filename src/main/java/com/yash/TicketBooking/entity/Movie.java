package com.yash.TicketBooking.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Ramayna")
@Data
public class Movie {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String seatid;
    Boolean Booked;
    String BookedBy;
    @Version
    Long version;
}
