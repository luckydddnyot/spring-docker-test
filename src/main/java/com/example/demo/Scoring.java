package com.example.demo;

import java.util.List;

/**
 * 점수/등급 계산. (가중치·등급기준은 v1에서 고정, Phase C에서 회사별 설정화 예정)
 *  - 종합점수 = 업적(가중평균) × 60% + 역량(평균) × 40%
 *  - 등급: 4.5↑ S · 4.0↑ A · 3.0↑ B · 2.0↑ C · 미만 D
 */
public class Scoring {
    public static final double W_PERF = 0.6;
    public static final double W_COMP = 0.4;

    /** 업적 가중평균 (점수 매겨진 목표만). 없으면 null */
    public static Double perfScore(List<Goal> goals) {
        double s = 0, w = 0;
        for (Goal g : goals) {
            if (g.getScore() != null && g.getWeight() != null) {
                s += g.getScore() * g.getWeight();
                w += g.getWeight();
            }
        }
        return w > 0 ? s / w : null;
    }

    /** 역량 평균 (점수 매겨진 항목만). 없으면 null */
    public static Double compScore(List<CompetencyScore> scores) {
        int n = 0, sum = 0;
        for (CompetencyScore c : scores) {
            if (c.getScore() != null) { sum += c.getScore(); n++; }
        }
        return n > 0 ? (double) sum / n : null;
    }

    public static Double totalScore(Double perf, Double comp) {
        if (perf == null || comp == null) return null;
        return perf * W_PERF + comp * W_COMP;
    }

    public static String grade(Double total) {
        if (total == null) return "-";
        if (total >= 4.5) return "S";
        if (total >= 4.0) return "A";
        if (total >= 3.0) return "B";
        if (total >= 2.0) return "C";
        return "D";
    }

    public static Double round2(Double v) {
        if (v == null) return null;
        return Math.round(v * 100) / 100.0;
    }
}
