package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationItemTemplateRepository extends JpaRepository<EvaluationItemTemplate, Long> {
    List<EvaluationItemTemplate> findByTypeOrderBySortOrderAsc(String type);
}
