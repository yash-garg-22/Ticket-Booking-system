package com.yash.TicketBooking.controller;

import com.yash.TicketBooking.DTO.Details;
import com.yash.TicketBooking.DTO.TransactionData;
import com.yash.TicketBooking.entity.Movie;
import com.yash.TicketBooking.entity.TransactionStatus;
import com.yash.TicketBooking.lock.AcquireLock;
import com.yash.TicketBooking.repository.MovieRepo;
import com.yash.TicketBooking.repository.TransRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class EntryPoints {

    private final AcquireLock lock;
    private final KafkaTemplate<String, TransactionData> kafkaTemplate;
    private final MovieRepo movieRepo;
    private final TransRepo transRepo;

    public EntryPoints(AcquireLock lock, KafkaTemplate<String, TransactionData> kafkaTemplate, MovieRepo movieRepo, TransRepo transRepo) {
        this.lock = lock;
        this.kafkaTemplate = kafkaTemplate;
        this.movieRepo = movieRepo;
        this.transRepo = transRepo;
    }

    @GetMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody Details details) throws InterruptedException, ExecutionException {
        Boolean value = lock.takeLock(details.getSeatNumber());

        if(!value) return ResponseEntity.status(HttpStatus.CONFLICT).body("Temporary booking closed for this seat number");
        // write code to check if booking is booked or not - pending
        // if no booking create a table with pending_payment - pending
        // then create a success payment

        // 1- check if seat is booked or not

        Optional<Movie> seatAvailable = movieRepo.findBySeatid("SeatN" + details.getSeatNumber());

        if(seatAvailable.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incorrect seat Number");
        }

        if(seatAvailable.get().getBooked() == true){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Booking is already booked");
        }
        else{
            TransactionStatus transactionStatus = new TransactionStatus();
            transactionStatus.setStatus("Pending");
            transactionStatus.setUsername(details.getUsername());
            transactionStatus.setSeatId("SeatN" + details.getSeatNumber());
            transactionStatus.setTransactionId(UUID.randomUUID().toString());
            transactionStatus.setLocalDateTime(LocalDateTime.now());
            transRepo.save(transactionStatus);

            if(details.getMockTransactionStatus().equals("SUCCESS")){
                TransactionData transactionData = new TransactionData();
                transactionData.setTransactionId(transactionStatus.getTransactionId());
                transactionData.setStatus("Success");
                CompletableFuture<SendResult<String, TransactionData>> send = kafkaTemplate.send("transaction_data", "key1", transactionData);
                CompletableFuture<ResponseEntity<String>> seatBooked = send.thenApply(sendResult -> ResponseEntity.status(HttpStatus.ACCEPTED).body("Seat Booked"))
                        .exceptionally(e -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
                return seatBooked.get();
            }

            if(details.getMockTransactionStatus().equals("FAILED")){
                TransactionData transactionData = new TransactionData();
                transactionData.setTransactionId(transactionStatus.getTransactionId());
                transactionData.setStatus("Failed");
                CompletableFuture<SendResult<String, TransactionData>> send = kafkaTemplate.send("transaction_data", "key1", transactionData);
                CompletableFuture<ResponseEntity<String>> seatBookingFailed = send.thenApply(sendResult -> {
                            ResponseEntity<String> body = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Seat Booking Failed");
                            return body;
                        })
                        .exceptionally(e -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));

                return seatBookingFailed.get();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking status will be shared shortly And if booking is not confirmed and money deducted will be refunded with in 2-3 business days");
    }

    @GetMapping("/createSeats")
    public ResponseEntity<String> createSeats(){
        try{
            for(int i=1; i<=50; i++){
                Movie movie = new Movie();
                movie.setSeatid("SeatN" + i);
                movie.setBookedBy(null);
                movie.setBooked(false);
                movieRepo.save(movie);
            }
        }
        catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("Seats created");
    }

}
