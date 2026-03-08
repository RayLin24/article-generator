package com.example.article.controller;

import com.example.article.dto.ApiResponse;
import com.example.article.dto.ArticleQueryRequest;
import com.example.article.dto.PageResult;
import com.example.article.entity.Article;
import com.example.article.service.ArticleService;
import com.example.article.task.DailyArticleTask;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final DailyArticleTask dailyArticleTask;

    @GetMapping
    public ApiResponse<PageResult<Article>> list(ArticleQueryRequest request) {
        PageResult<Article> result;
        if (request.getCategoryCode() != null && !request.getCategoryCode().isEmpty()) {
            result = articleService.getPageByCategoryCode(
                    request.getCategoryCode(),
                    request.getPage(),
                    request.getSize()
            );
        } else {
            result = articleService.getPage(request.getPage(), request.getSize());
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Article> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        return ApiResponse.success(article);
    }

    @GetMapping("/category/{code}")
    public ApiResponse<PageResult<Article>> listByCategory(
            @PathVariable String code,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(articleService.getPageByCategoryCode(code, page, size));
    }
}
