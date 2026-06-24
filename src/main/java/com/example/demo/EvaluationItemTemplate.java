package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 평가 항목 "템플릿". 평가 항목을 코드에 하드코딩하지 않고 이 테이블에서 읽는다.
 * (멀티테넌트/회사별로 항목이 달라질 수 있게 하는 기반 — 하드코딩 제거 원칙 적용)
 */
@Entity
public class EvaluationItemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;       // HR / PROJECT
    private String itemName;   // 항목명
    private Integer sortOrder; // 표시 순서

    public EvaluationItemTemplate() {
    }

    public EvaluationItemTemplate(String type, String itemName, Integer sortOrder) {
        this.type = type;
        this.itemName = itemName;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
