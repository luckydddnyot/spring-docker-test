package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** 평가 도메인 리포지토리 모음 (한 파일에 인터페이스 여러 개). */
class Repositories { }

interface CompanyRepository extends JpaRepository<Company, Long> {
}

interface CompanyEvalTypeRepository extends JpaRepository<CompanyEvalType, Long> {
    List<CompanyEvalType> findByCompanyId(Long companyId);
    Optional<CompanyEvalType> findByCompanyIdAndEvalType(Long companyId, String evalType);
}

interface EvaluateeRepository extends JpaRepository<Evaluatee, Long> {
    List<Evaluatee> findByCompanyId(Long companyId);
}

interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByEvaluateeIdOrderBySortOrderAsc(Long evaluateeId);
}

interface CompetencyScoreRepository extends JpaRepository<CompetencyScore, Long> {
    List<CompetencyScore> findByEvaluateeId(Long evaluateeId);
    Optional<CompetencyScore> findByEvaluateeIdAndItemName(Long evaluateeId, String itemName);
}

interface CompetencyTemplateRepository extends JpaRepository<CompetencyTemplate, Long> {
    List<CompetencyTemplate> findByEvalTypeOrderBySortOrderAsc(String evalType);
}
