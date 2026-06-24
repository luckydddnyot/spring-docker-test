package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 평가 API (회사 단위).
 *  GET  /api/companies                      회사 목록(+사용중 평가유형)
 *  GET  /api/companies/{cid}/targets        피평가자(평가대상) 목록
 *  GET  /api/targets/{id}                   평가 상세(목표/역량/점수)
 *  PUT  /api/targets/{id}                   평가 저장(임시저장/제출)
 *  GET  /api/companies/{cid}/summary        관리자 대시보드 집계
 */
@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final CompanyRepository companyRepo;
    private final CompanyEvalTypeRepository typeRepo;
    private final EvaluateeRepository evaluateeRepo;
    private final GoalRepository goalRepo;
    private final CompetencyScoreRepository scoreRepo;
    private final CompetencyTemplateRepository templateRepo;

    public EvaluationController(CompanyRepository companyRepo, CompanyEvalTypeRepository typeRepo,
                               EvaluateeRepository evaluateeRepo, GoalRepository goalRepo,
                               CompetencyScoreRepository scoreRepo, CompetencyTemplateRepository templateRepo) {
        this.companyRepo = companyRepo;
        this.typeRepo = typeRepo;
        this.evaluateeRepo = evaluateeRepo;
        this.goalRepo = goalRepo;
        this.scoreRepo = scoreRepo;
        this.templateRepo = templateRepo;
    }

    @GetMapping("/companies")
    public List<Map<String, Object>> companies() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Company c : companyRepo.findAll()) {
            List<String> enabled = typeRepo.findByCompanyId(c.getId()).stream()
                    .filter(t -> Boolean.TRUE.equals(t.getEnabled()))
                    .map(CompanyEvalType::getEvalType)
                    .collect(Collectors.toList());
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("enabledTypes", enabled);
            out.add(m);
        }
        return out;
    }

    @PostMapping("/companies/{cid}/evaluatees")
    public Map<String, Object> createEvaluatee(@PathVariable Long cid, @RequestBody CreateRequest req) {
        Evaluatee e = new Evaluatee();
        e.setCompanyId(cid);
        e.setEvalType(req.evalType);
        e.setName(req.name);
        e.setDept(req.dept);
        e.setPosition(req.position);
        e.setEvaluatorName(req.evaluatorName);
        e.setStatus("NONE");
        evaluateeRepo.save(e);

        int weightSum = 0;
        if (req.goals != null) {
            int order = 1;
            for (CreateRequest.GoalDef g : req.goals) {
                Goal goal = new Goal();
                goal.setEvaluateeId(e.getId());
                goal.setTitle(g.title);
                goal.setWeight(g.weight);
                goal.setSortOrder(order++);
                goalRepo.save(goal);
                if (g.weight != null) weightSum += g.weight;
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("id", e.getId());
        if (weightSum != 100) {
            out.put("warning", "가중치 합계가 " + weightSum + "% 입니다. (권장: 100%)");
        }
        return out;
    }

    @GetMapping("/companies/{cid}/targets")
    public List<Map<String, Object>> targets(@PathVariable Long cid,
                                             @RequestParam(required = false) String evaluator) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Evaluatee e : evaluateeRepo.findByCompanyId(cid)) {
            if (evaluator != null && !evaluator.isEmpty() && !evaluator.equals(e.getEvaluatorName())) continue;
            List<Goal> goals = goalRepo.findByEvaluateeIdOrderBySortOrderAsc(e.getId());
            List<CompetencyScore> scores = scoreRepo.findByEvaluateeId(e.getId());
            int compTotal = templateRepo.findByEvalTypeOrderBySortOrderAsc(e.getEvalType()).size();
            long filled = goals.stream().filter(g -> g.getScore() != null).count()
                    + scores.stream().filter(s -> s.getScore() != null).count();
            int total = goals.size() + compTotal;

            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("name", e.getName());
            m.put("dept", e.getDept());
            m.put("position", e.getPosition());
            m.put("evaluatorName", e.getEvaluatorName());
            m.put("evalType", e.getEvalType());
            m.put("status", e.getStatus());
            m.put("filled", filled);
            m.put("total", total);
            out.add(m);
        }
        out.sort(Comparator.comparing(a -> (String) a.get("name")));
        return out;
    }

    @GetMapping("/targets/{id}")
    public Map<String, Object> target(@PathVariable Long id) {
        Evaluatee e = evaluateeRepo.findById(id).orElseThrow();
        List<Goal> goals = goalRepo.findByEvaluateeIdOrderBySortOrderAsc(id);
        List<CompetencyScore> scores = scoreRepo.findByEvaluateeId(id);
        Map<String, Integer> scoreByName = new HashMap<>();
        for (CompetencyScore s : scores) scoreByName.put(s.getItemName(), s.getScore());

        List<Map<String, Object>> comps = new ArrayList<>();
        for (CompetencyTemplate t : templateRepo.findByEvalTypeOrderBySortOrderAsc(e.getEvalType())) {
            Map<String, Object> cm = new HashMap<>();
            cm.put("itemName", t.getItemName());
            cm.put("description", t.getDescription());
            cm.put("score", scoreByName.get(t.getItemName()));
            comps.add(cm);
        }

        Double perf = Scoring.perfScore(goals);
        Double comp = Scoring.compScore(scores);
        Double total = Scoring.totalScore(perf, comp);

        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("companyId", e.getCompanyId());
        m.put("evalType", e.getEvalType());
        m.put("name", e.getName());
        m.put("dept", e.getDept());
        m.put("position", e.getPosition());
        m.put("evaluatorName", e.getEvaluatorName());
        m.put("status", e.getStatus());
        m.put("overallComment", e.getOverallComment());
        m.put("goals", goals);
        m.put("competencies", comps);
        m.put("perfScore", Scoring.round2(perf));
        m.put("compScore", Scoring.round2(comp));
        m.put("totalScore", Scoring.round2(total));
        m.put("grade", Scoring.grade(total));
        return m;
    }

    @PutMapping("/targets/{id}")
    public Map<String, Object> save(@PathVariable Long id, @RequestBody SaveRequest req) {
        Evaluatee e = evaluateeRepo.findById(id).orElseThrow();

        // 업적(목표) 점수 갱신
        if (req.goals != null) {
            Map<Long, Integer> goalScore = new HashMap<>();
            for (SaveRequest.GoalScore g : req.goals) goalScore.put(g.id, g.score);
            for (Goal g : goalRepo.findByEvaluateeIdOrderBySortOrderAsc(id)) {
                if (goalScore.containsKey(g.getId())) {
                    g.setScore(goalScore.get(g.getId()));
                    goalRepo.save(g);
                }
            }
        }

        // 역량 점수 upsert
        if (req.competencies != null) {
            for (SaveRequest.CompScore c : req.competencies) {
                CompetencyScore cs = scoreRepo.findByEvaluateeIdAndItemName(id, c.itemName)
                        .orElseGet(() -> {
                            CompetencyScore n = new CompetencyScore();
                            n.setEvaluateeId(id);
                            n.setItemName(c.itemName);
                            return n;
                        });
                cs.setScore(c.score);
                scoreRepo.save(cs);
            }
        }

        e.setOverallComment(req.overallComment);
        if (req.status != null) e.setStatus(req.status);
        evaluateeRepo.save(e);

        return target(id);
    }

    @GetMapping("/companies/{cid}/summary")
    public Map<String, Object> summary(@PathVariable Long cid) {
        List<Evaluatee> list = evaluateeRepo.findByCompanyId(cid);
        int total = list.size();
        int done = 0, draft = 0, none = 0;
        Map<String, Integer> dist = new LinkedHashMap<>();
        for (String g : new String[]{"S", "A", "B", "C", "D"}) dist.put(g, 0);

        Map<String, int[]> byEval = new LinkedHashMap<>();        // evaluator -> [total, done]
        Map<String, List<String>> pending = new LinkedHashMap<>(); // evaluator -> pending names
        List<Map<String, Object>> results = new ArrayList<>();

        for (Evaluatee e : list) {
            String st = e.getStatus() == null ? "NONE" : e.getStatus();
            if ("DONE".equals(st)) done++; else if ("DRAFT".equals(st)) draft++; else none++;

            byEval.computeIfAbsent(e.getEvaluatorName(), k -> new int[2])[0]++;
            pending.computeIfAbsent(e.getEvaluatorName(), k -> new ArrayList<>());
            if ("DONE".equals(st)) byEval.get(e.getEvaluatorName())[1]++;
            else pending.get(e.getEvaluatorName()).add(e.getName());

            if ("DONE".equals(st)) {
                List<Goal> goals = goalRepo.findByEvaluateeIdOrderBySortOrderAsc(e.getId());
                List<CompetencyScore> scores = scoreRepo.findByEvaluateeId(e.getId());
                Double perf = Scoring.perfScore(goals);
                Double comp = Scoring.compScore(scores);
                Double tot = Scoring.totalScore(perf, comp);
                String grade = Scoring.grade(tot);
                dist.put(grade, dist.getOrDefault(grade, 0) + 1);

                Map<String, Object> r = new HashMap<>();
                r.put("name", e.getName());
                r.put("dept", e.getDept());
                r.put("evaluatorName", e.getEvaluatorName());
                r.put("perfScore", Scoring.round2(perf));
                r.put("compScore", Scoring.round2(comp));
                r.put("totalScore", Scoring.round2(tot));
                r.put("grade", grade);
                results.add(r);
            }
        }

        List<Map<String, Object>> evaluators = new ArrayList<>();
        for (Map.Entry<String, int[]> en : byEval.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("evaluator", en.getKey());
            m.put("total", en.getValue()[0]);
            m.put("done", en.getValue()[1]);
            m.put("pending", pending.get(en.getKey()));
            evaluators.add(m);
        }

        Map<String, Object> out = new HashMap<>();
        out.put("total", total);
        out.put("done", done);
        out.put("draft", draft);
        out.put("none", none);
        out.put("pct", total > 0 ? (int) Math.round(done * 100.0 / total) : 0);
        out.put("byEvaluator", evaluators);
        out.put("distribution", dist);
        out.put("results", results);
        return out;
    }

    /** 저장 요청 본문 */
    public static class SaveRequest {
        public List<GoalScore> goals;
        public List<CompScore> competencies;
        public String overallComment;
        public String status;   // DRAFT / DONE

        public static class GoalScore { public Long id; public Integer score; }
        public static class CompScore { public String itemName; public Integer score; }
    }

    /** 피평가자 등록 요청 본문 */
    public static class CreateRequest {
        public String evalType;
        public String name;
        public String dept;
        public String position;
        public String evaluatorName;
        public List<GoalDef> goals;

        public static class GoalDef { public String title; public Integer weight; }
    }
}
