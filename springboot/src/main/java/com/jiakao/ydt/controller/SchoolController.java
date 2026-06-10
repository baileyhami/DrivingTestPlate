package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.dto.SchoolPublicCardVO;
import com.jiakao.ydt.dto.SchoolPublicDetailVO;
import com.jiakao.ydt.entity.SysSchool;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.BizReviewMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysSchoolMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 驾校管理 / 公开选项
 */
@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SysSchoolMapper schoolMapper;
    private final SysUserMapper userMapper;
    private final SysCoachMapper coachMapper;
    private final BizReviewMapper bizReviewMapper;
    private final PasswordEncoder passwordEncoder;

    /** 注册页下拉：仅返回正常驾校 */
    @GetMapping("/public/list")
    public R<List<SysSchool>> publicList() {
        List<SysSchool> list = schoolMapper.selectList(new LambdaQueryWrapper<SysSchool>()
                .eq(SysSchool::getStatus, 1)
                .orderByAsc(SysSchool::getId));
        return R.ok(list);
    }

    @GetMapping("/public/rankings")
    public R<List<SchoolPublicCardVO>> publicRankings() {
        return R.ok(schoolMapper.selectSchoolPublicRankings());
    }

    @GetMapping("/public/{id}/detail")
    public R<SchoolPublicDetailVO> publicDetail(@PathVariable Long id) {
        SysSchool s = schoolMapper.selectById(id);
        if (s == null || s.getStatus() == null || s.getStatus() != 1) {
            throw new BusinessException("驾校不存在或已停用");
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
        return R.ok(vo);
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
    public R<Void> create(@Valid @RequestBody CreateSchoolBody body) {
        Long c = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, body.getUsername()));
        if (c != null && c > 0) {
            throw new BusinessException("登录名已存在");
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
