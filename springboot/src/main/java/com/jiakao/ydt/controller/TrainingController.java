package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.entity.BizTraining;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.entity.BizAppointment;
import com.jiakao.ydt.mapper.BizTrainingMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.mapper.BizAppointmentMapper;
import com.jiakao.ydt.service.UserDisplayService;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 培训 / 签到记录
 */
@RestController
@RequestMapping("/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final BizTrainingMapper trainingMapper;
    private final SysStudentMapper studentMapper;
    private final SysCoachMapper coachMapper;
    private final UserDisplayService userDisplayService;
    private final BizAppointmentMapper appointmentMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public R<Page<BizTraining>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizTraining> q = new LambdaQueryWrapper<>();
        if (Roles.STUDENT.equals(u.getRoleCode())) {
            SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getUserId, u.getId()));
            if (st == null) {
                return R.ok(new Page<>(current, size));
            }
            q.eq(BizTraining::getStudentId, st.getId());
        } else if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach c = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
            if (c == null) {
                return R.ok(new Page<>(current, size));
            }
            q.eq(BizTraining::getCoachId, c.getId());
        } else if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizTraining::getSchoolId, u.getSchoolId());
        }
        q.orderByDesc(BizTraining::getTrainingDate);
        Page<BizTraining> page = trainingMapper.selectPage(new Page<>(current, size), q);
        userDisplayService.fillTrainingParties(page.getRecords());
        return R.ok(page);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','COACH')")
    public R<Void> create(@Valid @RequestBody TrainingBody body) {
        SecurityUser u = SecurityUtils.requireUser();
        Long coachId = body.getCoachId();
        if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach c = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
            if (c == null) {
                throw new BusinessException("教练档案不存在");
            }
            coachId = c.getId();
        }
        if (coachId == null) {
            throw new BusinessException("请指定教练");
        }
        SysCoach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }
        SysStudent st = studentMapper.selectById(body.getStudentId());
        if (st == null) {
            throw new BusinessException("学员不存在");
        }
        if (st.getSchoolId() == null) {
            throw new BusinessException("学员尚未报名驾校");
        }
        if (!Objects.equals(coach.getSchoolId(), st.getSchoolId())) {
            throw new BusinessException("教练与学员不属于同一驾校");
        }
        if (Roles.COACH.equals(u.getRoleCode()) && (st.getCoachId() == null || !st.getCoachId().equals(coachId))) {
            throw new BusinessException("只能为本人名下学员登记培训");
        }
        assertSchool(u, st.getSchoolId());
        if (body.getAppointmentId() != null) {
            BizAppointment appt = appointmentMapper.selectById(body.getAppointmentId());
            if (appt == null) {
                throw new BusinessException("预约不存在");
            }
            if (!Objects.equals(appt.getStudentId(), st.getId()) || !Objects.equals(appt.getCoachId(), coachId)) {
                throw new BusinessException("预约与学员或教练不匹配");
            }
            if ("CANCELLED".equalsIgnoreCase(appt.getStatus())) {
                throw new BusinessException("已取消的预约不能登记培训");
            }
            BizTraining exist = trainingMapper.selectOne(new LambdaQueryWrapper<BizTraining>().eq(BizTraining::getAppointmentId, body.getAppointmentId()));
            if (exist != null) {
                throw new BusinessException("该预约已登记培训");
            }
        }
        BizTraining t = new BizTraining();
        t.setAppointmentId(body.getAppointmentId());
        t.setSchoolId(st.getSchoolId());
        t.setStudentId(st.getId());
        t.setCoachId(coachId);
        t.setSubjectType(body.getSubjectType());
        t.setTrainingDate(body.getTrainingDate());
        t.setCheckInTime(body.getCheckInTime() != null ? body.getCheckInTime() : LocalDateTime.now());
        t.setDurationHours(body.getDurationHours());
        t.setContent(body.getContent());
        t.setScore(body.getScore());
        trainingMapper.insert(t);
        return R.ok();
    }

    private void assertSchool(SecurityUser u, Long schoolId) {
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null && !u.getSchoolId().equals(schoolId)) {
            throw new BusinessException("无权限");
        }
        if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach c = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
            if (c == null || !Objects.equals(c.getSchoolId(), schoolId)) {
                throw new BusinessException("无权限");
            }
        }
    }

    @Data
    public static class TrainingBody {
        private Long appointmentId;
        @NotNull private Long studentId;
        private Long coachId;
        @NotNull private String subjectType;
        @NotNull private LocalDate trainingDate;
        private LocalDateTime checkInTime;
        private BigDecimal durationHours;
        private String content;
        private Integer score;
    }
}
