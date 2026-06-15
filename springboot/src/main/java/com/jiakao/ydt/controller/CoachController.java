package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.dto.CoachOptionVO;
import com.jiakao.ydt.dto.CoachRankCardVO;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import com.jiakao.ydt.dto.CoachStudentVO;
import com.jiakao.ydt.service.CoachStudentService;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教练下拉数据（预约用 coachId）
 */
@RestController
@RequestMapping("/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final SysCoachMapper coachMapper;
    private final SysUserMapper userMapper;
    private final CoachStudentService coachStudentService;

    @GetMapping("/my-students")
    @PreAuthorize("hasRole('COACH')")
    public R<List<CoachStudentVO>> myStudents() {
        return R.ok(coachStudentService.listMyStudents(SecurityUtils.requireUser()));
    }

    @GetMapping("/public/rankings")
    public R<List<CoachRankCardVO>> publicRankings() {
        return R.ok(coachMapper.selectCoachPublicRankings());
    }

    @GetMapping("/public/by-school")
    public R<List<CoachOptionVO>> publicBySchool(@RequestParam Long schoolId) {
        LambdaQueryWrapper<SysCoach> q = new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getSchoolId, schoolId);
        List<SysCoach> coaches = coachMapper.selectList(q);
        if (coaches.isEmpty()) {
            return R.ok(List.of());
        }
        List<Long> uids = coaches.stream().map(SysCoach::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, uids))
                .stream().collect(Collectors.toMap(SysUser::getId, x -> x));
        List<CoachOptionVO> list = coaches.stream().map(c -> {
            CoachOptionVO vo = new CoachOptionVO();
            vo.setCoachId(c.getId());
            vo.setSpecialty(c.getSpecialty());
            SysUser usr = userMap.get(c.getUserId());
            if (usr != null) {
                vo.setNickname(usr.getNickname());
                vo.setUsername(usr.getUsername());
            }
            return vo;
        }).collect(Collectors.toList());
        return R.ok(list);
    }

    @GetMapping("/options")
    @PreAuthorize("isAuthenticated()")
    public R<List<CoachOptionVO>> options() {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<SysCoach> q = new LambdaQueryWrapper<>();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(SysCoach::getSchoolId, u.getSchoolId());
        } else if (Roles.STUDENT.equals(u.getRoleCode())) {
            if (u.getSchoolId() == null) {
                return R.ok(List.of());
            }
            q.eq(SysCoach::getSchoolId, u.getSchoolId());
        } else if (Roles.COACH.equals(u.getRoleCode())) {
            q.eq(SysCoach::getUserId, u.getId());
        }
        // ADMIN: 全部
        List<SysCoach> coaches = coachMapper.selectList(q);
        if (coaches.isEmpty()) {
            return R.ok(List.of());
        }
        List<Long> uids = coaches.stream().map(SysCoach::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, uids))
                .stream().collect(Collectors.toMap(SysUser::getId, x -> x));
        List<CoachOptionVO> list = coaches.stream().map(c -> {
            CoachOptionVO vo = new CoachOptionVO();
            vo.setCoachId(c.getId());
            vo.setSpecialty(c.getSpecialty());
            SysUser usr = userMap.get(c.getUserId());
            if (usr != null) {
                vo.setNickname(usr.getNickname());
                vo.setUsername(usr.getUsername());
            }
            return vo;
        }).collect(Collectors.toList());
        return R.ok(list);
    }
}
