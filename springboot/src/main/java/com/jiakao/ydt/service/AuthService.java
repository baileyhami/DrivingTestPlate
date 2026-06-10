package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.dto.LoginRequest;
import com.jiakao.ydt.dto.LoginResponse;
import com.jiakao.ydt.dto.RegisterCoachRequest;
import com.jiakao.ydt.dto.RegisterRequest;
import com.jiakao.ydt.dto.RegisterSchoolRequest;
import com.jiakao.ydt.dto.RegisterStudentRequest;
import com.jiakao.ydt.dto.UserProfileVO;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysSchool;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.BizReviewMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysSchoolMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import com.jiakao.ydt.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 登录注册
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysSchoolMapper schoolMapper;
    private final SysStudentMapper studentMapper;
    private final SysCoachMapper coachMapper;
    private final BizReviewMapper bizReviewMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public LoginResponse login(LoginRequest req) {
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (u == null || u.getStatus() != null && u.getStatus() == 0) {
            throw new BusinessException("用户不存在或已禁用");
        }
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        Long jwtSchoolId = resolveJwtSchoolId(u);
        String token = jwtUtils.createToken(u.getId(), u.getRoleCode(), jwtSchoolId);
        return new LoginResponse(token, buildProfile(u));
    }

    public LoginResponse refreshLogin(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null || u.getStatus() != null && u.getStatus() == 0) {
            throw new BusinessException("用户不存在或已禁用");
        }
        Long jwtSchoolId = resolveJwtSchoolId(u);
        String token = jwtUtils.createToken(u.getId(), u.getRoleCode(), jwtSchoolId);
        return new LoginResponse(token, buildProfile(u));
    }

    private Long resolveJwtSchoolId(SysUser u) {
        if (Roles.ADMIN.equals(u.getRoleCode())) {
            return null;
        }
        if (Roles.SCHOOL.equals(u.getRoleCode())) {
            SysSchool school = schoolMapper.selectOne(new LambdaQueryWrapper<SysSchool>()
                    .eq(SysSchool::getUserId, u.getId()));
            return school != null ? school.getId() : u.getSchoolId();
        }
        return u.getSchoolId();
    }

    /**
     * 兼容旧版：注册时已选驾校的学员
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest req) {
        Long c = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (c != null && c > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysSchool school = schoolMapper.selectById(req.getSchoolId());
        if (school == null || school.getStatus() != null && school.getStatus() == 0) {
            throw new BusinessException("驾校不存在或已停用");
        }
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setPhone(req.getPhone());
        u.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        u.setRoleCode(Roles.STUDENT);
        u.setSchoolId(req.getSchoolId());
        u.setStatus(1);
        userMapper.insert(u);

        SysStudent st = new SysStudent();
        st.setUserId(u.getId());
        st.setSchoolId(req.getSchoolId());
        st.setEnrollDate(LocalDate.now());
        st.setLearningStage(1);
        studentMapper.insert(st);
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerStudent(RegisterStudentRequest req) {
        assertUsernameAvailable(req.getUsername());
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getUsername());
        u.setRoleCode(Roles.STUDENT);
        u.setStatus(1);
        userMapper.insert(u);

        SysStudent st = new SysStudent();
        st.setUserId(u.getId());
        st.setSchoolId(null);
        st.setLearningStage(1);
        studentMapper.insert(st);
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerCoach(RegisterCoachRequest req) {
        assertUsernameAvailable(req.getUsername());
        SysSchool school = schoolMapper.selectById(req.getSchoolId());
        if (school == null || school.getStatus() != null && school.getStatus() == 0) {
            throw new BusinessException("驾校不存在或已停用");
        }
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getUsername());
        u.setRoleCode(Roles.COACH);
        u.setSchoolId(req.getSchoolId());
        u.setStatus(1);
        userMapper.insert(u);

        SysCoach c = new SysCoach();
        c.setUserId(u.getId());
        c.setSchoolId(req.getSchoolId());
        coachMapper.insert(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerSchool(RegisterSchoolRequest req) {
        assertUsernameAvailable(req.getUsername());
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getShortName());
        u.setRoleCode(Roles.SCHOOL);
        u.setStatus(1);
        userMapper.insert(u);

        SysSchool s = new SysSchool();
        s.setName(req.getName());
        s.setShortName(req.getShortName());
        s.setCity(req.getCity());
        s.setLicenseNo(req.getName());
        s.setContactName(req.getShortName());
        s.setUserId(u.getId());
        s.setStatus(1);
        schoolMapper.insert(s);

        u.setSchoolId(s.getId());
        userMapper.updateById(u);
    }

    private void assertUsernameAvailable(String username) {
        Long c = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (c != null && c > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    public UserProfileVO profile(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        return buildProfile(u);
    }

    private UserProfileVO buildProfile(SysUser u) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setRoleCode(u.getRoleCode());
        vo.setSchoolId(resolveJwtSchoolId(u));
        if (Roles.STUDENT.equals(u.getRoleCode())) {
            SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>()
                    .eq(SysStudent::getUserId, u.getId()));
            if (st != null) {
                vo.setStudentId(st.getId());
                vo.setSchoolId(st.getSchoolId());
                vo.setLearningStage(st.getLearningStage());
                boolean en = st.getSchoolId() != null;
                vo.setEnrolled(en);
                boolean submitted = bizReviewMapper.countByStudentUserId(u.getId()) > 0;
                vo.setReviewSubmitted(submitted);
                vo.setCanSubmitReview(Boolean.TRUE.equals(en) && !submitted);
            }
        }
        if (Roles.COACH.equals(u.getRoleCode())) {
            SysCoach c = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>()
                    .eq(SysCoach::getUserId, u.getId()));
            if (c != null) {
                vo.setCoachId(c.getId());
                vo.setSchoolId(c.getSchoolId());
            }
        }
        return vo;
    }
}
