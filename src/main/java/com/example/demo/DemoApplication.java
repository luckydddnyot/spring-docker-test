package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Cloud Run + DB(Supabase PostgreSQL) 연결 연습용 더미 앱.
 * - "/"     : 동작 확인용 인사
 * - "/db"   : DB에 방문 기록 한 줄 저장 후 누적 건수 반환 (DB 연결 성공 확인)
 */
@SpringBootApplication
@RestController
public class DemoApplication {

    private final VisitLogRepository visitLogRepository;

    public DemoApplication(VisitLogRepository visitLogRepository) {
        this.visitLogRepository = visitLogRepository;
    }

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

    @GetMapping("/db")
    public String db() {
        // 호출될 때마다 방문 기록 한 줄 저장 → DB 쓰기/읽기가 되는지 확인
        visitLogRepository.save(new VisitLog(LocalDateTime.now()));
        long count = visitLogRepository.count();
        return "DB 연결 성공! ✅  누적 방문 기록: " + count + "건";
    }
}
