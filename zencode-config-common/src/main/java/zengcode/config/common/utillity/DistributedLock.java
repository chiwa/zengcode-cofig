package zengcode.config.common.utillity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class DistributedLock {

    private final RedissonClient redissonClient;

    public <T> Optional<T> withDistributedLock(String lockName, long waitTime, long leaseTime, Callable<T> action) {
        RLock lock = redissonClient.getLock(lockName);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (isLocked) return Optional.ofNullable(action.call());
        } catch (Exception e) {
            log.error("Lock error", e);
        } finally {
            if (isLocked) lock.unlock();
        }
        return Optional.empty();
    }
}
