package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.entity.BizEnrollment;
import com.jiakao.ydt.mapper.BizEnrollmentMapper;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 招生管理
 */
@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final BizEnrollmentMapper enrollmentMapper;

    private static final Set<String> ENROLL_STATUS_SET = Set.of("PENDING", "APPROVED", "REJECTED");

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public R<Page<BizEnrollment>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status) {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizEnrollment> q = new LambdaQueryWrapper<>();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizEnrollment::getSchoolId, u.getSchoolId());
        }
        if (status != null && !status.isBlank()) {
            q.eq(BizEnrollment::getStatus, status);
        }
        q.orderByDesc(BizEnrollment::getId);
        return R.ok(enrollmentMapper.selectPage(new Page<>(current, size), q));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public R<Void> create(@Valid @RequestBody EnrollBody body) {
        SecurityUser u = SecurityUtils.requireUser();
        Long schoolId = Roles.ADMIN.equals(u.getRoleCode()) ? body.getSchoolId() : u.getSchoolId();
        if (schoolId == null) {
            throw new BusinessException("请指定驾校");
        }
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null && !u.getSchoolId().equals(schoolId)) {
            throw new BusinessException("无权限");
        }
        BizEnrollment e = new BizEnrollment();
        e.setSchoolId(schoolId);
        e.setApplicantName(body.getApplicantName());
        e.setPhone(body.getPhone());
        e.setIdCard(body.getIdCard());
        e.setSource(body.getSource());
        e.setStatus("PENDING");
        e.setRemark(body.getRemark());
        enrollmentMapper.insert(e);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        BizEnrollment e = enrollmentMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("记录不存在");
        }
        assertSchoolRow(e.getSchoolId());
        String targetStatus = normalizeStatus(status);
        if (!ENROLL_STATUS_SET.contains(targetStatus)) {
            throw new BusinessException("非法状态");
        }
        String currentStatus = normalizeStatus(e.getStatus());
        if (currentStatus.equals(targetStatus)) {
            throw new BusinessException("状态已更新，无需重复操作");
        }
        if (!isAllowedEnrollTransition(currentStatus, targetStatus)) {
            throw new BusinessException("非法状态流转");
        }
        e.setStatus(targetStatus);
        e.setHandlerUserId(SecurityUtils.requireUser().getId());
        enrollmentMapper.updateById(e);
        return R.ok();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.trim().toUpperCase();
    }

    private boolean isAllowedEnrollTransition(String from, String to) {
        if ("PENDING".equals(from)) {
            return "APPROVED".equals(to) || "REJECTED".equals(to);
        }
        return false;
    }

    private void assertSchoolRow(Long schoolId) {
        SecurityUser u = SecurityUtils.requireUser();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null && !u.getSchoolId().equals(schoolId)) {
            throw new BusinessException("无权限");
        }
    }

    @Data
    public static class EnrollBody {
        private Long schoolId;
        @NotBlank private String applicantName;
        @NotBlank private String phone;
        private String idCard;
        private String source;
        private String remark;
    }
}
