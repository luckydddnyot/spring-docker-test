package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * 평가 1건. type으로 두 케이스를 구분한다.
 *  - HR      : 인사 성과평가 (피평가자 ← 평가자)
 *  - PROJECT : 프로젝트 단위 평가 (회계/컨설팅펌) — projectName 사용
 */
@Entity
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;          // HR / PROJECT
    private String evaluatorName; // 평가자
    private String evaluateeName; // 피평가자
    private String projectName;   // PROJECT 케이스용 (HR이면 null)
    private String period;        // 평가기간 (예: 2026-H1)
    private LocalDateTime createdAt;

    public Evaluation() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }
    public String getEvaluateeName() { return evaluateeName; }
    public void setEvaluateeName(String evaluateeName) { this.evaluateeName = evaluateeName; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
