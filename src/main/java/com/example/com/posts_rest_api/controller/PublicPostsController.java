package com.example.com.posts_rest_api.controller;

import com.example.com.posts_rest_api.dto.PostDtos.PostPublicResponse;
import com.example.com.posts_rest_api.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PublicPostsController {

    private final PostService postService;

    public PublicPostsController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostPublicResponse> getAll() {
        return postService.getAll();
    }

    @GetMapping("/{id}")
    public PostPublicResponse getById(@PathVariable Long id) {
        return postService.getById(id);
    }

}