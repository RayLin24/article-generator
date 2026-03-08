package com.example.article.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ImageService {

    @Value("${picsum.base-url}")
    private String baseUrl;

    @Value("${picsum.default-width}")
    private int defaultWidth;

    @Value("${picsum.default-height}")
    private int defaultHeight;

    private static final Pattern IMAGE_MARKER_PATTERN = Pattern.compile("\\[图片:([^\\]]+)\\]");
    private static final Random RANDOM = new Random();

    /**
     * 获取随机图片URL
     */
    public String getRandomImageUrl() {
        return getRandomImageUrl(defaultWidth, defaultHeight);
    }

    /**
     * 获取指定尺寸的随机图片URL
     */
    public String getRandomImageUrl(int width, int height) {
        int seed = RANDOM.nextInt(10000);
        return String.format("%s/%d/%d?random=%d", baseUrl, width, height, seed);
    }

    /**
     * 根据种子获取固定图片URL（同一seed返回同一图片）
     * 用于封面图，保证每次刷新看到相同图片
     */
    public String getImageUrlBySeed(String seed) {
        return getImageUrlBySeed(seed, defaultWidth, defaultHeight);
    }

    /**
     * 根据种子获取指定尺寸的固定图片URL
     */
    public String getImageUrlBySeed(String seed, int width, int height) {
        int hash = Math.abs(seed.hashCode() % 10000);
        return String.format("%s/seed/%s/%d/%d", baseUrl, hash, width, height);
    }

    /**
     * 替换文章内容中的[图片:关键词]标记为实际图片URL
     */
    public String replaceImageMarkers(String content, Long articleId) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        Matcher matcher = IMAGE_MARKER_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        int imageIndex = 0;

        while (matcher.find()) {
            String keyword = matcher.group(1);
            String imageUrl = generateImageUrl(articleId, imageIndex);
            
            String imgTag = String.format("\n\n![%s](%s)\n\n", keyword, imageUrl);
            matcher.appendReplacement(result, imgTag);
            imageIndex++;
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 生成文章封面图URL
     */
    public String generateCoverImageUrl(Long articleId) {
        return getImageUrlBySeed("cover_" + articleId, 1200, 600);
    }

    /**
     * 生成正文配图URL
     */
    private String generateImageUrl(Long articleId, int index) {
        return getImageUrlBySeed("content_" + articleId + "_" + index, 800, 400);
    }
}
