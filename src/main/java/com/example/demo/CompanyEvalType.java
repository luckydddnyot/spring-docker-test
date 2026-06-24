package com.example.demo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회사별로 어떤 평가유형을 쓰는지(on/off). 슈퍼어드민이 토글. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CompanyEvalType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;
    private String evalType;   // HR / PROJECT
    private Boolean enabled;
}
