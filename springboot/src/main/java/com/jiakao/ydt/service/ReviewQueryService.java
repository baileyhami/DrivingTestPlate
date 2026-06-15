package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.dto.ReviewPublicVO;
import com.jiakao.ydt.entity.BizReview;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysSchool;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.BizReviewMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysSchoolMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

    private final BizReviewMapper reviewMapper;
    private final SysSchoolMapper schoolMapper;
    private final SysCoachMapper coachMapper;
    private final SysUserMapper userMapper;

    @Cacheable(cacheNames = RedisCacheConfig.REVIEWS_PUBLIC_RECENT, key = "#limit")
    public List<ReviewPublicVO> recentPublic(int limit) {
        int n = Math.min(Math.max(limit, 1), 50);
        List<BizReview> rows = reviewMapper.selectList(new LambdaQueryWrapper<BizReview>()
                .orderByDesc(BizReview::getCreateTime)
                .last("LIMIT " + n));
        if (rows.isEmpty()) {
            return List.of();
        }
        Set<Long> schoolIds = new HashSet<>();
        Set<Long> coachIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (BizReview r : rows) {
            if (r.getSchoolId() != null) schoolIds.add(r.getSchoolId());
            if (r.getCoachId() != null) coachIds.add(r.getCoachId());
            if (r.getStudentUserId() != null) userIds.add(r.getStudentUserId());
        }
        Map<Long, String> schoolNames = schoolMapper.selectList(
                        new LambdaQueryWrapper<SysSchool>().in(SysSchool::getId, schoolIds))
                .stream().collect(Collectors.toMap(SysSchool::getId, SysSchool::getName, (a, b) -> a));
        List<SysCoach> coaches = coachMapper.selectList(
                new LambdaQueryWrapper<SysCoach>().in(SysCoach::getId, coachIds));
        Set<Long> coachUserIds = coaches.stream().map(SysCoach::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userNick = loadNick(userIds);
        Map<Long, String> coachUserNick = loadNick(coachUserIds);
        Map<Long, String> coachIdToName = new HashMap<>();
        for (SysCoach c : coaches) {
            if (c.getUserId() != null) {
                coachIdToName.put(c.getId(), coachUserNick.getOrDefault(c.getUserId(), "教练"));
            }
        }
        List<ReviewPublicVO> out = new ArrayList<>();
        for (BizReview r : rows) {
            ReviewPublicVO vo = new ReviewPublicVO();
            vo.setSchoolName(schoolNames.getOrDefault(r.getSchoolId(), "驾校"));
            vo.setCoachName(coachIdToName.getOrDefault(r.getCoachId(), "教练"));
            vo.setSchoolStars(r.getSchoolStars());
            vo.setCoachStars(r.getCoachStars());
            vo.setCreateTime(r.getCreateTime());
            String nick = userNick.getOrDefault(r.getStudentUserId(), "学员");
            vo.setReviewerLabel(maskName(nick));
            out.add(vo);
        }
        return out;
    }

    private Map<Long, String> loadNick(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds))
                .stream().collect(Collectors.toMap(SysUser::getId, u -> {
                    String n = u.getNickname();
                    if (n == null || n.isBlank()) {
                        n = u.getUsername();
                    }
                    return n != null ? n : "学员";
                }, (a, b) -> a));
    }

    private static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "学员";
        }
        if (name.length() == 1) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "**";
    }
}
