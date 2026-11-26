package com.yash.TicketBooking.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AcquireLock {
    private final RedissonClient redissonClient;

    public AcquireLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public Boolean takeLock(String key) throws InterruptedException {
        RLock lock = redissonClient.getLock(key);
        return lock.tryLock(10,300, TimeUnit.SECONDS);
    }

    public RLock checkOwner(String key){
        RLock lock = redissonClient.getLock(key);
        return lock;
    }
}
 // acquire lock
// then lcik on oay 1st with pending transaction row is create with unique transactionid
// then with mock satus through kafka status is send if got it as success booked ekse failed if got late mmssg
// then it is check in db if present book else not
// and if no status is come req goes to payemt gateway for status that i not created but if its is pending
// more than 10 min then schdeular will expire it.