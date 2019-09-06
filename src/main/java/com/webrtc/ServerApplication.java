package com.webrtc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * ServerApplication
 * 联系方式  微信 Liu19940528 qq 2425358736
 *
 * @author 刘志强
 * @created Create Time: 2019/3/26
 */
@RestController
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

    @GetMapping("/")
    public void index(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.sendRedirect("/web/webBroadcast");
    }
}