package com.example.article.task;

import com.example.article.entity.Category;
import com.example.article.service.ArticleService;
import com.example.article.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyArticleTask {

    private final CategoryService categoryService;
    private final ArticleService articleService;

    @Value("${schedule.enabled:true}")
    private boolean scheduleEnabled;

    @Scheduled(cron = "${schedule.cron:0 0 6 * * ?}")
    public void generateDailyArticles() {
        if (!scheduleEnabled) {
            log.info("定时任务已禁用");
            return;
        }

        log.info("开始执行每日文章生成任务...");
        List<Category> categories = categoryService.listEnabled();

        for (Category category : categories) {
            try {
                articleService.generateArticle(category);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("任务被中断", e);
            } catch (Exception e) {
                log.error("栏目 [{}] 生成文章失败", category.getName(), e);
            }
        }

        log.info("每日文章生成任务执行完成，共生成 {} 篇文章", categories.size());
    }

    public void generateArticleForCategory(Long categoryId) {
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new RuntimeException("栏目不存在");
        }
        articleService.generateArticle(category);
    }
}
