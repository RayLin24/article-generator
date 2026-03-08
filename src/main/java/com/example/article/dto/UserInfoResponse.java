package com.example.article.dto;

import lombok.Data;

@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String role;
}
