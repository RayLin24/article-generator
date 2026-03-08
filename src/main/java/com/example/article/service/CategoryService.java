package com.example.article.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.article.entity.Category;
import com.example.article.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
        );
    }

    public List<Category> listEnabled() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSortOrder)
        );
    }

    public Category getByCode(String code) {
        return categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getCode, code)
        );
    }

    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }
}
