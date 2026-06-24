package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DB 연결 실습용 리포지토리. JpaRepository만 상속하면 기본 CRUD가 자동 제공됨.
 */
public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {
}
