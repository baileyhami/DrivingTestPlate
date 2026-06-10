package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.dto.MockExamResultVO;
import com.jiakao.ydt.dto.MockProgressRequest;
import com.jiakao.ydt.dto.MockProgressVO;
import com.jiakao.ydt.dto.MockStartRequest;
import com.jiakao.ydt.dto.MockSubmitRequest;
import com.jiakao.ydt.dto.QuestionExamVO;
import com.jiakao.ydt.entity.BizMockExam;
import com.jiakao.ydt.security.SecurityUtils;
import com.jiakao.ydt.service.MockExamService;
import com.jiakao.ydt.service.UserDisplayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模拟考试（学员练习；游客公开试做不落库）
 */
@RestController
@RequestMapping("/mock-exams")
@RequiredArgsConstructor
public class MockExamController {

    private final MockExamService mockExamService;
    private final UserDisplayService userDisplayService;

    @PostMapping("/public/start")
    public R<List<QuestionExamVO>> publicStart(@Valid @RequestBody MockStartRequest req) {
        return R.ok(mockExamService.start(req));
    }

    @PostMapping("/public/submit")
    public R<MockExamResultVO> publicSubmit(@Valid @RequestBody MockSubmitRequest req) {
        return R.ok(mockExamService.submitGuest(req));
    }

    @PostMapping("/public/progress")
    public R<MockProgressVO> publicProgress(@Valid @RequestBody MockProgressRequest req) {
        return R.ok(mockExamService.progress(req));
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('STUDENT')")
    public R<List<QuestionExamVO>> start(@Valid @RequestBody MockStartRequest req) {
        return R.ok(mockExamService.start(req));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public R<MockExamResultVO> submit(@Valid @RequestBody MockSubmitRequest req) {
        BizMockExam exam = mockExamService.submit(SecurityUtils.requireUser().getId(), req);
        return R.ok(mockExamService.toResultVo(exam));
    }

    @PostMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public R<MockProgressVO> progress(@Valid @RequestBody MockProgressRequest req) {
        return R.ok(mockExamService.progress(req));
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('STUDENT','COACH')")
    public R<Page<BizMockExam>> records(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "100") long size,
            @RequestParam(required = false) Integer subject) {
        var page = mockExamService.pageRecords(SecurityUtils.requireUser(), current, size, subject);
        userDisplayService.fillMockExamStudents(page.getRecords());
        return R.ok(page);
    }
}
