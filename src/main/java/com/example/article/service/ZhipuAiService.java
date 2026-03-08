package com.example.article.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZhipuAiService {

    @Value("${zhipu.api-key}")
    private String apiKey;

    @Value("${zhipu.api-url}")
    private String apiUrl;

    @Value("${zhipu.model}")
    private String model;

    @Data
    public static class ArticleResult {
        private String title;
        private String content;
    }

    public ArticleResult generateArticle(String prompt) {
        String fullPrompt = prompt + "\n\n请以以下格式返回：\n标题：[文章标题]\n\n[文章正文内容]\n\n注意：在文章正文的合适位置插入2-4张配图，格式为[图片:图片描述关键词]，例如[图片:山水风景]、[图片:古代建筑]等。配图应该与文章内容相关，放置在段落的自然分隔处。";

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);
        requestBody.set("messages", new JSONArray()
                .put(new JSONObject()
                        .set("role", "system")
                        .set("content", "你是一位专业的内容创作者，擅长撰写各类文章。请根据用户的要求创作高质量、原创的文章。")
                )
                .put(new JSONObject()
                        .set("role", "user")
                        .set("content", fullPrompt)
                )
        );
        requestBody.set("temperature", 0.8);
        requestBody.set("max_tokens", 4096);

        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(60000)
                    .execute();

            if (!response.isOk()) {
                throw new RuntimeException("智谱AI API调用失败: " + response.getStatus() + " - " + response.body());
            }

            JSONObject responseBody = JSONUtil.parseObj(response.body());
            JSONArray choices = responseBody.getJSONArray("choices");

            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("智谱AI返回结果为空");
            }

            String content = choices.getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            return parseArticleResult(content);
        } catch (Exception e) {
            log.error("调用智谱AI失败", e);
            throw new RuntimeException("生成文章失败: " + e.getMessage(), e);
        }
    }

    private ArticleResult parseArticleResult(String content) {
        ArticleResult result = new ArticleResult();

        String[] lines = content.split("\n", 3);
        String title = "未命名文章";
        String articleContent = content;

        if (lines.length >= 1 && lines[0].startsWith("标题：")) {
            title = lines[0].substring(3).trim();
            if (lines.length >= 2) {
                articleContent = lines[1];
                if (lines.length >= 3) {
                    articleContent = lines[1] + "\n" + lines[2];
                }
            }
        } else if (lines.length >= 1 && lines[0].startsWith("标题:")) {
            title = lines[0].substring(3).trim();
            if (lines.length >= 2) {
                articleContent = lines[1];
                if (lines.length >= 3) {
                    articleContent = lines[1] + "\n" + lines[2];
                }
            }
        } else {
            int titleEnd = content.indexOf("\n");
            if (titleEnd > 0 && titleEnd < 100) {
                title = content.substring(0, titleEnd).trim();
                articleContent = content.substring(titleEnd + 1).trim();
            }
        }

        result.setTitle(title);
        result.setContent(articleContent);
        return result;
    }
}
