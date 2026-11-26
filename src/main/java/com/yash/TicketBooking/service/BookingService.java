package com.yash.TicketBooking.service;

import com.yash.TicketBooking.DTO.TransactionData;
import com.yash.TicketBooking.entity.Movie;
import com.yash.TicketBooking.entity.TransactionStatus;
import com.yash.TicketBooking.repository.MovieRepo;
import com.yash.TicketBooking.repository.TransRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final MovieRepo movieRepo;
    private final TransRepo transRepo;

    Logger logger = LoggerFactory.getLogger(BookingService.class);

    public BookingService(MovieRepo movieRepo, TransRepo transRepo) {
        this.movieRepo = movieRepo;
        this.transRepo = transRepo;
    }

    @Transactional
    public void ConfirmBooking(Movie movie, TransactionStatus transactionStatus,TransactionData transactionData){
        movie.setBooked(true);
        movie.setBookedBy(transactionData.getTransactionId());
        int success = transRepo.success(transactionData.getTransactionId());
        if(success==1){
            movieRepo.save(movie);
            logger.info("Booked successfully");
        }
        else{
            logger.error("Booking failed if money deducted will be refunded");
        }
    }
}
