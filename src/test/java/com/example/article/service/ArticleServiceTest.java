package com.example.article.service;

import com.example.article.entity.Article;
import com.example.article.entity.Category;
import com.example.article.mapper.ArticleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ZhipuAiService zhipuAiService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void generateArticleShouldUseCategorySpecificTopicForCustomCategory() {
        Category category = new Category();
        category.setId(9L);
        category.setCode("travel");
        category.setName("旅行攻略");
        category.setPromptTemplate("请写一篇关于{topic}的旅行文章");

        ZhipuAiService.ArticleResult result = new ZhipuAiService.ArticleResult();
        result.setTitle("旅行标题");
        result.setContent("旅行内容");

        when(zhipuAiService.generateArticle(any())).thenReturn(result);
        when(articleMapper.insert(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            article.setId(1L);
            return 1;
        });
        when(imageService.replaceImageMarkers("旅行内容", 1L)).thenReturn("旅行内容");
        when(imageService.generateCoverImageUrl(1L)).thenReturn("cover");
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);

        articleService.generateArticle(category);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(zhipuAiService).generateArticle(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertTrue(prompt.contains("旅行攻略"));
        assertFalse(prompt.contains("综合话题"));
    }
}
