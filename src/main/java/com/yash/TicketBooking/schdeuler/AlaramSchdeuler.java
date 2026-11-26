package com.yash.TicketBooking.schdeuler;

import com.yash.TicketBooking.entity.TransactionStatus;
import com.yash.TicketBooking.lock.AcquireLock;
import com.yash.TicketBooking.repository.TransRepo;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class AlaramSchdeuler {

    private final TransRepo transRepo;
    private final AcquireLock acquireLock;
    Logger logger = LoggerFactory.getLogger(AlaramSchdeuler.class);
    public AlaramSchdeuler(TransRepo transRepo, AcquireLock acquireLock) {
        this.transRepo = transRepo;
        this.acquireLock = acquireLock;
    }

    @Scheduled(fixedDelay = 5000)
    public void run(){
        Optional<List<TransactionStatus>> pending = transRepo.findByStatusAndLocalDateTimeBefore("Pending", LocalDateTime.now().minusMinutes(1));
        if(pending.isPresent()){
            List<TransactionStatus> pendingTransactions = pending.get();
            for(TransactionStatus transactionStatus : pendingTransactions){
                transactionStatus.setStatus("Expired");
                int expiry = transRepo.expiry(transactionStatus.getTransactionId());
                if(expiry > 0) logger.info("transaction expired for : " + transactionStatus.getUsername() + " for transactionId: " + transactionStatus.getTransactionId());
            }
        }
    }
}
