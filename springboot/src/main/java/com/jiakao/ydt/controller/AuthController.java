package com.jiakao.ydt.controller;

import com.jiakao.ydt.common.R;
import com.jiakao.ydt.dto.LoginRequest;
import com.jiakao.ydt.dto.LoginResponse;
import com.jiakao.ydt.dto.RegisterCoachRequest;
import com.jiakao.ydt.dto.RegisterRequest;
import com.jiakao.ydt.dto.RegisterSchoolRequest;
import com.jiakao.ydt.dto.RegisterStudentRequest;
import com.jiakao.ydt.dto.UserProfileVO;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import com.jiakao.ydt.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证：登录、注册、当前用户
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(authService.login(req));
    }

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return R.ok();
    }

    @PostMapping("/register/student")
    public R<Void> registerStudent(@Valid @RequestBody RegisterStudentRequest req) {
        authService.registerStudent(req);
        return R.ok();
    }

    @PostMapping("/register/coach")
    public R<Void> registerCoach(@Valid @RequestBody RegisterCoachRequest req) {
        authService.registerCoach(req);
        return R.ok();
    }

    @PostMapping("/register/school")
    public R<Void> registerSchool(@Valid @RequestBody RegisterSchoolRequest req) {
        authService.registerSchool(req);
        return R.ok();
    }

    @GetMapping("/me")
    public R<UserProfileVO> me() {
        SecurityUser u = SecurityUtils.requireUser();
        return R.ok(authService.profile(u.getId()));
    }
}
