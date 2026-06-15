package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.dto.SchoolPublicCardVO;
import com.jiakao.ydt.dto.SchoolPublicDetailVO;
import com.jiakao.ydt.entity.SysSchool;
import com.jiakao.ydt.mapper.BizReviewMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysSchoolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolQueryService {

    private final SysSchoolMapper schoolMapper;
    private final SysCoachMapper coachMapper;
    private final BizReviewMapper bizReviewMapper;

    @Cacheable(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_LIST, key = "'all'")
    public List<SysSchool> publicList() {
        return schoolMapper.selectList(new LambdaQueryWrapper<SysSchool>()
                .eq(SysSchool::getStatus, 1)
                .orderByAsc(SysSchool::getId));
    }

    @Cacheable(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_RANKINGS, key = "'all'")
    public List<SchoolPublicCardVO> publicRankings() {
        return schoolMapper.selectSchoolPublicRankings();
    }

    @Cacheable(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_DETAIL, key = "#id")
    public SchoolPublicDetailVO publicDetail(Long id) {
        SysSchool s = schoolMapper.selectById(id);
        if (s == null || s.getStatus() == null || s.getStatus() != 1) {
            throw new BusinessException("椹炬牎涓嶅瓨鍦ㄦ垨宸插仠鐢?");
        }
        SchoolPublicDetailVO vo = new SchoolPublicDetailVO();
        vo.setId(s.getId());
        vo.setName(s.getName());
        vo.setShortName(s.getShortName());
        vo.setCity(s.getCity());
        vo.setLicenseNo(s.getLicenseNo());
        vo.setAddress(s.getAddress());
        vo.setContactPhone(s.getContactPhone());
        vo.setContactEmail(s.getContactEmail());
        vo.setWorkHours(s.getWorkHours());
        vo.setPriceC1(s.getPriceC1());
        vo.setPriceC2(s.getPriceC2());
        vo.setRatingAvg(bizReviewMapper.avgSchoolStars(id));
        vo.setRatingCount(bizReviewMapper.countSchoolReviews(id));
        vo.setCoaches(coachMapper.selectCoachesBySchool(id));
        return vo;
    }
}
