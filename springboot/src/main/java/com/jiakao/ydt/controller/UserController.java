package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理（管理员创建教练/学员账号）
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final SysCoachMapper coachMapper;
    private final SysStudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public R<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String username) {
        SecurityUser su = SecurityUtils.requireUser();
        LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            q.like(SysUser::getUsername, username);
        }
        if (roleCode != null && !roleCode.isBlank()) {
            q.eq(SysUser::getRoleCode, roleCode);
        }
        if (Roles.SCHOOL.equals(su.getRoleCode()) && su.getSchoolId() != null) {
            q.eq(SysUser::getSchoolId, su.getSchoolId());
        }
        q.orderByDesc(SysUser::getId);
        return R.ok(userMapper.selectPage(new Page<>(current, size), q));
    }

    @PostMapping("/coach")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> createCoach(@Valid @RequestBody CreateCoachBody body) {
        Long schoolId = resolveSchoolId(body.getSchoolId());
        assertSchoolScope(schoolId);
        checkUsername(body.getUsername());

        SysUser u = new SysUser();
        u.setUsername(body.getUsername());
        u.setPassword(passwordEncoder.encode(body.getPassword()));
        u.setPhone(body.getPhone());
        u.setNickname(body.getNickname());
        u.setRoleCode(Roles.COACH);
        u.setSchoolId(schoolId);
        u.setStatus(1);
        userMapper.insert(u);

        SysCoach c = new SysCoach();
        c.setUserId(u.getId());
        c.setSchoolId(schoolId);
        c.setLicenseNo(body.getLicenseNo());
        c.setSpecialty(body.getSpecialty());
        c.setIntro(body.getIntro());
        coachMapper.insert(c);
        return R.ok();
    }

    @PostMapping("/student")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> createStudent(@Valid @RequestBody CreateStudentBody body) {
        Long schoolId = resolveSchoolId(body.getSchoolId());
        assertSchoolScope(schoolId);
        checkUsername(body.getUsername());

        SysUser u = new SysUser();
        u.setUsername(body.getUsername());
        u.setPassword(passwordEncoder.encode(body.getPassword()));
        u.setPhone(body.getPhone());
        u.setNickname(body.getNickname());
        u.setRoleCode(Roles.STUDENT);
        u.setSchoolId(schoolId);
        u.setStatus(1);
        userMapper.insert(u);

        // 校验 coachId（若传入）是否存在并属于同一驾校，防止跨校或无效引用
        if (body.getCoachId() != null) {
            SysCoach coach = coachMapper.selectById(body.getCoachId());
            if (coach == null) {
                throw new BusinessException("教练不存在");
            }
            if (!Objects.equals(coach.getSchoolId(), schoolId)) {
                throw new BusinessException("所选教练不属于指定驾校");
            }
        }

        SysStudent st = new SysStudent();
        st.setUserId(u.getId());
        st.setSchoolId(schoolId);
        st.setCoachId(body.getCoachId());
        st.setIdCardNo(body.getIdCardNo());
        studentMapper.insert(st);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> status(@PathVariable Long id, @RequestParam int status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("非法状态");
        }
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(status);
        userMapper.updateById(u);
        return R.ok();
    }

    private void checkUsername(String username) {
        Long c = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (c != null && c > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    private Long resolveSchoolId(Long bodySchoolId) {
        SecurityUser su = SecurityUtils.requireUser();
        if (Roles.ADMIN.equals(su.getRoleCode())) {
            if (bodySchoolId == null) {
                throw new BusinessException("请指定驾校");
            }
            return bodySchoolId;
        }
        return su.getSchoolId();
    }

    private void assertSchoolScope(Long schoolId) {
        SecurityUser su = SecurityUtils.requireUser();
        if (Roles.SCHOOL.equals(su.getRoleCode()) && su.getSchoolId() != null && !su.getSchoolId().equals(schoolId)) {
            throw new BusinessException("只能为本驾校创建账号");
        }
    }

    @Data
    public static class CreateCoachBody {
        @NotBlank private String username;
        @NotBlank private String password;
        private String phone;
        private String nickname;
        private Long schoolId;
        private String licenseNo;
        private String specialty;
        private String intro;
    }

    @Data
    public static class CreateStudentBody {
        @NotBlank private String username;
        @NotBlank private String password;
        private String phone;
        private String nickname;
        private Long schoolId;
        private Long coachId;
        private String idCardNo;
    }
}
