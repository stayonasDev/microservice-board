package com.board.article.data;

import com.board.article.entity.Article;
import com.board.common.snowflake.Snowflake;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 스스로 학습 정리
 * 1. EntityManager로 JPA persist 사용이유는 Spring Data JPA의 save는 persist, merge가 있다.
 * MSA 환경에서 UUID, AUTO_INCREMENT KEY를 사용하지 않고 현재 직접 PK를 넣었기 때문에 merge를 사용해
 * 직접 DB를 조회하는 비용이 추가로 들기 때문에 직접 persist 사용
 * 2.Transaction Template 사용 이유는 JPA는 Transaction이 없으면 예외를 발생 시키기 때문에
 * 직접 코드로 트랜잭션을 제어한다. @Transaction 등 사용하지 않는 이유는 현재 같은 클래스로 Self-Invocation이
 * 발생하기 때문이다. (트랜잭션은 AOP임)
 * 3.latch는 메인 스레드가 for문이 끝나면 종료하기 때문에 latch.await()으로 스레드 작업이 다 끝난 시점
 * latch count가 0일 때까지 메인 스레드를 대기 시킨다.
 * 4.shutdownNow()가 큐에 있는 스레드까지 종료, shutdown()은 새로운 작업을 받지 않는 것
 */

@SpringBootTest
@Slf4j
public class DataInitializer {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    static final int BULK_INSERT_SIZE = 2000;
    static final int EXECUTE_COUNT = 6000;

    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < EXECUTE_COUNT; i++) {
            executorService.submit(() -> {
                try{
                    insert();
                }catch(Exception e){
                    log.error("insert 실패",e);
                }finally {
                    latch.countDown(); //실패해도 반드시 카운트를 내린다
                }
                log.info("latch.getCount() = {}", latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void insert(){
        transactionTemplate.executeWithoutResult(status -> {
            for(int i = 0; i < BULK_INSERT_SIZE; i++){
                Article article = Article.create(
                        snowflake.nextId(),
                        "title" + i,
                        "content" + i,
                        1L,
                        1L
                );
                em.persist(article);
            }
        });
    }

}
