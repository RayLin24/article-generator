package com.example.article.dto;

import lombok.Data;

@Data
public class StatsResponse {
    private Long articleCount;
    private Long categoryCount;
    private Long userCount;
    private Long totalViewCount;
}
