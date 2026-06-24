package com.example.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 피평가자. 회사·평가유형에 소속되며 한 명의 평가자가 평가한다. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Evaluatee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;
    private String evalType;       // HR / PROJECT
    private String name;
    private String dept;
    private String position;
    private String evaluatorName;  // 평가자
    private String status;         // NONE / DRAFT / DONE

    @Column(length = 2000)
    private String overallComment;
}
