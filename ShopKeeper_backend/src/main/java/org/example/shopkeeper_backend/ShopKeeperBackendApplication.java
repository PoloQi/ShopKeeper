package org.example.shopkeeper_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.shopkeeper_backend.mapper")
public class ShopKeeperBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopKeeperBackendApplication.class, args);
    }

}
