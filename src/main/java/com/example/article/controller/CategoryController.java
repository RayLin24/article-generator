package com.example.article.controller;

import com.example.article.dto.ApiResponse;
import com.example.article.entity.Category;
import com.example.article.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.listEnabled());
    }

    @GetMapping("/{code}")
    public ApiResponse<Category> getByCode(@PathVariable String code) {
        Category category = categoryService.getByCode(code);
        if (category == null) {
            return ApiResponse.error(404, "栏目不存在");
        }
        return ApiResponse.success(category);
    }
}
