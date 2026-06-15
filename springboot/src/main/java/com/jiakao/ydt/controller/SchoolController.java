package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.dto.SchoolPublicCardVO;
import com.jiakao.ydt.dto.SchoolPublicDetailVO;
import com.jiakao.ydt.entity.SysSchool;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.SysSchoolMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import com.jiakao.ydt.service.SchoolQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SysSchoolMapper schoolMapper;
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SchoolQueryService schoolQueryService;

    @GetMapping("/public/list")
    public R<List<SysSchool>> publicList() {
        return R.ok(schoolQueryService.publicList());
    }

    @GetMapping("/public/rankings")
    public R<List<SchoolPublicCardVO>> publicRankings() {
        return R.ok(schoolQueryService.publicRankings());
    }

    @GetMapping("/public/{id}/detail")
    public R<SchoolPublicDetailVO> publicDetail(@PathVariable Long id) {
        return R.ok(schoolQueryService.publicDetail(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Page<SysSchool>> page(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size) {
        return R.ok(schoolMapper.selectPage(new Page<>(current, size), new LambdaQueryWrapper<>()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_LIST, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_RANKINGS, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_DETAIL, allEntries = true)
    })
    public R<Void> create(@Valid @RequestBody CreateSchoolBody body) {
        Long c = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, body.getUsername()));
        if (c != null && c > 0) {
            throw new BusinessException("Username already exists");
        }
        SysUser u = new SysUser();
        u.setUsername(body.getUsername());
        u.setPassword(passwordEncoder.encode(body.getPassword()));
        u.setPhone(body.getContactPhone());
        u.setNickname(body.getContactName());
        u.setRoleCode(Roles.SCHOOL);
        u.setStatus(1);
        userMapper.insert(u);

        SysSchool s = new SysSchool();
        s.setName(body.getName());
        s.setCity(body.getCity());
        s.setShortName(body.getShortName());
        s.setLicenseNo(body.getLicenseNo());
        s.setAddress(body.getAddress());
        s.setContactPhone(body.getContactPhone());
        s.setContactEmail(body.getContactEmail());
        s.setContactName(body.getContactName());
        s.setWorkHours(body.getWorkHours());
        s.setPriceC1(body.getPriceC1());
        s.setPriceC2(body.getPriceC2());
        s.setUserId(u.getId());
        s.setStatus(1);
        schoolMapper.insert(s);
        u.setSchoolId(s.getId());
        userMapper.updateById(u);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_LIST, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_RANKINGS, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.SCHOOLS_PUBLIC_DETAIL, key = "#id")
    })
    public R<Void> update(@PathVariable Long id, @RequestBody SysSchool school) {
        school.setId(id);
        school.setUserId(null);
        schoolMapper.updateById(school);
        return R.ok();
    }

    @Data
    public static class CreateSchoolBody {
        @NotBlank private String name;
        private String city;
        private String shortName;
        private String licenseNo;
        private String address;
        private String contactPhone;
        private String contactEmail;
        private String contactName;
        private String workHours;
        private BigDecimal priceC1;
        private BigDecimal priceC2;
        @NotBlank private String username;
        @NotBlank private String password;
    }
}
