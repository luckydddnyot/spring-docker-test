package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 평가 항목별 점수 1줄. (한 Evaluation에 여러 EvaluationScore)
 * 단순화를 위해 관계 매핑 대신 evaluationId(평가 PK)를 컬럼으로 보관.
 */
@Entity
public class EvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long evaluationId; // 어느 평가에 속하는지
    private String itemName;   // 평가 항목명 (템플릿에서 옴)
    private Integer score;     // 1~5

    @Column(length = 1000)
    private String comment;    // 항목별 코멘트

    public EvaluationScore() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Long evaluationId) { this.evaluationId = evaluationId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
