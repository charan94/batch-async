package com.skanl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableMongoAuditing
@EnableScheduling
@EnableAsync
public class BatchAsyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchAsyncApplication.class, args);
    }


}
