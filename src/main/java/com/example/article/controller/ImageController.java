package com.example.article.controller;

import com.example.article.dto.ApiResponse;
import com.example.article.service.ImageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/random")
    public ApiResponse<ImageResponse> getRandomImage(
            @RequestParam(defaultValue = "800") int width,
            @RequestParam(defaultValue = "400") int height) {
        String url = imageService.getRandomImageUrl(width, height);
        return ApiResponse.success(new ImageResponse(url, width, height));
    }

    @GetMapping("/batch")
    public ApiResponse<List<ImageResponse>> getRandomImages(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "800") int width,
            @RequestParam(defaultValue = "400") int height) {
        List<ImageResponse> images = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String url = imageService.getRandomImageUrl(width, height);
            images.add(new ImageResponse(url, width, height));
        }
        return ApiResponse.success(images);
    }

    @GetMapping("/seed/{seed}")
    public ApiResponse<ImageResponse> getImageBySeed(
            @PathVariable String seed,
            @RequestParam(defaultValue = "800") int width,
            @RequestParam(defaultValue = "400") int height) {
        String url = imageService.getImageUrlBySeed(seed, width, height);
        return ApiResponse.success(new ImageResponse(url, width, height));
    }

    @GetMapping("/cover/{articleId}")
    public ApiResponse<ImageResponse> getCoverImage(@PathVariable Long articleId) {
        String url = imageService.generateCoverImageUrl(articleId);
        return ApiResponse.success(new ImageResponse(url, 1200, 600));
    }

    @Data
    public static class ImageResponse {
        private String url;
        private int width;
        private int height;

        public ImageResponse(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }
    }
}
