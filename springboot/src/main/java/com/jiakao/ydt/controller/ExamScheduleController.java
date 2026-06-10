package com.jiakao.ydt.controller;

import com.jiakao.ydt.common.R;
import com.jiakao.ydt.dto.OfficialExamScheduleInfoDto;
import com.jiakao.ydt.service.OfficialExamScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 官方考试时间 / 考试能力查询（对接公开页配置）
 */
@RestController
@RequestMapping("/exam-schedule")
@RequiredArgsConstructor
public class ExamScheduleController {

    private final OfficialExamScheduleService officialExamScheduleService;

    @GetMapping("/official-info")
    @PreAuthorize("isAuthenticated()")
    public R<OfficialExamScheduleInfoDto> officialInfo() {
        return R.ok(officialExamScheduleService.getInfo());
    }
}
