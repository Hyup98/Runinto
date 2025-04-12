package com.runinto;

import com.runinto.config.EventConfig;
import com.runinto.config.UserConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({UserConfig.class, EventConfig.class})
public class Backend {
    public static void main(String[] args) {
        SpringApplication.run(Backend.class, args);
    }
}