package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cloud Run 연습용 더미 앱.
 * - HTTP 엔드포인트 하나만 가진 최소 Spring Boot 앱.
 * - Cloud Run은 컨테이너가 PORT 환경변수 포트로 listen 하길 기대한다 (application.properties 참고).
 */
@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        return "Hello from Cloud Run! 🚀  (Spring Boot 컨테이너가 정상 동작 중)";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
