package com.twilight.campus;

//import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.twilight.campus.mapper")
public class CampusServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusServiceApplication.class, args);
    }
}
