package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 学员查询接口（登记时用于选择学员）
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final SysStudentMapper studentMapper;
    private final SysCoachMapper coachMapper;
    private final SysUserMapper userMapper;

    @GetMapping("/options")
    public R<List<Map<String, Object>>> options(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String coachName,
            @RequestParam(defaultValue = "50") int limit) {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<SysStudent> q = new LambdaQueryWrapper<>();
        // 角色范围限制
        if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach c = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
            if (c == null) {
                return R.ok(Collections.emptyList());
            }
            q.eq(SysStudent::getCoachId, c.getId());
        } else if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(SysStudent::getSchoolId, u.getSchoolId());
        }
        // 按学员姓名（昵称或用户名）模糊过滤
        if (name != null && !name.isBlank()) {
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().like(SysUser::getNickname, name).or().like(SysUser::getUsername, name).last("limit 200"));
            if (users.isEmpty()) {
                return R.ok(Collections.emptyList());
            }
            Set<Long> uids = new HashSet<>();
            for (SysUser uu : users) uids.add(uu.getId());
            q.in(SysStudent::getUserId, uids);
        }
        // 按教练姓名过滤（仅管理员或驾校可用）
        if (coachName != null && !coachName.isBlank()) {
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().like(SysUser::getNickname, coachName).or().like(SysUser::getUsername, coachName).last("limit 200"));
            if (users.isEmpty()) {
                return R.ok(Collections.emptyList());
            }
            Set<Long> uids = new HashSet<>();
            for (SysUser uu : users) uids.add(uu.getId());
            List<SysCoach> coaches = coachMapper.selectList(new LambdaQueryWrapper<SysCoach>().in(SysCoach::getUserId, uids));
            if (coaches.isEmpty()) {
                return R.ok(Collections.emptyList());
            }
            Set<Long> cids = new HashSet<>();
            for (SysCoach cc : coaches) cids.add(cc.getId());
            q.in(SysStudent::getCoachId, cids);
        }
        q.last("limit " + Math.max(1, limit));
        List<SysStudent> studs = studentMapper.selectList(q);
        if (studs.isEmpty()) {
            return R.ok(Collections.emptyList());
        }
        // 加载用户昵称与教练昵称
        Set<Long> userIds = new HashSet<>();
        Set<Long> coachIds = new HashSet<>();
        for (SysStudent s : studs) {
            if (s.getUserId() != null) userIds.add(s.getUserId());
            if (s.getCoachId() != null) coachIds.add(s.getCoachId());
        }
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds));
        Map<Long, String> uid2nick = new HashMap<>();
        for (SysUser uu : users) {
            String n = uu.getNickname();
            if (n == null || n.isBlank()) n = uu.getUsername();
            uid2nick.put(uu.getId(), n == null ? "" : n);
        }
        List<SysCoach> coaches = coachMapper.selectList(new LambdaQueryWrapper<SysCoach>().in(SysCoach::getId, coachIds));
        Set<Long> coachUserIds = new HashSet<>();
        for (SysCoach cc : coaches) if (cc.getUserId() != null) coachUserIds.add(cc.getUserId());
        List<SysUser> coachUsers = userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, coachUserIds));
        Map<Long, String> coachUid2nick = new HashMap<>();
        for (SysUser uu : coachUsers) {
            String n = uu.getNickname();
            if (n == null || n.isBlank()) n = uu.getUsername();
            coachUid2nick.put(uu.getId(), n == null ? "" : n);
        }
        Map<Long, String> coachId2nick = new HashMap<>();
        for (SysCoach cc : coaches) {
            if (cc.getUserId() != null) coachId2nick.put(cc.getId(), coachUid2nick.getOrDefault(cc.getUserId(), ""));
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (SysStudent s : studs) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("userId", s.getUserId());
            m.put("nickname", uid2nick.getOrDefault(s.getUserId(), ""));
            m.put("coachId", s.getCoachId());
            m.put("coachNickname", coachId2nick.getOrDefault(s.getCoachId(), ""));
            m.put("learningStage", s.getLearningStage());
            out.add(m);
        }
        return R.ok(out);
    }
}

