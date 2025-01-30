package com.stock_concurrency.facade;

import com.stock_concurrency.repository.RedisLockRepository;
import com.stock_concurrency.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        // 레디스 부하 감소를 위해 100대기
        // 스핀락 방식이기때문에 ( 레디스 부하를 줄여줘야된다)
        while (!redisLockRepository.lock(id)){
            Thread.sleep(100);
        }

        try {
            stockService.decrease(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }

    }
}
