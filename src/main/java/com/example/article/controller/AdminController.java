package com.example.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.article.dto.*;
import com.example.article.entity.Article;
import com.example.article.entity.User;
import com.example.article.mapper.UserMapper;
import com.example.article.service.ArticleService;
import com.example.article.service.CategoryService;
import com.example.article.service.UserService;
import com.example.article.task.DailyArticleTask;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DailyArticleTask dailyArticleTask;
    private final CategoryService categoryService;
    private final ArticleService articleService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/articles/generate")
    public ApiResponse<String> generateAllArticles() {
        try {
            dailyArticleTask.generateDailyArticles();
            return ApiResponse.success("文章生成任务已执行");
        } catch (Exception e) {
            return ApiResponse.error("生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/articles/generate/{categoryId}")
    public ApiResponse<String> generateArticleForCategory(@PathVariable Long categoryId) {
        try {
            if (categoryService.getById(categoryId) == null) {
                return ApiResponse.error(404, "栏目不存在");
            }
            dailyArticleTask.generateArticleForCategory(categoryId);
            return ApiResponse.success("文章生成成功");
        } catch (Exception e) {
            return ApiResponse.error("生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<Article>> listArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(articleService.getAdminPage(page, size));
    }

    @GetMapping("/categories")
    public ApiResponse<java.util.List<com.example.article.entity.Category>> listCategories() {
        return ApiResponse.success(categoryService.listAll());
    }

    @PostMapping("/categories")
    public ApiResponse<com.example.article.entity.Category> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        try {
            com.example.article.entity.Category category = categoryService.createCategory(request);
            return ApiResponse.success("创建成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<com.example.article.entity.Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
        try {
            com.example.article.entity.Category category = categoryService.updateCategory(id, request);
            return ApiResponse.success("更新成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/categories/{id}/status")
    public ApiResponse<com.example.article.entity.Category> updateCategoryStatus(@PathVariable Long id, @Valid @RequestBody CategoryStatusUpdateRequest request) {
        try {
            com.example.article.entity.Category category = categoryService.updateCategoryStatus(id, request.getStatus());
            return ApiResponse.success("更新成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/categories/{id}/move-up")
    public ApiResponse<com.example.article.entity.Category> moveCategoryUp(@PathVariable Long id) {
        try {
            com.example.article.entity.Category category = categoryService.moveCategoryUp(id);
            return ApiResponse.success("上移成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/categories/{id}/move-down")
    public ApiResponse<com.example.article.entity.Category> moveCategoryDown(@PathVariable Long id) {
        try {
            com.example.article.entity.Category category = categoryService.moveCategoryDown(id);
            return ApiResponse.success("下移成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/articles")
    public ApiResponse<Article> createArticle(@Valid @RequestBody ArticleCreateRequest request) {
        try {
            Article article = articleService.createArticle(request);
            return ApiResponse.success(article);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<Article> updateArticle(@PathVariable Long id, @RequestBody ArticleUpdateRequest request) {
        try {
            Article article = articleService.updateArticle(id, request);
            return ApiResponse.success(article);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<String> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        StatsResponse stats = new StatsResponse();
        stats.setArticleCount(articleService.getAdminPage(1, 1).getTotal());
        stats.setCategoryCount((long) categoryService.listAll().size());
        stats.setUserCount(userMapper.selectCount(null));
        stats.setTotalViewCount(articleService.getTotalViewCount());
        return ApiResponse.success(stats);
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            wrapper.like(User::getUsername, username).or().like(User::getNickname, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @PostMapping("/users")
    public ApiResponse<User> createUser(@RequestBody User user) {
        try {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
            );
            if (count > 0) {
                return ApiResponse.error("用户名已存在");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRole() == null) {
                user.setRole("USER");
            }
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            userMapper.insert(user);
            user.setPassword(null);
            return ApiResponse.success("创建成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User existing = userMapper.selectById(id);
            if (existing == null) {
                return ApiResponse.error(404, "用户不存在");
            }
            if (user.getUsername() != null) {
                existing.setUsername(user.getUsername());
            }
            if (user.getNickname() != null) {
                existing.setNickname(user.getNickname());
            }
            if (user.getEmail() != null) {
                existing.setEmail(user.getEmail());
            }
            if (user.getAvatar() != null) {
                existing.setAvatar(user.getAvatar());
            }
            if (user.getRole() != null) {
                existing.setRole(user.getRole());
            }
            if (user.getStatus() != null) {
                existing.setStatus(user.getStatus());
            }
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            userMapper.updateById(existing);
            existing.setPassword(null);
            return ApiResponse.success("更新成功", existing);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }
            if ("ADMIN".equals(user.getRole())) {
                return ApiResponse.error("不能删除管理员账户");
            }
            userMapper.deleteById(id);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
