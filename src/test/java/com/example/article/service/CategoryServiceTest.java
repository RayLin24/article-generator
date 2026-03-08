package com.example.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.article.dto.PageResult;
import com.example.article.entity.Category;
import com.example.article.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getEnabledPageShouldReturnPagedEnabledCategories() {
        Category category = new Category();
        category.setId(1L);
        category.setName("历史故事");
        category.setCode("history");

        IPage<Category> pageResult = new Page<>(2, 10, 21);
        pageResult.setRecords(List.of(category));

        when(categoryMapper.selectPage(any(Page.class), any())).thenReturn(pageResult);

        PageResult<Category> result = categoryService.getEnabledPage(2, 10);

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
}
