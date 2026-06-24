package com.example.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 역량 항목별 점수(피평가자별). itemName으로 템플릿과 매칭. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CompetencyScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long evaluateeId;
    private String itemName;
    private Integer score;   // 1~5 (미평가 시 null)
}
