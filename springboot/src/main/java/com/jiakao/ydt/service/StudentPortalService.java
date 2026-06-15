package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.dto.LoginResponse;
import com.jiakao.ydt.dto.StudentEnrollRequest;
import com.jiakao.ydt.dto.StudentEnrollmentVO;
import com.jiakao.ydt.dto.StudentReviewRequest;
import com.jiakao.ydt.entity.BizReview;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysSchool;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.BizReviewMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysSchoolMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StudentPortalService {

    private final SysUserMapper userMapper;
    private final SysSchoolMapper schoolMapper;
    private final SysCoachMapper coachMapper;
    private final SysStudentMapper studentMapper;
    private final BizReviewMapper bizReviewMapper;
    private final AuthService authService;

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse enroll(Long userId, StudentEnrollRequest req) {
        SysUser u = userMapper.selectById(userId);
        if (u == null || !Roles.STUDENT.equals(u.getRoleCode())) {
            throw new BusinessException("仅学员可报名驾校");
        }
        SysSchool school = schoolMapper.selectById(req.getSchoolId());
        if (school == null || school.getStatus() != null && school.getStatus() == 0) {
            throw new BusinessException("驾校不存在或已停用");
        }
        SysCoach coach = coachMapper.selectById(req.getCoachId());
        if (coach == null || !coach.getSchoolId().equals(req.getSchoolId())) {
            throw new BusinessException("教练不属于该驾校");
        }
        SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>()
                .eq(SysStudent::getUserId, userId));
        if (st == null) {
            throw new BusinessException("学员档案不存在");
        }
        if (st.getSchoolId() != null) {
            throw new BusinessException("您已报名驾校，无需重复报名");
        }

        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setSchoolId(req.getSchoolId());
        userMapper.updateById(u);

        st.setSchoolId(req.getSchoolId());
        st.setCoachId(req.getCoachId());
        st.setEnrollDate(LocalDate.now());
        st.setLearningStage(1);
        studentMapper.updateById(st);

        return authService.refreshLogin(userId);
    }

    public StudentEnrollmentVO enrollmentInfo(Long userId) {
        SysUser u = userMapper.selectById(userId);
        StudentEnrollmentVO vo = new StudentEnrollmentVO();
        if (u == null || !Roles.STUDENT.equals(u.getRoleCode())) {
            vo.setEnrolled(false);
            return vo;
        }
        SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>()
                .eq(SysStudent::getUserId, userId));
        if (st == null || st.getSchoolId() == null) {
            vo.setEnrolled(false);
            vo.setPhone(u.getPhone());
            vo.setEmail(u.getEmail());
            return vo;
        }
        SysSchool school = schoolMapper.selectById(st.getSchoolId());
        String coachName = null;
        if (st.getCoachId() != null) {
            SysCoach c = coachMapper.selectById(st.getCoachId());
            if (c != null) {
                SysUser cu = userMapper.selectById(c.getUserId());
                coachName = cu != null ? cu.getNickname() : null;
            }
        }
        vo.setEnrolled(true);
        vo.setSchoolId(st.getSchoolId());
        vo.setSchoolName(school != null ? school.getName() : null);
        vo.setCoachId(st.getCoachId());
        vo.setCoachName(coachName);
        vo.setEnrollDate(st.getEnrollDate());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        boolean submitted = bizReviewMapper.countByStudentUserId(userId) > 0;
        vo.setReviewSubmitted(submitted);
        vo.setCanSubmitReview(!submitted);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_RANKINGS, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.REVIEWS_PUBLIC_RECENT, allEntries = true)
    })
    public void submitReview(Long userId, StudentReviewRequest req) {
        SysUser u = userMapper.selectById(userId);
        if (u == null || !Roles.STUDENT.equals(u.getRoleCode())) {
            throw new BusinessException("仅学员可评价");
        }
        SysStudent st = studentMapper.selectOne(new LambdaQueryWrapper<SysStudent>()
                .eq(SysStudent::getUserId, userId));
        if (st == null || st.getSchoolId() == null || st.getCoachId() == null) {
            throw new BusinessException("请先完成报名");
        }
        if (bizReviewMapper.countByStudentUserId(userId) > 0) {
            throw new BusinessException("您已提交过评价");
        }
        BizReview r = new BizReview();
        r.setStudentUserId(userId);
        r.setSchoolId(st.getSchoolId());
        r.setCoachId(st.getCoachId());
        r.setSchoolStars(req.getSchoolStars());
        r.setCoachStars(req.getCoachStars());
        bizReviewMapper.insert(r);
    }
}
