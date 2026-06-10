package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.ResultCode;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.dto.GeoPointDto;
import com.jiakao.ydt.dto.RoutePlanningResultDto;
import com.jiakao.ydt.entity.BizExamRoute;
import com.jiakao.ydt.entity.BizExamVenue;
import com.jiakao.ydt.mapper.BizExamRouteMapper;
import com.jiakao.ydt.mapper.BizExamVenueMapper;
import com.jiakao.ydt.service.AmapDirectionService;
import com.jiakao.ydt.service.AmapGeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考场与线路图
 */
@RestController
@RequestMapping("/exam-venues")
@RequiredArgsConstructor
public class ExamVenueController {

    private final BizExamVenueMapper venueMapper;
    private final BizExamRouteMapper routeMapper;
    private final AmapGeocodeService amapGeocodeService;
    private final AmapDirectionService amapDirectionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public R<Page<BizExamVenue>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "100") long size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizExamVenue> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(BizExamVenue::getName, keyword).or().like(BizExamVenue::getAddress, keyword));
        }
        q.orderByDesc(BizExamVenue::getId);
        return R.ok(venueMapper.selectPage(new Page<>(current, size), q));
    }

    /**
     * 根据地址调用高德地理编码，用于维护考场坐标或地图展示（路径避免与 /{venueId} 冲突）。
     */
    @GetMapping("/address-geocode")
    @PreAuthorize("isAuthenticated()")
    public R<GeoPointDto> geocode(@RequestParam String address) {
        return amapGeocodeService.geocode(address)
                .map(R::ok)
                .orElseGet(() -> R.fail(ResultCode.BAD_REQUEST.getCode(), "地理编码失败，请检查地址或高德 Key 配置"));
    }

    @GetMapping("/{venueId}/routes")
    @PreAuthorize("isAuthenticated()")
    public R<List<BizExamRoute>> routes(@PathVariable Long venueId) {
        List<BizExamRoute> list = routeMapper.selectList(new LambdaQueryWrapper<BizExamRoute>()
                .eq(BizExamRoute::getVenueId, venueId)
                .orderByAsc(BizExamRoute::getSortOrder));
        return R.ok(list);
    }

    /**
     * 根据线路锚点调用高德 Web 路径规划（驾车优先，失败则步行分段），返回可绘制折线。
     */
    @GetMapping("/routes/{routeId}/planned-path")
    @PreAuthorize("isAuthenticated()")
    public R<RoutePlanningResultDto> plannedPath(@PathVariable Long routeId) {
        BizExamRoute route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        RoutePlanningResultDto dto = amapDirectionService.planFromRoutePathJson(route.getRoutePathJson());
        if ("ERROR".equals(dto.getPlanMode())) {
            return R.fail(ResultCode.BAD_REQUEST.getCode(), dto.getAmapInfo());
        }
        return R.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> createVenue(@RequestBody BizExamVenue v) {
        venueMapper.insert(v);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> updateVenue(@PathVariable Long id, @RequestBody BizExamVenue v) {
        v.setId(id);
        venueMapper.updateById(v);
        return R.ok();
    }

    @PostMapping("/{venueId}/routes")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> createRoute(@PathVariable Long venueId, @RequestBody BizExamRoute r) {
        r.setVenueId(venueId);
        routeMapper.insert(r);
        return R.ok();
    }

    @PutMapping("/routes/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> updateRoute(@PathVariable Long routeId, @RequestBody BizExamRoute r) {
        r.setId(routeId);
        routeMapper.updateById(r);
        return R.ok();
    }

    @DeleteMapping("/routes/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> deleteRoute(@PathVariable Long routeId) {
        routeMapper.deleteById(routeId);
        return R.ok();
    }
}
