package com.hs.mallchat.common;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @Author: CZF
 * @Create: 2024/5/27 - 17:06
 */

@SpringBootApplication(scanBasePackages = {"com.hs.mallchat"})
@MapperScan({"com.hs.mallchat.common.**.mapper"})
@ServletComponentScan
public class MallchatCustomApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallchatCustomApplication.class, args);
    }

}
