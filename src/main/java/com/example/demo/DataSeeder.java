package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 최초 1회 데모 데이터 시드.
 *  - 역량 템플릿(유형별)
 *  - 회사 2개: A컨설팅(프로젝트평가), B커머스(인사평가)
 *  - 회사별 피평가자 + 목표(업적) + 일부 점수(완료/작성중/미시작 섞어서)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepo;
    private final CompanyEvalTypeRepository typeRepo;
    private final EvaluateeRepository evaluateeRepo;
    private final GoalRepository goalRepo;
    private final CompetencyScoreRepository scoreRepo;
    private final CompetencyTemplateRepository templateRepo;

    private static final String[] HR_ITEMS = {"직무 전문성", "문제 해결", "협업", "커뮤니케이션", "주도성"};
    private static final String[] PROJECT_ITEMS = {"전문성/기여도", "협업/커뮤니케이션", "일정준수", "산출물품질", "리더십"};

    public DataSeeder(CompanyRepository companyRepo, CompanyEvalTypeRepository typeRepo,
                      EvaluateeRepository evaluateeRepo, GoalRepository goalRepo,
                      CompetencyScoreRepository scoreRepo, CompetencyTemplateRepository templateRepo) {
        this.companyRepo = companyRepo;
        this.typeRepo = typeRepo;
        this.evaluateeRepo = evaluateeRepo;
        this.goalRepo = goalRepo;
        this.scoreRepo = scoreRepo;
        this.templateRepo = templateRepo;
    }

    @Override
    public void run(String... args) {
        if (companyRepo.count() > 0) return;

        // 역량 템플릿
        String[] hrDesc = {
            "담당 직무의 지식·기술을 갖추고 품질 높은 결과물을 내는가",
            "이슈를 구조적으로 분석하고 실행 가능한 해결안을 만드는가",
            "동료·유관부서와 신뢰를 바탕으로 협력하는가",
            "명확하게 전달하고 경청하며 보고가 적시에 이뤄지는가",
            "지시 없이도 개선점을 찾아 실행하는가"
        };
        String[] projDesc = {
            "프로젝트에 기여하는 전문성과 결과물 수준",
            "팀·클라이언트와의 협업 및 소통",
            "마일스톤·납기 준수",
            "산출물(보고서/딜리버러블)의 품질",
            "이니셔티브와 리더십"
        };
        for (int i = 0; i < HR_ITEMS.length; i++) seedTemplate("HR", HR_ITEMS[i], hrDesc[i], i + 1);
        for (int i = 0; i < PROJECT_ITEMS.length; i++) seedTemplate("PROJECT", PROJECT_ITEMS[i], projDesc[i], i + 1);

        // ── 회사 A: 컨설팅 (프로젝트 평가) ──
        Long a = createCompany("A컨설팅");
        setType(a, "PROJECT", true);
        setType(a, "HR", false);
        seedEvaluatee(a, "PROJECT", "김민서", "감사본부", "Senior", "박파트너", "DONE",
                new String[]{"A사 회계감사 마감", "감사조서 품질개선", "신입 OJT"}, new int[]{50, 30, 20},
                new Integer[]{5, 4, 4}, PROJECT_ITEMS, new Integer[]{5, 4, 5, 4, 4}, "납기 준수 우수, 리더십 좋음.");
        seedEvaluatee(a, "PROJECT", "이준호", "감사본부", "Staff", "박파트너", "DRAFT",
                new String[]{"실사 지원", "조서 작성", "교육 이수"}, new int[]{50, 30, 20},
                new Integer[]{3, 4, null}, PROJECT_ITEMS, new Integer[]{3, 4, null, null, null}, null);
        seedEvaluatee(a, "PROJECT", "정수아", "딜본부", "Manager", "최파트너", "NONE",
                new String[]{"M&A 자문 2건", "밸류에이션 모델링", "제안서 작성"}, new int[]{40, 40, 20},
                null, PROJECT_ITEMS, null, null);

        // ── 회사 B: 커머스 (인사 성과평가) ──
        Long b = createCompany("B커머스");
        setType(b, "HR", true);
        setType(b, "PROJECT", false);
        seedEvaluatee(b, "HR", "최현우", "개발팀", "대리", "김실장", "DONE",
                new String[]{"서비스 장애율 0.1% 이하", "신규 기능 3건 출시", "코드리뷰 문화 정착"}, new int[]{40, 40, 20},
                new Integer[]{4, 5, 4}, HR_ITEMS, new Integer[]{5, 4, 4, 3, 5}, "기술 리더십 돋보임.");
        seedEvaluatee(b, "HR", "한지민", "개발팀", "사원", "김실장", "NONE",
                new String[]{"배포 자동화 구축", "테스트 커버리지 60%", "기술 블로그 2건"}, new int[]{50, 30, 20},
                null, HR_ITEMS, null, null);
        seedEvaluatee(b, "HR", "오세훈", "마케팅팀", "대리", "이팀장", "DONE",
                new String[]{"리드 전환율 5% 달성", "콘텐츠 발행 월 8건", "브랜드 설문 NPS +10"}, new int[]{50, 30, 20},
                new Integer[]{3, 4, 3}, HR_ITEMS, new Integer[]{3, 4, 3, 4, 3}, "꾸준한 성과. 전환율 개선 여지.");
    }

    private void seedTemplate(String type, String name, String desc, int order) {
        CompetencyTemplate t = new CompetencyTemplate();
        t.setEvalType(type);
        t.setItemName(name);
        t.setDescription(desc);
        t.setSortOrder(order);
        templateRepo.save(t);
    }

    private Long createCompany(String name) {
        Company c = new Company();
        c.setName(name);
        return companyRepo.save(c).getId();
    }

    private void setType(Long companyId, String type, boolean enabled) {
        CompanyEvalType t = new CompanyEvalType();
        t.setCompanyId(companyId);
        t.setEvalType(type);
        t.setEnabled(enabled);
        typeRepo.save(t);
    }

    private void seedEvaluatee(Long companyId, String type, String name, String dept, String position,
                               String evaluator, String status, String[] goalTitles, int[] weights,
                               Integer[] goalScores, String[] compItems, Integer[] compScores, String comment) {
        Evaluatee e = new Evaluatee();
        e.setCompanyId(companyId);
        e.setEvalType(type);
        e.setName(name);
        e.setDept(dept);
        e.setPosition(position);
        e.setEvaluatorName(evaluator);
        e.setStatus(status);
        e.setOverallComment(comment);
        Long id = evaluateeRepo.save(e).getId();

        for (int i = 0; i < goalTitles.length; i++) {
            Goal g = new Goal();
            g.setEvaluateeId(id);
            g.setTitle(goalTitles[i]);
            g.setWeight(weights[i]);
            g.setScore(goalScores != null ? goalScores[i] : null);
            g.setSortOrder(i + 1);
            goalRepo.save(g);
        }
        if (compScores != null) {
            for (int i = 0; i < compItems.length; i++) {
                if (compScores[i] == null) continue;
                CompetencyScore cs = new CompetencyScore();
                cs.setEvaluateeId(id);
                cs.setItemName(compItems[i]);
                cs.setScore(compScores[i]);
                scoreRepo.save(cs);
            }
        }
    }
}
