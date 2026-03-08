package com.example.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.article.dto.CategoryCreateRequest;
import com.example.article.dto.CategoryUpdateRequest;
import com.example.article.dto.PageResult;
import com.example.article.entity.Category;
import com.example.article.mapper.ArticleMapper;
import com.example.article.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getPageShouldReturnPagedCategories() {
        Category category = new Category();
        category.setId(1L);
        category.setName("历史故事");
        category.setCode("history");

        IPage<Category> pageResult = new Page<>(2, 10, 21);
        pageResult.setRecords(List.of(category));

        when(categoryMapper.selectPage(any(Page.class), any())).thenReturn(pageResult);

        PageResult<Category> result = categoryService.getPage(2, 10);

        ArgumentCaptor<Page<Category>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(categoryMapper).selectPage(pageCaptor.capture(), any());

        assertEquals(2L, pageCaptor.getValue().getCurrent());
        assertEquals(10L, pageCaptor.getValue().getSize());
        assertEquals(1, result.getRecords().size());
        assertEquals(21L, result.getTotal());
        assertEquals(10L, result.getSize());
        assertEquals(2L, result.getCurrent());
        assertEquals(3L, result.getPages());
    }

    @Test
    void createCategoryShouldApplyDefaultsAndPersist() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("旅行攻略");
        request.setCode("travel");
        request.setPromptTemplate("请写一篇关于{topic}的旅行文章");

        when(categoryMapper.selectOne(any())).thenReturn(null);

        Category result = categoryService.createCategory(request);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).insert(categoryCaptor.capture());

        Category savedCategory = categoryCaptor.getValue();
        assertEquals("旅行攻略", savedCategory.getName());
        assertEquals("travel", savedCategory.getCode());
        assertEquals("请写一篇关于{topic}的旅行文章", savedCategory.getPromptTemplate());
        assertEquals(0, savedCategory.getSortOrder());
        assertEquals(1, savedCategory.getStatus());
        assertEquals(savedCategory, result);
    }

    @Test
    void createCategoryShouldRejectDuplicateCode() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("旅行攻略");
        request.setCode("travel");
        request.setPromptTemplate("请写一篇关于{topic}的旅行文章");

        when(categoryMapper.selectOne(any())).thenReturn(new Category());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> categoryService.createCategory(request));

        assertEquals("栏目编码已存在", exception.getMessage());
    }

    @Test
    void updateCategoryShouldPersistChanges() {
        Category existingCategory = new Category();
        existingCategory.setId(3L);
        existingCategory.setName("旧栏目");
        existingCategory.setCode("old-code");
        existingCategory.setPromptTemplate("旧模板");
        existingCategory.setSortOrder(1);
        existingCategory.setStatus(1);

        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("新栏目");
        request.setCode("new-code");
        request.setPromptTemplate("新模板 {topic}");
        request.setSortOrder(5);
        request.setStatus(0);

        when(categoryMapper.selectById(3L)).thenReturn(existingCategory);
        when(categoryMapper.selectOne(any())).thenReturn(null);

        Category result = categoryService.updateCategory(3L, request);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).updateById(categoryCaptor.capture());

        Category updatedCategory = categoryCaptor.getValue();
        assertEquals("新栏目", updatedCategory.getName());
        assertEquals("new-code", updatedCategory.getCode());
        assertEquals("新模板 {topic}", updatedCategory.getPromptTemplate());
        assertEquals(5, updatedCategory.getSortOrder());
        assertEquals(0, updatedCategory.getStatus());
        assertEquals(updatedCategory, result);
    }

    @Test
    void updateCategoryShouldRejectDuplicateCodeOwnedByAnotherCategory() {
        Category existingCategory = new Category();
        existingCategory.setId(3L);

        Category duplicateCategory = new Category();
        duplicateCategory.setId(8L);

        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("新栏目");
        request.setCode("travel");
        request.setPromptTemplate("新模板 {topic}");

        when(categoryMapper.selectById(3L)).thenReturn(existingCategory);
        when(categoryMapper.selectOne(any())).thenReturn(duplicateCategory);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> categoryService.updateCategory(3L, request));

        assertEquals("栏目编码已存在", exception.getMessage());
    }

    @Test
    void deleteCategoryShouldRejectWhenArticlesExist() {
        Category existingCategory = new Category();
        existingCategory.setId(3L);

        when(categoryMapper.selectById(3L)).thenReturn(existingCategory);
        when(articleMapper.selectCount(any())).thenReturn(2L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(3L));

        assertEquals("该栏目下存在文章，无法删除", exception.getMessage());
    }

    @Test
    void deleteCategoryShouldDeleteWhenNoArticlesExist() {
        Category existingCategory = new Category();
        existingCategory.setId(3L);

        when(categoryMapper.selectById(3L)).thenReturn(existingCategory);
        when(articleMapper.selectCount(any())).thenReturn(0L);

        categoryService.deleteCategory(3L);

        verify(categoryMapper).deleteById(3L);
    }

    @Test
    void updateCategoryStatusShouldPersistQuickToggle() {
        Category existingCategory = new Category();
        existingCategory.setId(3L);
        existingCategory.setStatus(1);

        when(categoryMapper.selectById(3L)).thenReturn(existingCategory);

        Category result = categoryService.updateCategoryStatus(3L, 0);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).updateById(categoryCaptor.capture());

        assertEquals(0, categoryCaptor.getValue().getStatus());
        assertEquals(0, result.getStatus());
    }

    @Test
    void moveCategoryUpShouldSwapDisplayOrder() {
        Category first = new Category();
        first.setId(1L);
        first.setSortOrder(1);

        Category second = new Category();
        second.setId(2L);
        second.setSortOrder(2);

        Category third = new Category();
        third.setId(3L);
        third.setSortOrder(3);

        when(categoryMapper.selectById(2L)).thenReturn(second);
        when(categoryMapper.selectList(any())).thenReturn(List.of(first, second, third));

        Category result = categoryService.moveCategoryUp(2L);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper, times(2)).updateById(categoryCaptor.capture());

        List<Category> updatedCategories = categoryCaptor.getAllValues();
        assertEquals(2L, updatedCategories.get(0).getId());
        assertEquals(1, updatedCategories.get(0).getSortOrder());
        assertEquals(1L, updatedCategories.get(1).getId());
        assertEquals(2, updatedCategories.get(1).getSortOrder());
        assertEquals(1, result.getSortOrder());
    }

    @Test
    void moveCategoryDownShouldSwapDisplayOrder() {
        Category first = new Category();
        first.setId(1L);
        first.setSortOrder(1);

        Category second = new Category();
        second.setId(2L);
        second.setSortOrder(2);

        Category third = new Category();
        third.setId(3L);
        third.setSortOrder(3);

        when(categoryMapper.selectById(2L)).thenReturn(second);
        when(categoryMapper.selectList(any())).thenReturn(List.of(first, second, third));

        Category result = categoryService.moveCategoryDown(2L);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper, times(2)).updateById(categoryCaptor.capture());

        List<Category> updatedCategories = categoryCaptor.getAllValues();
        assertEquals(3L, updatedCategories.get(0).getId());
        assertEquals(2, updatedCategories.get(0).getSortOrder());
        assertEquals(2L, updatedCategories.get(1).getId());
        assertEquals(3, updatedCategories.get(1).getSortOrder());
        assertEquals(3, result.getSortOrder());
    }
}
