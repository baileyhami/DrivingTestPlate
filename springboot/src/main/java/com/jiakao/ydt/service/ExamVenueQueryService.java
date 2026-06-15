package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.entity.BizExamRoute;
import com.jiakao.ydt.entity.BizExamVenue;
import com.jiakao.ydt.mapper.BizExamRouteMapper;
import com.jiakao.ydt.mapper.BizExamVenueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamVenueQueryService {

    private final BizExamVenueMapper venueMapper;
    private final BizExamRouteMapper routeMapper;

    @Cacheable(cacheNames = RedisCacheConfig.VENUES_PAGE, key = "#current + ':' + #size + ':' + (#keyword == null ? '' : #keyword.trim())")
    public Page<BizExamVenue> page(long current, long size, String keyword) {
        LambdaQueryWrapper<BizExamVenue> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(BizExamVenue::getName, keyword).or().like(BizExamVenue::getAddress, keyword));
        }
        q.orderByDesc(BizExamVenue::getId);
        return venueMapper.selectPage(new Page<>(current, size), q);
    }

    @Cacheable(cacheNames = RedisCacheConfig.VENUES_ROUTES, key = "#venueId")
    public List<BizExamRoute> routes(Long venueId) {
        return routeMapper.selectList(new LambdaQueryWrapper<BizExamRoute>()
                .eq(BizExamRoute::getVenueId, venueId)
                .orderByAsc(BizExamRoute::getSortOrder));
    }
}
