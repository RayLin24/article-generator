package com.example.article.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.article.dto.CategoryCreateRequest;
import com.example.article.dto.CategoryUpdateRequest;
import com.example.article.entity.Article;
import com.example.article.dto.PageResult;
import com.example.article.entity.Category;
import com.example.article.mapper.ArticleMapper;
import com.example.article.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId)
        );
    }

    public List<Category> listEnabled() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId)
        );
    }

    public PageResult<Category> getPage(int page, int size) {
        Page<Category> pageParam = new Page<>(page, size);
        IPage<Category> result = categoryMapper.selectPage(
                pageParam,
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId)
        );
        return PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
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

    public Category createCategory(CategoryCreateRequest request) {
        validateUniqueCode(null, request.getCode());

        Category category = new Category();
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setPromptTemplate(request.getPromptTemplate());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        categoryMapper.insert(category);
        return category;
    }

    public Category updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("栏目不存在");
        }

        validateUniqueCode(id, request.getCode());

        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setPromptTemplate(request.getPromptTemplate());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        categoryMapper.updateById(category);
        return category;
    }

    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("栏目不存在");
        }

        Long articleCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getCategoryId, id)
        );
        if (articleCount != null && articleCount > 0) {
            throw new RuntimeException("该栏目下存在文章，无法删除");
        }

        categoryMapper.deleteById(id);
    }

    public Category updateCategoryStatus(Long id, Integer status) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("栏目不存在");
        }

        category.setStatus(status);
        categoryMapper.updateById(category);
        return category;
    }

    public Category moveCategoryUp(Long id) {
        return moveCategory(id, -1);
    }

    public Category moveCategoryDown(Long id) {
        return moveCategory(id, 1);
    }

    private void validateUniqueCode(Long currentId, String code) {
        Category existingCategory = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getCode, code)
        );
        if (existingCategory != null && !Objects.equals(existingCategory.getId(), currentId)) {
            throw new RuntimeException("栏目编码已存在");
        }
    }

    private Category moveCategory(Long id, int offset) {
        Category currentCategory = categoryMapper.selectById(id);
        if (currentCategory == null) {
            throw new RuntimeException("栏目不存在");
        }

        List<Category> categories = listAll();
        int currentIndex = -1;
        for (int index = 0; index < categories.size(); index++) {
            if (Objects.equals(categories.get(index).getId(), id)) {
                currentIndex = index;
                break;
            }
        }

        if (currentIndex < 0) {
            throw new RuntimeException("栏目不存在");
        }

        int targetIndex = currentIndex + offset;
        if (targetIndex < 0 || targetIndex >= categories.size()) {
            return currentCategory;
        }

        Category targetCategory = categories.get(targetIndex);
        categories.set(targetIndex, currentCategory);
        categories.set(currentIndex, targetCategory);

        for (int index = 0; index < categories.size(); index++) {
            Category category = categories.get(index);
            int newSortOrder = index + 1;
            if (!Objects.equals(category.getSortOrder(), newSortOrder)) {
                category.setSortOrder(newSortOrder);
                categoryMapper.updateById(category);
            }
        }

        return currentCategory;
    }
}
