package com.ecommerce.api.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Bài thực hành: Chứng minh Race Condition và fix bằng synchronized
 *
 * Yêu cầu:
 * 1. Biến stock bắt đầu = 100
 * 2. Tạo thread pool với 10 threads
 * 3. Submit 100 task, mỗi task gọi decreaseStock(1)
 * 4. Kết quả ĐÚNG: stock = 0
 * 5. Kết quả SAI (race condition): stock > 0
 *
 * Bước 1: Triển khai decreaseStock() KHÔNG có synchronized → chạy thử, thấy kết quả sai
 * Bước 2: Thêm synchronized → chạy lại, thấy kết quả đúng
 */
public class RaceConditionDemo {
    private int stock = 100;

    /**
     * TODO: Triển khai method này
     * - Kiểm tra stock >= quantity
     * - Nếu đủ thì trừ stock
     * - In ra thread name + stock hiện tại (dùng Thread.currentThread().getName())
     *
     * Lần 1: Viết bình thường (không synchronized) → chạy main, quan sát kết quả
     * Lần 2: Thêm synchronized → chạy lại, so sánh
     */
    synchronized
    public void decreaseStock(int quantity) {
        if(this.stock >= quantity) {
            this.stock -= quantity;
        }
        System.out.println("thread name :" + Thread.currentThread().getName() + ", Current Stock " + this.stock);
    }

    public int getStock() {
        return stock;
    }

    public static void main(String[] args) throws InterruptedException {
        RaceConditionDemo demo = new RaceConditionDemo();
        ExecutorService pool = Executors.newFixedThreadPool(10);

        for(int i = 0 ; i < 100 ; i++) {
            pool.submit(() -> {
                demo.decreaseStock(1);
            });
        }


        // TODO: Gọi pool.shutdown() và pool.awaitTermination() để chờ tất cả task hoàn thành
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        // In kết quả cuối cùng
        System.out.println("=== Kết quả ===");
        System.out.println("Stock còn lại: " + demo.getStock());
        System.out.println("Kết quả đúng phải là: 0");
    }
}