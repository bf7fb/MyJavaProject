package com.hmdp;

import com.hmdp.utils.RedisWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private RedisWorker redisWorker;

    private ExecutorService ex = Executors.newFixedThreadPool(500);

    /**
     * 一个线程调用方法100次 每调用一次，就将这个任务提交300次 redis中id为3w
     * CountDownLatch保证最后线程同步结束，确保时间正确
     * 500个线程来执行方法共100次，500个线程执行方法后再提交300次 所以redis中id为3w
     * @throws InterruptedException
     */
        @Test
        void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () ->{
            for (int i = 0; i < 100; i++) {
                long id = redisWorker.nextId("order");
                System.out.println(id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            ex.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println(end - begin);
    }
}
