package com.yash.TicketBooking.kafka;

import com.yash.TicketBooking.DTO.TransactionData;
import com.yash.TicketBooking.entity.Movie;
import com.yash.TicketBooking.entity.TransactionStatus;
import com.yash.TicketBooking.repository.MovieRepo;
import com.yash.TicketBooking.repository.TransRepo;
import com.yash.TicketBooking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Consumer {

    Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final MovieRepo  movieRepo;
    private final TransRepo  transRepo;
    private final BookingService bookingService;

    public Consumer(MovieRepo movieRepo, TransRepo transRepo, BookingService bookingService) {
        this.movieRepo = movieRepo;
        this.transRepo = transRepo;
        this.bookingService = bookingService;
    }

    @KafkaListener(groupId = "movies",topics = "transaction_data")
    public void ConsumeData(TransactionData transactionData){
        System.out.println("welcome");
        String status = transactionData.getStatus();
        if(status.equals("Success")){
            // check in db again
            try{
                Optional<TransactionStatus> byTransaction = transRepo.findByTransactionId(transactionData.getTransactionId());
                if(byTransaction.isPresent()){
                    if(movieRepo.findBySeatid(byTransaction.get().getSeatId()).isPresent()){
                        Optional<Movie> bySeatId = movieRepo.findBySeatid(byTransaction.get().getSeatId());
                        if(bySeatId.get().getBooked() == true){
                            // refund code
                            TransactionStatus transactionStatus = byTransaction.get();
                            transactionStatus.setStatus("Refunded");
                            transRepo.save(transactionStatus);
                            logger.warn("Already Booked Refund initiated");
                            return;
                        }
                        else{
                            try{
                                bookingService.ConfirmBooking(bySeatId.get(),byTransaction.get(),transactionData);
                                return;
                            }
                        catch (Exception e){
                                logger.error("Booking failed : " + e.getMessage());
                            TransactionStatus transactionStatus = byTransaction.get();
                            transactionStatus.setStatus("Failed");
                            transRepo.save(transactionStatus);
                            logger.error("Refund initiated");
                        }
                        }
                    }
                }
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }



        if(status.equals("Failed")){
            // check in db again
            try{
                Optional<TransactionStatus> byTransaction = transRepo.findByTransactionId(transactionData.getTransactionId());
                if(byTransaction.isPresent()){
                    TransactionStatus transactionStatus = byTransaction.get();
                    transactionStatus.setStatus("Failed");
                    transRepo.save(transactionStatus);
                    logger.error("Transaction Failed Please try again");
                    return;
                }
                    }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        logger.warn("Money deduction will be refunded within 2-3 business days");
    }
}
