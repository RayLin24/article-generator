package com.example.article.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.article.dto.PageResult;
import com.example.article.entity.Article;
import com.example.article.entity.Category;
import com.example.article.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryService categoryService;
    private final ZhipuAiService zhipuAiService;
    private final ImageService imageService;

    public PageResult<Article> getPage(int page, int size) {
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPageWithCategory(pageParam);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    public PageResult<Article> getPageByCategoryCode(String code, int page, int size) {
        Category category = categoryService.getByCode(code);
        if (category == null) {
            return PageResult.of(List.of(), 0, size, page);
        }
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPageByCategoryId(pageParam, category.getId());
        return PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    public Article getById(Long id) {
        Article article = articleMapper.selectArticleWithCategory(id);
        if (article != null) {
            articleMapper.updateById(new Article() {{
                setId(id);
                setViewCount(article.getViewCount() + 1);
            }});
        }
        return article;
    }

    public List<Article> getLatestByCategory(Long categoryId, int limit) {
        return articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getCategoryId, categoryId)
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getPublishDate)
                        .last("LIMIT " + limit)
        );
    }

    public void generateArticle(Category category) {
        try {
            log.info("开始为栏目 [{}] 生成文章", category.getName());

            String[] topics = getTopicsForCategory(category.getCode());
            String topic = topics[(int) (Math.random() * topics.length)];
            String prompt = category.getPromptTemplate().replace("{topic}", topic);

            ZhipuAiService.ArticleResult result = zhipuAiService.generateArticle(prompt);

            Article article = new Article();
            article.setCategoryId(category.getId());
            article.setTitle(result.getTitle());
            article.setContent(result.getContent());
            article.setSummary(generateSummary(result.getContent()));
            article.setPublishDate(LocalDate.now());
            article.setViewCount(0);
            article.setStatus(1);

            articleMapper.insert(article);

            Long articleId = article.getId();
            String processedContent = imageService.replaceImageMarkers(result.getContent(), articleId);
            String coverImageUrl = imageService.generateCoverImageUrl(articleId);

            article.setContent(processedContent);
            article.setCoverImage(coverImageUrl);
            articleMapper.updateById(article);

            log.info("栏目 [{}] 文章生成成功: {}", category.getName(), result.getTitle());
        } catch (Exception e) {
            log.error("栏目 [{}] 文章生成失败", category.getName(), e);
        }
    }

    private String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String cleaned = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return cleaned.length() > 200 ? cleaned.substring(0, 200) + "..." : cleaned;
    }

    private String[] getTopicsForCategory(String code) {
        return switch (code) {
            case "history" -> new String[]{"三国演义", "唐朝盛世", "丝绸之路", "秦始皇统一六国", "明朝航海", "清朝康熙", "战国七雄", "汉武帝", "宋朝文化", "元朝疆域"};
            case "love" -> new String[]{"校园初恋", "异地恋", "青梅竹马", "一见钟情", "相伴一生", "跨越时空", "战地爱情", "都市情缘", "书香门第", "江湖侠侣"};
            case "fable" -> new String[]{"勤奋与懒惰", "诚实与欺骗", "知足常乐", "团结力量", "谦虚与骄傲", "智慧与力量", "耐心与急躁", "友谊", "勇气", "感恩"};
            case "news" -> new String[]{"社会热点", "国际局势", "经济动态", "民生关注", "教育改革", "环境保护", "文化传承", "体育赛事", "医疗健康", "科技创新"};
            case "tech" -> new String[]{"人工智能", "量子计算", "元宇宙", "新能源", "生物科技", "太空探索", "自动驾驶", "区块链", "5G通信", "芯片技术"};
            default -> new String[]{"综合话题"};
        };
    }

    public Article createArticle(com.example.article.dto.ArticleCreateRequest request) {
        Article article = new Article();
        article.setCategoryId(request.getCategoryId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary() != null ? request.getSummary() : generateSummary(request.getContent()));
        article.setCoverImage(request.getCoverImage());
        article.setPublishDate(LocalDate.now());
        article.setViewCount(0);
        article.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        articleMapper.insert(article);
        return article;
    }

    public Article updateArticle(Long id, com.example.article.dto.ArticleUpdateRequest request) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        if (request.getCategoryId() != null) {
            article.setCategoryId(request.getCategoryId());
        }
        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
            article.setSummary(request.getSummary() != null ? request.getSummary() : generateSummary(request.getContent()));
        }
        if (request.getCoverImage() != null) {
            article.setCoverImage(request.getCoverImage());
        }
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }

        articleMapper.updateById(article);
        return article;
    }

    public void deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        articleMapper.deleteById(id);
    }

    public PageResult<Article> getAdminPage(int page, int size) {
        Page<Article> pageParam = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPageWithCategory(pageParam);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }
}
