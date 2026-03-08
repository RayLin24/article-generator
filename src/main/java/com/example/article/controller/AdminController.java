package com.example.article.controller;

import com.example.article.dto.*;
import com.example.article.entity.Article;
import com.example.article.service.ArticleService;
import com.example.article.service.CategoryService;
import com.example.article.task.DailyArticleTask;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DailyArticleTask dailyArticleTask;
    private final CategoryService categoryService;
    private final ArticleService articleService;

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
}
