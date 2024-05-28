package com.hs.mallchat.common;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: CZF
 * @Create: 2024/5/27 - 17:06
 */

@SpringBootApplication(scanBasePackages = {"com.hs.mallchat"})
public class MallchatCustomApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallchatCustomApplication.class, args);
    }

}
