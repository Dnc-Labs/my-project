package com.ecommerce.api.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ThreadDemo {

    ExecutorService pool = Executors.newFixedThreadPool(3);
    List<Future<String>> futures = new ArrayList<>();

    public void submit5Tasks() {
        for(int i = 0; i <= 4; i++) {
            int taskNumber = i;
            Future<String> future =  pool.submit(() -> {
                try {
                    Thread.sleep(2000);
                    System.out.println("Task " + taskNumber + " - " + Thread.currentThread().getName());
                    return "Kết quả task " + taskNumber;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            futures.add(future);
        }

        for (Future<String> future : futures) {
            String result = null;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            System.out.println(result);
        }
        pool.shutdown();
    }
}
