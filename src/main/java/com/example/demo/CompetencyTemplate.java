package com.example.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 평가유형별 역량 항목 정의(하드코딩 대신 데이터로 관리). */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CompetencyTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String evalType;     // HR / PROJECT
    private String itemName;
    @Column(length = 500)
    private String description;
    private Integer sortOrder;
}
