package com.yash.TicketBooking.repository;

import com.yash.TicketBooking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepo extends JpaRepository<Movie,Long> {
    Optional<Movie> findBySeatid(String seatid);
}
