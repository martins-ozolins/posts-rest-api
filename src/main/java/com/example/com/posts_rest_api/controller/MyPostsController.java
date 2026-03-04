package com.example.com.posts_rest_api.controller;

import com.example.com.posts_rest_api.dto.PostDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.com.posts_rest_api.dto.PostDtos.*;

import com.example.com.posts_rest_api.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/posts")
public class MyPostsController {

    private final PostService postService;

    public MyPostsController(PostService postService) {
        this.postService = postService;
    }

    // CREATE (auth required)
    @PostMapping
    public ResponseEntity<PostPublicResponse> create(
            @Valid @RequestBody PostCreateRequest req,
            @AuthenticationPrincipal UserDetails principal
    ) {
        PostPublicResponse res = postService.createPost(req, principal.getUsername());
        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostPrivateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest req,
            @AuthenticationPrincipal UserDetails principal
    ) {
        PostPrivateResponse res = postService.updatePost(req, id, principal.getUsername());
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public List<PostPrivateResponse> myPosts(@AuthenticationPrincipal UserDetails principal) {
        return postService.getUserPosts(principal.getUsername());
    }

    @GetMapping("/{id}")
    public PostPrivateResponse myPostById(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails principal) {
        return postService.getMyPostById(id, principal.getUsername());
    }


}
