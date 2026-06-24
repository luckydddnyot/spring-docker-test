package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * DB 연결 실습용 엔티티.
 * 호출될 때마다 방문 기록 한 줄을 DB에 저장한다. (테이블은 ddl-auto=update 로 자동 생성)
 */
@Entity
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime visitedAt;

    public VisitLog() {
    }

    public VisitLog(LocalDateTime visitedAt) {
        this.visitedAt = visitedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getVisitedAt() {
        return visitedAt;
    }
}
