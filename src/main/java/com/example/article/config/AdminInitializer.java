package com.example.article.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.article.entity.User;
import com.example.article.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
        initTestUser();
    }

    private void initAdminUser() {
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")
        );

        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setEmail("admin@example.com");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("管理员账号已创建: admin / admin123");
        } else {
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            log.info("管理员密码已重置: admin / admin123");
        }
    }

    private void initTestUser() {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "user")
        );

        if (user == null) {
            user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setNickname("测试用户");
            user.setEmail("user@example.com");
            user.setRole("USER");
            user.setStatus(1);
            userMapper.insert(user);
            log.info("测试用户已创建: user / user123");
        } else {
            user.setPassword(passwordEncoder.encode("user123"));
            userMapper.updateById(user);
            log.info("测试用户密码已重置: user / user123");
        }
    }
}
