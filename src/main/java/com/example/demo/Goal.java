package com.example.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 업적(목표) 1개. 관리자가 사전 등록, 평가자는 달성도(score 1~5)만 매긴다. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long evaluateeId;
    private String title;
    private Integer weight;     // 가중치 %
    private Integer score;      // 1~5 (미평가 시 null)
    private Integer sortOrder;
}
