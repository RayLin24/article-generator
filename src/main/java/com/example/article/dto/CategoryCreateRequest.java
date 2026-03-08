package com.example.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CategoryCreateRequest {

    @NotBlank(message = "栏目名称不能为空")
    private String name;

    @NotBlank(message = "栏目编码不能为空")
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "栏目编码只能包含小写字母、数字、下划线和中划线")
    private String code;

    @NotBlank(message = "提示词模板不能为空")
    private String promptTemplate;

    private Integer sortOrder = 0;

    private Integer status = 1;
}
