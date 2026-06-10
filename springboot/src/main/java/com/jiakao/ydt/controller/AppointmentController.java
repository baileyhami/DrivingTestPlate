package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.entity.BizAppointment;
import com.jiakao.ydt.entity.BizTraining;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.mapper.BizAppointmentMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.service.UserDisplayService;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import com.jiakao.ydt.config.IntegrationProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 练车预约
 */
@RestController
@Slf4j
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final BizAppointmentMapper appointmentMapper;
    private final SysStudentMapper studentMapper;
    private final SysCoachMapper coachMapper;
    private final UserDisplayService userDisplayService;
    private final com.jiakao.ydt.mapper.BizTrainingMapper trainingMapper;
    private final IntegrationProperties integrationProperties;

    private static final Set<String> APPT_STATUS_SET = Set.of("BOOKED", "CHECKED_IN", "COMPLETED", "CANCELLED");

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public R<Page<BizAppointment>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status) {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizAppointment> q = new LambdaQueryWrapper<>();
        if (Roles.STUDENT.equals(u.getRoleCode())) {
            SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getUserId, u.getId()));
            if (st == null) {
                return R.ok(new Page<>(current, size));
            }
            q.eq(BizAppointment::getStudentId, st.getId());
        } else if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach coach = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
            if (coach == null) {
                return R.ok(new Page<>(current, size));
            }
            q.eq(BizAppointment::getCoachId, coach.getId());
        } else if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizAppointment::getSchoolId, u.getSchoolId());
        }
        // ADMIN: 全部
        if (status != null && !status.isBlank()) {
            q.eq(BizAppointment::getStatus, normalizeStatus(status));
        }
        q.orderByDesc(BizAppointment::getAppointmentTime);
        Page<BizAppointment> page = appointmentMapper.selectPage(new Page<>(current, size), q);
        userDisplayService.fillAppointmentParties(page.getRecords());
        return R.ok(page);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public R<Void> create(@Valid @RequestBody ApptBody body) {
        SecurityUser u = SecurityUtils.requireUser();
        SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getUserId, u.getId()));
        if (st == null) {
            throw new BusinessException("学员档案不存在");
        }
        if (st.getSchoolId() == null) {
            throw new BusinessException("请先报名驾校");
        }
        SysCoach coach = coachMapper.selectById(body.getCoachId());
        if (coach == null || !st.getSchoolId().equals(coach.getSchoolId())) {
            throw new BusinessException("教练不存在或不属于本驾校");
        }
        BizAppointment a = new BizAppointment();
        a.setSchoolId(st.getSchoolId());
        a.setStudentId(st.getId());
        a.setCoachId(body.getCoachId());
        a.setSubjectType(body.getSubjectType());
        a.setAppointmentTime(body.getAppointmentTime());
        a.setStatus("BOOKED");
        a.setRemark(body.getRemark());
        appointmentMapper.insert(a);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','COACH')")
    public R<Void> status(@PathVariable Long id, @RequestParam String status) {
        BizAppointment a = appointmentMapper.selectById(id);
        if (a == null) {
            throw new BusinessException("预约不存在");
        }
        SecurityUser u = SecurityUtils.requireUser();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null && !u.getSchoolId().equals(a.getSchoolId())) {
            throw new BusinessException("无权限");
        }
        if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach coach = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
            if (coach == null || !coach.getId().equals(a.getCoachId())) {
                throw new BusinessException("无权限");
            }
        }
        String targetStatus = normalizeStatus(status);
        if (!APPT_STATUS_SET.contains(targetStatus)) {
            throw new BusinessException("非法状态");
        }
        String currentStatus = normalizeStatus(a.getStatus());
        if (currentStatus.equals(targetStatus)) {
            throw new BusinessException("状态已更新，无需重复操作");
        }
        if (!isAllowedApptTransition(currentStatus, targetStatus)) {
            throw new BusinessException("非法状态流转");
        }
        a.setStatus(targetStatus);
        appointmentMapper.updateById(a);
        // 如果状态变为 COMPLETED，则自动生成一条登记培训记录（幂等：先检查是否已存在）
        try {
            if ("COMPLETED".equals(targetStatus)
                    && Boolean.TRUE.equals(integrationProperties.isAutoCreateTrainingOnAppointmentComplete())) {
                BizTraining exist = trainingMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizTraining>().eq(BizTraining::getAppointmentId, a.getId()));
                if (exist == null) {
                    BizTraining t = new BizTraining();
                    t.setAppointmentId(a.getId());
                    t.setSchoolId(a.getSchoolId());
                    t.setStudentId(a.getStudentId());
                    t.setCoachId(a.getCoachId());
                    t.setSubjectType(a.getSubjectType());
                    if (a.getAppointmentTime() != null) {
                        t.setTrainingDate(a.getAppointmentTime().toLocalDate());
                        t.setCheckInTime(a.getAppointmentTime());
                    } else {
                        t.setTrainingDate(java.time.LocalDate.now());
                        t.setCheckInTime(LocalDateTime.now());
                    }
                    t.setDurationHours(new BigDecimal("2"));
                    t.setScore(100);
                    t.setContent("练车预约完成自动登记");
                    trainingMapper.insert(t);
                }
            }
        } catch (Exception ex) {
            // 自动生成培训为可补偿操作，记录异常但不影响状态更新
            log.error("自动生成培训记录失败，appointmentId={}", a.getId(), ex);
        }
        return R.ok();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.trim().toUpperCase();
    }

    private boolean isAllowedApptTransition(String from, String to) {
        if ("BOOKED".equals(from)) {
            return "CHECKED_IN".equals(to) || "COMPLETED".equals(to) || "CANCELLED".equals(to);
        }
        if ("CHECKED_IN".equals(from)) {
            return "COMPLETED".equals(to) || "CANCELLED".equals(to);
        }
        return false;
    }

    @Data
    public static class ApptBody {
        @NotNull private Long coachId;
        @NotNull private String subjectType;
        @NotNull private LocalDateTime appointmentTime;
        private String remark;
    }
}
