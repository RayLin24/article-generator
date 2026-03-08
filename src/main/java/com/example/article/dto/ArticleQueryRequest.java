package com.example.article.dto;

import lombok.Data;

@Data
public class ArticleQueryRequest {

    private Integer page = 1;
    private Integer size = 10;
    private String categoryCode;
}
