package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 슈퍼어드민 API. 회사 등록 + 회사별 평가유형 on/off.
 *  GET  /api/admin/companies            회사 + 평가유형 설정 목록
 *  POST /api/admin/companies            회사 등록
 *  PUT  /api/admin/companies/{id}/types 평가유형 on/off 설정
 */
@RestController
@RequestMapping("/api/admin")
public class SuperAdminController {

    private static final String[] ALL_TYPES = {"HR", "PROJECT"};

    private final CompanyRepository companyRepo;
    private final CompanyEvalTypeRepository typeRepo;

    public SuperAdminController(CompanyRepository companyRepo, CompanyEvalTypeRepository typeRepo) {
        this.companyRepo = companyRepo;
        this.typeRepo = typeRepo;
    }

    @GetMapping("/companies")
    public List<Map<String, Object>> companies() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Company c : companyRepo.findAll()) {
            Map<String, Boolean> enabledMap = new HashMap<>();
            for (CompanyEvalType t : typeRepo.findByCompanyId(c.getId())) {
                enabledMap.put(t.getEvalType(), Boolean.TRUE.equals(t.getEnabled()));
            }
            List<Map<String, Object>> types = new ArrayList<>();
            for (String type : ALL_TYPES) {
                Map<String, Object> tm = new HashMap<>();
                tm.put("evalType", type);
                tm.put("enabled", enabledMap.getOrDefault(type, false));
                types.add(tm);
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("types", types);
            out.add(m);
        }
        return out;
    }

    @PostMapping("/companies")
    public Map<String, Object> create(@RequestBody Map<String, String> body) {
        Company c = new Company();
        c.setName(body.get("name"));
        Company saved = companyRepo.save(c);
        // 기본 평가유형 행 생성 (모두 off로 시작 → 슈퍼어드민이 켬)
        for (String type : ALL_TYPES) {
            CompanyEvalType t = new CompanyEvalType();
            t.setCompanyId(saved.getId());
            t.setEvalType(type);
            t.setEnabled(false);
            typeRepo.save(t);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("id", saved.getId());
        return r;
    }

    @PutMapping("/companies/{id}/types")
    public Map<String, Object> setTypes(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        // body 예: {"HR": true, "PROJECT": false}
        for (String type : ALL_TYPES) {
            if (!body.containsKey(type)) continue;
            CompanyEvalType t = typeRepo.findByCompanyIdAndEvalType(id, type)
                    .orElseGet(() -> {
                        CompanyEvalType n = new CompanyEvalType();
                        n.setCompanyId(id);
                        n.setEvalType(type);
                        return n;
                    });
            t.setEnabled(body.get(type));
            typeRepo.save(t);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("ok", true);
        return r;
    }
}
