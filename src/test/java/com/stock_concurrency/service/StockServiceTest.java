package com.stock_concurrency.service;

import com.stock_concurrency.domain.Stock;
import com.stock_concurrency.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before(){
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }


    @Test
    void 재고감소() {
        stockService.decrease(1L,1L);

        // 100 - 1 => 99
        Stock stock = stockRepository.findById(1L).orElseThrow();

        Assertions.assertEquals(99,stock.getQuantity());
    }


    // synchronized - 한개의 스레드만 접근이 가능
    @Test
    public void 동시에_100개의_요청이_들어온다() throws InterruptedException {
        int threadCount = 100;

        // 멀티쓰레드 - (비동기
        // 병렬 작업 시 여러 개의 작업을 효율적으로 처리하기 위해 제공되는 JAVA 라이브러리
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 멀티스레드환경에서,  특정쓰레드 작업을 대기하게끔해주는 동기화 도구.
        CountDownLatch latch = new CountDownLatch(threadCount);

        //100개의 요청
        for(int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L,1L);

                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 작업끝날때까지 대기..

        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 0개를 기대함
        // 그러나 90
        // 내 생각 : 각 쓰레드마다 조회할때 순차적으로 하는 것이 아니기 때문에
        // 99 , 98, 97 순으로 조회 되는 것이 ㅏ닌 99, 100, 99 등 조회가 된후 -1 하기 때문에
        // "레이스 컨디션 " -  자원을 공유할 때 발생하는 경쟁 상태
        assertEquals(0,stock.getQuantity());

    }

}