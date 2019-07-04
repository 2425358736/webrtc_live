package com.webrtc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * ServerApplication
 * 联系方式  微信 Liu19940528 qq 2425358736
 *
 * @author 刘志强
 * @created Create Time: 2019/3/26
 */
@SpringBootApplication
@EnableAutoConfiguration
public class ServerApplication implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        logger.info("服务器启动成功");
    }
}