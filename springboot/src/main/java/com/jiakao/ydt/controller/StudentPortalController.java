package com.jiakao.ydt.controller;

import com.jiakao.ydt.common.R;
import com.jiakao.ydt.dto.LoginResponse;
import com.jiakao.ydt.dto.StudentEnrollRequest;
import com.jiakao.ydt.dto.StudentEnrollmentVO;
import com.jiakao.ydt.dto.StudentReviewRequest;
import com.jiakao.ydt.security.SecurityUtils;
import com.jiakao.ydt.service.StudentPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 学员门户：报名驾校、评价
 */
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    @PostMapping("/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public R<LoginResponse> enroll(@Valid @RequestBody StudentEnrollRequest req) {
        return R.ok(studentPortalService.enroll(SecurityUtils.requireUser().getId(), req));
    }

    @GetMapping("/enrollment")
    @PreAuthorize("hasRole('STUDENT')")
    public R<StudentEnrollmentVO> enrollment() {
        return R.ok(studentPortalService.enrollmentInfo(SecurityUtils.requireUser().getId()));
    }

    @PostMapping("/review")
    @PreAuthorize("hasRole('STUDENT')")
    public R<Void> review(@Valid @RequestBody StudentReviewRequest req) {
        studentPortalService.submitReview(SecurityUtils.requireUser().getId(), req);
        return R.ok();
    }
}
