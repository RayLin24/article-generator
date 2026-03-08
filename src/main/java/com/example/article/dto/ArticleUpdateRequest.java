package com.example.article.dto;

import lombok.Data;

@Data
public class ArticleUpdateRequest {

    private Long categoryId;

    private String title;

    private String content;

    private String summary;

    private String coverImage;

    private Integer status;

    private String contentType;
}
