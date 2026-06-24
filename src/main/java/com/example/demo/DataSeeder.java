package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 평가 항목 템플릿이 비어 있으면 기본 항목을 한 번 심는다.
 * (하드코딩이 아니라 데이터로 관리 — 이후 화면/DB에서 자유롭게 추가·수정 가능)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final EvaluationItemTemplateRepository templateRepository;

    public DataSeeder(EvaluationItemTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void run(String... args) {
        if (templateRepository.count() > 0) {
            return; // 이미 있으면 건너뜀
        }

        // 인사 성과평가 항목
        String[] hr = {"업무역량", "협업", "책임감", "적극성", "전문성"};
        for (int i = 0; i < hr.length; i++) {
            templateRepository.save(new EvaluationItemTemplate("HR", hr[i], i + 1));
        }

        // 프로젝트 단위 평가 항목 (회계/컨설팅펌)
        String[] project = {"전문성/기여도", "협업/커뮤니케이션", "일정준수", "산출물품질", "리더십"};
        for (int i = 0; i < project.length; i++) {
            templateRepository.save(new EvaluationItemTemplate("PROJECT", project[i], i + 1));
        }
    }
}
