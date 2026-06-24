package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 평가 REST API.
 *  GET  /api/items?type=HR        평가 항목 템플릿 조회
 *  POST /api/evaluations          평가 저장 (항목별 점수 포함)
 *  GET  /api/evaluations          평가 목록 (평균점수 포함)
 *  GET  /api/evaluations/{id}     평가 상세 (항목별 점수)
 */
@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationScoreRepository scoreRepository;
    private final EvaluationItemTemplateRepository templateRepository;

    public EvaluationController(EvaluationRepository evaluationRepository,
                               EvaluationScoreRepository scoreRepository,
                               EvaluationItemTemplateRepository templateRepository) {
        this.evaluationRepository = evaluationRepository;
        this.scoreRepository = scoreRepository;
        this.templateRepository = templateRepository;
    }

    @GetMapping("/items")
    public List<EvaluationItemTemplate> items(@RequestParam(defaultValue = "HR") String type) {
        return templateRepository.findByTypeOrderBySortOrderAsc(type);
    }

    @PostMapping("/evaluations")
    public Map<String, Object> create(@RequestBody EvaluationRequest req) {
        Evaluation e = new Evaluation();
        e.setType(req.type != null ? req.type : "HR");
        e.setEvaluatorName(req.evaluatorName);
        e.setEvaluateeName(req.evaluateeName);
        e.setProjectName(req.projectName);
        e.setPeriod(req.period);
        e.setCreatedAt(LocalDateTime.now());
        Evaluation saved = evaluationRepository.save(e);

        if (req.scores != null) {
            for (EvaluationRequest.ScoreInput s : req.scores) {
                EvaluationScore sc = new EvaluationScore();
                sc.setEvaluationId(saved.getId());
                sc.setItemName(s.itemName);
                sc.setScore(s.score);
                sc.setComment(s.comment);
                scoreRepository.save(sc);
            }
        }

        Map<String, Object> r = new HashMap<>();
        r.put("id", saved.getId());
        r.put("message", "평가가 저장되었습니다.");
        return r;
    }

    @GetMapping("/evaluations")
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Evaluation e : evaluationRepository.findAll()) {
            List<EvaluationScore> scores = scoreRepository.findByEvaluationId(e.getId());
            double avg = scores.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToInt(EvaluationScore::getScore)
                    .average().orElse(0);

            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("type", e.getType());
            m.put("evaluateeName", e.getEvaluateeName());
            m.put("evaluatorName", e.getEvaluatorName());
            m.put("projectName", e.getProjectName());
            m.put("period", e.getPeriod());
            m.put("avgScore", Math.round(avg * 10) / 10.0);
            m.put("createdAt", e.getCreatedAt());
            result.add(m);
        }
        // 최신순 정렬
        result.sort((a, b) -> Long.compare((Long) b.get("id"), (Long) a.get("id")));
        return result;
    }

    @GetMapping("/evaluations/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> m = new HashMap<>();
        m.put("evaluation", evaluationRepository.findById(id).orElse(null));
        m.put("scores", scoreRepository.findByEvaluationId(id));
        return m;
    }

    /** 평가 저장 요청 본문 */
    public static class EvaluationRequest {
        public String type;
        public String evaluatorName;
        public String evaluateeName;
        public String projectName;
        public String period;
        public List<ScoreInput> scores;

        public static class ScoreInput {
            public String itemName;
            public Integer score;
            public String comment;
        }
    }
}
