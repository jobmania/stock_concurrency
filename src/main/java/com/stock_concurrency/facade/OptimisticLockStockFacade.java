package com.stock_concurrency.facade;

import com.stock_concurrency.domain.Stock;
import com.stock_concurrency.service.OptimisticLockStockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OptimisticLockStockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }


    public void decrease(Long id, Long quantity)  {
        while (true){
            try {
                optimisticLockStockService.decrease(id, quantity);
                break;
            }catch (Exception e){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
