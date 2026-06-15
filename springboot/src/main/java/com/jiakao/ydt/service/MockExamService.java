package com.jiakao.ydt.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.dto.MockExamResultVO;
import com.jiakao.ydt.dto.MockProgressRequest;
import com.jiakao.ydt.dto.MockProgressVO;
import com.jiakao.ydt.dto.MockStartRequest;
import com.jiakao.ydt.dto.MockSubmitRequest;
import com.jiakao.ydt.dto.QuestionExamVO;
import com.jiakao.ydt.entity.BizMockExam;
import com.jiakao.ydt.entity.BizQuestion;
import com.jiakao.ydt.entity.BizWrongBook;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.mapper.BizMockExamMapper;
import com.jiakao.ydt.mapper.BizWrongBookMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockExamService {

    private static final int PASS_SCORE = 90;

    private final QuestionQueryService questionQueryService;
    private final BizMockExamMapper mockExamMapper;
    private final BizWrongBookMapper wrongBookMapper;
    private final SysCoachMapper coachMapper;
    private final SysStudentMapper studentMapper;

    public List<QuestionExamVO> start(MockStartRequest req) {
        String mode = normalizeMode(req.getMode());
        List<BizQuestion> list = new ArrayList<>(questionQueryService.listBySubject(req.getSubject()));
        if (list.isEmpty()) {
            throw new BusinessException("Question bank is empty for subject " + req.getSubject());
        }
        int n;
        if ("SIMULATION".equals(mode)) {
            n = requiredSimulationCount(req.getSubject());
            if (list.size() < n) {
                throw new BusinessException("Question bank has fewer than " + n + " questions for subject " + req.getSubject());
            }
            Collections.shuffle(list);
        } else if ("SEQUENTIAL".equals(mode)) {
            int startNo = req.getStartQuestionNo() == null ? 1 : req.getStartQuestionNo();
            if (startNo > list.size()) {
                throw new BusinessException("Start question number exceeds current subject question count: " + list.size());
            }
            list = list.subList(startNo - 1, list.size());
            n = Math.min(req.getQuestionCount(), list.size());
        } else {
            Collections.shuffle(list);
            n = Math.min(req.getQuestionCount(), list.size());
        }
        return list.stream().limit(n).map(this::toExamVo).collect(Collectors.toList());
    }

    public MockProgressVO progress(MockProgressRequest req) {
        int wrong = countWrong(req.getSubject(), req.getAnswers());
        int maxWrong = maxWrong(req.getSubject());
        MockProgressVO vo = new MockProgressVO();
        vo.setWrongCount(wrong);
        vo.setMaxWrong(maxWrong);
        vo.setFailed(wrong > maxWrong);
        return vo;
    }

    private int requiredSimulationCount(Integer subject) {
        if (Integer.valueOf(1).equals(subject)) {
            return 100;
        }
        if (Integer.valueOf(4).equals(subject)) {
            return 50;
        }
        throw new BusinessException("Only subject 1 and subject 4 mock exams are supported");
    }

    private String normalizeMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return "RANDOM";
        }
        String mode = raw.trim().toUpperCase(Locale.ROOT);
        if ("RANDOM".equals(mode) || "SEQUENTIAL".equals(mode) || "SIMULATION".equals(mode)) {
            return mode;
        }
        return "RANDOM";
    }

    private QuestionExamVO toExamVo(BizQuestion q) {
        QuestionExamVO vo = new QuestionExamVO();
        BeanUtils.copyProperties(q, vo);
        return vo;
    }

    public MockExamResultVO submitGuest(MockSubmitRequest req) {
        GradedExam graded = grade(req);
        return toResultVo(graded, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public BizMockExam submit(Long userId, MockSubmitRequest req) {
        GradedExam graded = grade(req);
        LocalDateTime now = LocalDateTime.now();
        for (Long qid : graded.wrongQuestionIds()) {
            upsertWrong(userId, qid, now);
        }
        BizMockExam exam = new BizMockExam();
        exam.setUserId(userId);
        exam.setSubject(req.getSubject());
        exam.setScore(graded.score());
        exam.setTotalCount(graded.total());
        exam.setCorrectCount(graded.correct());
        exam.setDurationSec(req.getDurationSec());
        exam.setDetailJson(graded.detailJson());
        mockExamMapper.insert(exam);
        return exam;
    }

    private MockExamResultVO toResultVo(GradedExam graded, Long persistedId) {
        MockExamResultVO vo = new MockExamResultVO();
        vo.setId(persistedId);
        vo.setSubject(graded.subject());
        vo.setScore(graded.score());
        vo.setTotalCount(graded.total());
        vo.setCorrectCount(graded.correct());
        vo.setDurationSec(graded.durationSec());
        vo.setPassed(graded.score() >= PASS_SCORE);
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }

    public MockExamResultVO toResultVo(BizMockExam exam) {
        MockExamResultVO vo = new MockExamResultVO();
        vo.setId(exam.getId());
        vo.setSubject(exam.getSubject());
        vo.setScore(exam.getScore());
        vo.setTotalCount(exam.getTotalCount());
        vo.setCorrectCount(exam.getCorrectCount());
        vo.setDurationSec(exam.getDurationSec());
        vo.setPassed(exam.getScore() != null && exam.getScore() >= PASS_SCORE);
        vo.setCreateTime(exam.getCreateTime());
        return vo;
    }

    public Page<BizMockExam> pageRecords(SecurityUser u, long current, long size, Integer subject) {
        LambdaQueryWrapper<BizMockExam> q = new LambdaQueryWrapper<>();
        if (Roles.STUDENT.equals(u.getRoleCode())) {
            q.eq(BizMockExam::getUserId, u.getId());
        } else if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach c = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>()
                    .eq(SysCoach::getUserId, u.getId()));
            if (c == null) {
                return emptyPage(current, size);
            }
            List<SysStudent> studs = studentMapper.selectList(new LambdaQueryWrapper<SysStudent>()
                    .eq(SysStudent::getCoachId, c.getId()));
            Set<Long> uids = studs.stream().map(SysStudent::getUserId).collect(Collectors.toSet());
            if (uids.isEmpty()) {
                return emptyPage(current, size);
            }
            q.in(BizMockExam::getUserId, uids);
        } else {
            throw new BusinessException("No permission to view exam records");
        }
        if (subject != null) {
            q.eq(BizMockExam::getSubject, subject);
        }
        q.orderByDesc(BizMockExam::getId);
        return mockExamMapper.selectPage(new Page<>(current, size), q);
    }

    private Page<BizMockExam> emptyPage(long current, long size) {
        Page<BizMockExam> p = new Page<>(current, size, 0);
        p.setRecords(List.of());
        return p;
    }

    private GradedExam grade(MockSubmitRequest req) {
        Map<Long, BizQuestion> map = fetchQuestionMap(req.getSubject(), req.getAnswers());
        int correct = 0;
        List<Long> wrongIds = new ArrayList<>();
        JSONArray detail = new JSONArray();
        for (MockSubmitRequest.AnswerItem item : req.getAnswers()) {
            BizQuestion q = map.get(item.getQuestionId());
            boolean ok = normalizeAnswer(item.getUserAnswer()).equals(normalizeAnswer(q.getAnswer()));
            if (ok) {
                correct++;
            } else {
                wrongIds.add(q.getId());
            }
            JSONObject row = new JSONObject();
            row.set("questionId", q.getId());
            row.set("userAnswer", item.getUserAnswer());
            row.set("correctAnswer", q.getAnswer());
            row.set("right", ok);
            row.set("title", q.getTitle());
            detail.add(row);
        }
        int total = req.getAnswers().size();
        int score = total == 0 ? 0 : (int) Math.round(correct * 100.0 / total);
        return new GradedExam(req.getSubject(), score, total, correct, req.getDurationSec(), detail.toString(), wrongIds);
    }

    private int countWrong(Integer subject, List<MockProgressRequest.AnswerItem> answers) {
        Map<Long, BizQuestion> map = fetchQuestionMap(subject, answers);
        int wrong = 0;
        for (MockProgressRequest.AnswerItem item : answers) {
            BizQuestion q = map.get(item.getQuestionId());
            boolean ok = normalizeAnswer(item.getUserAnswer()).equals(normalizeAnswer(q.getAnswer()));
            if (!ok) {
                wrong++;
            }
        }
        return wrong;
    }

    private <T> Map<Long, BizQuestion> fetchQuestionMap(Integer subject, List<T> answers) {
        Set<Long> questionIds = new HashSet<>();
        for (T item : answers) {
            Long questionId;
            if (item instanceof MockSubmitRequest.AnswerItem submitItem) {
                questionId = submitItem.getQuestionId();
            } else if (item instanceof MockProgressRequest.AnswerItem progressItem) {
                questionId = progressItem.getQuestionId();
            } else {
                continue;
            }
            if (questionId != null) {
                questionIds.add(questionId);
            }
        }
        Map<Long, BizQuestion> map = questionQueryService.mapByIds(questionIds);
        for (Long questionId : questionIds) {
            BizQuestion q = map.get(questionId);
            if (q == null || !Objects.equals(q.getSubject(), subject)) {
                throw new BusinessException("Invalid question: " + questionId);
            }
        }
        return map;
    }

    private int maxWrong(Integer subject) {
        int total = requiredSimulationCount(subject);
        int requiredCorrect = (int) Math.ceil(total * PASS_SCORE / 100.0);
        return total - requiredCorrect;
    }

    private record GradedExam(
            Integer subject,
            int score,
            int total,
            int correct,
            Integer durationSec,
            String detailJson,
            List<Long> wrongQuestionIds
    ) {
    }

    private void upsertWrong(Long userId, Long questionId, LocalDateTime now) {
        BizWrongBook wb = wrongBookMapper.selectOne(new LambdaQueryWrapper<BizWrongBook>()
                .eq(BizWrongBook::getUserId, userId)
                .eq(BizWrongBook::getQuestionId, questionId));
        if (wb == null) {
            wb = new BizWrongBook();
            wb.setUserId(userId);
            wb.setQuestionId(questionId);
            wb.setWrongTimes(1);
            wb.setLastWrongAt(now);
            wrongBookMapper.insert(wb);
        } else {
            wb.setWrongTimes(wb.getWrongTimes() + 1);
            wb.setLastWrongAt(now);
            wrongBookMapper.updateById(wb);
        }
    }

    private String normalizeAnswer(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-D]", "");
        char[] arr = s.toCharArray();
        Arrays.sort(arr);
        return new String(arr);
    }
}
