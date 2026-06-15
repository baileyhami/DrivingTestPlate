package com.jiakao.ydt.controller;

import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员/驾校/教练 调整学员学车阶段（1-4 学习中，5 结业可评价）
 */
@RestController
@RequestMapping("/roster")
@RequiredArgsConstructor
public class StudentRosterController {

    private final SysStudentMapper studentMapper;
    private final SysCoachMapper coachMapper;

    @PatchMapping("/students/{studentRowId}/learning-stage")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','COACH')")
    public R<Void> updateLearningStage(
            @PathVariable Long studentRowId,
            @RequestParam int stage) {
        if (stage < 1 || stage > 5) {
            throw new BusinessException("阶段取值为 1-5");
        }
        SysStudent st = studentMapper.selectById(studentRowId);
        if (st == null) {
            throw new BusinessException("学员不存在");
        }
        SecurityUser u = SecurityUtils.requireUser();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null && !u.getSchoolId().equals(st.getSchoolId())) {
            throw new BusinessException("无权限");
        }
        if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach c = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>()
                    .eq(SysCoach::getUserId, u.getId()));
            if (c == null || st.getCoachId() == null || !st.getCoachId().equals(c.getId())) {
                throw new BusinessException("只能调整本人学员");
            }
        }
        st.setLearningStage(stage);
        studentMapper.updateById(st);
        return R.ok();
    }
}
