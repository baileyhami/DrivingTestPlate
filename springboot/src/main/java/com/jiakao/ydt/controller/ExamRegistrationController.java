package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.entity.BizExamRegistration;
import com.jiakao.ydt.entity.BizExamVenue;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.mapper.BizExamVenueMapper;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.mapper.BizExamRegistrationMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.service.UserDisplayService;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考场报考
 */
@RestController
@RequestMapping("/exam-registrations")
@RequiredArgsConstructor
public class ExamRegistrationController {

    private final BizExamRegistrationMapper registrationMapper;
    private final SysStudentMapper studentMapper;
    private final com.jiakao.ydt.mapper.SysCoachMapper coachMapper;
    private final BizExamVenueMapper venueMapper;
    private final UserDisplayService userDisplayService;

    private static final Set<String> EXAM_STATUS_SET = Set.of("SUBMITTED", "APPROVED", "REJECTED", "PASSED");

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public R<Page<BizExamRegistration>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "100") long size) {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizExamRegistration> q = new LambdaQueryWrapper<>();
        if (Roles.STUDENT.equals(u.getRoleCode())) {
            SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getUserId, u.getId()));
            if (st == null) {
                return R.ok(new Page<>(current, size));
            }
            q.eq(BizExamRegistration::getStudentId, st.getId());
        } else if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach coach = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
            if (coach == null) {
                return R.ok(new Page<>(current, size));
            }
            List<SysStudent> studs = studentMapper.selectList(new LambdaQueryWrapper<SysStudent>()
                    .eq(SysStudent::getCoachId, coach.getId()));
            if (studs.isEmpty()) {
                return R.ok(new Page<>(current, size));
            }
            List<Long> studentIds = studs.stream().map(SysStudent::getId).collect(Collectors.toList());
            q.in(BizExamRegistration::getStudentId, studentIds);
        } else if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizExamRegistration::getSchoolId, u.getSchoolId());
        }
        q.orderByDesc(BizExamRegistration::getUpdateTime).orderByDesc(BizExamRegistration::getId);
        Page<BizExamRegistration> page = registrationMapper.selectPage(new Page<>(current, size), q);
        userDisplayService.fillExamRegistrationStudents(page.getRecords());
        return R.ok(page);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public R<Void> create(@Valid @RequestBody RegBody body) {
        SecurityUser u = SecurityUtils.requireUser();
        SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getUserId, u.getId()));
        if (st == null) {
            throw new BusinessException("学员档案不存在");
        }
        if (st.getSchoolId() == null) {
            throw new BusinessException("请先报名驾校");
        }
        Long venueId = body.getVenueId();
        if (venueId == null && body.getVenueName() != null && !body.getVenueName().isBlank()) {
            BizExamVenue venue = venueMapper.selectOne(new LambdaQueryWrapper<BizExamVenue>()
                    .eq(BizExamVenue::getName, body.getVenueName().trim())
                    .last("LIMIT 1"));
            if (venue == null) {
                throw new BusinessException("考场不存在，请从考场列表选择正确名称");
            }
            venueId = venue.getId();
        }
        if (venueId == null) {
            throw new BusinessException("请填写考场名称");
        }
        BizExamRegistration r = new BizExamRegistration();
        r.setSchoolId(st.getSchoolId());
        r.setStudentId(st.getId());
        r.setVenueId(venueId);
        r.setExamSubject(body.getExamSubject());
        r.setExamDate(body.getExamDate());
        r.setStatus("SUBMITTED");
        r.setRemark(body.getRemark());
        registrationMapper.insert(r);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public R<Void> status(@PathVariable Long id, @RequestParam String status) {
        BizExamRegistration r = registrationMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("记录不存在");
        }
        SecurityUser u = SecurityUtils.requireUser();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null && !u.getSchoolId().equals(r.getSchoolId())) {
            throw new BusinessException("无权限");
        }
        String targetStatus = normalizeStatus(status);
        if (!EXAM_STATUS_SET.contains(targetStatus)) {
            throw new BusinessException("非法状态");
        }
        String currentStatus = normalizeStatus(r.getStatus());
        if (currentStatus.equals(targetStatus)) {
            throw new BusinessException("状态已更新，无需重复操作");
        }
        if (!isAllowedExamTransition(currentStatus, targetStatus)) {
            throw new BusinessException("非法状态流转");
        }
        r.setStatus(targetStatus);
        registrationMapper.updateById(r);
        return R.ok();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.trim().toUpperCase();
    }

    private boolean isAllowedExamTransition(String from, String to) {
        if ("SUBMITTED".equals(from)) {
            return "APPROVED".equals(to) || "REJECTED".equals(to);
        }
        if ("APPROVED".equals(from)) {
            return "PASSED".equals(to);
        }
        return false;
    }

    @Data
    public static class RegBody {
        private Long venueId;
        private String venueName;
        @NotBlank private String examSubject;
        private LocalDate examDate;
        private String remark;
    }
}
