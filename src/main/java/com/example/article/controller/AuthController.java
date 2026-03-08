package com.example.article.controller;

import com.example.article.config.JwtAuthenticationFilter;
import com.example.article.dto.*;
import com.example.article.service.UserService;
import com.example.article.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            JwtAuthenticationFilter.UserPrincipal principal = new JwtAuthenticationFilter.UserPrincipal(
                    jwtUtil.getUserIdFromToken(token),
                    jwtUtil.getUsernameFromToken(token),
                    jwtUtil.getRoleFromToken(token)
            );
            UserInfoResponse userInfo = userService.getUserInfo(principal.userId());
            return ApiResponse.success(new LoginResponse(token, userInfo));
        } catch (Exception e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            return ApiResponse.success("注册成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal JwtAuthenticationFilter.UserPrincipal principal) {
        if (principal == null) {
            return ApiResponse.error(401, "未登录");
        }
        UserInfoResponse userInfo = userService.getUserInfo(principal.userId());
        return ApiResponse.success(userInfo);
    }
}
