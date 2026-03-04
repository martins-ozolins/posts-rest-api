package com.example.com.posts_rest_api.service;


import com.example.com.posts_rest_api.dto.PostDtos.*;
import com.example.com.posts_rest_api.exceptions.NotFoundException;
import com.example.com.posts_rest_api.model.Post;
import com.example.com.posts_rest_api.model.User;
import com.example.com.posts_rest_api.repository.PostRepository;
import com.example.com.posts_rest_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public PostPublicResponse createPost(PostCreateRequest request, String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setTitle(request.title());
        post.setSubtitle(request.subtitle());
        post.setDescription(request.description());
        post.setOwner(owner);

        Post saved = postRepository.save(post);
        return toPublicResponse(saved);
    }

    // Public: everyone can see
    public List<PostPublicResponse> getAll() {
        return postRepository.findAll()
                .stream()
                .map(this::toPublicResponse)
                .toList();
    }

    // Public: everyone can see
    public PostPublicResponse getById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        return toPublicResponse(post);
    }

    // Private: only the logged-in user’s posts
    public List<PostPrivateResponse> getUserPosts(String email) {
        return postRepository.findByOwnerEmail(email)
                .stream()
                .map(this::toPrivateResponse)
                .toList();
    }

    // Private: must belong to the logged-in user
    public PostPrivateResponse getMyPostById(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getOwner() == null || !email.equals(post.getOwner().getEmail())) {
            throw new NotFoundException("Post not found");
        }

        return toPrivateResponse(post);
    }

    // Private: must belong to the logged-in user
    public PostPrivateResponse updatePost(PostUpdateRequest postUpdateRequest, Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getOwner() == null || !email.equals(post.getOwner().getEmail())) {
            throw new RuntimeException("Post not found");
        }

        post.setTitle(postUpdateRequest.title());
        post.setSubtitle(postUpdateRequest.subtitle());
        post.setDescription(postUpdateRequest.description());

        Post saved = postRepository.save(post);
        return toPrivateResponse(saved);
    }

    private PostPublicResponse toPublicResponse(Post p) {
        return new PostPublicResponse(
                p.getId(),
                p.getTitle(),
                p.getSubtitle(),
                p.getDescription(),
                p.getCreatedAt()
        );
    }

    private PostPrivateResponse toPrivateResponse(Post p) {
        return new PostPrivateResponse(
                p.getId(),
                p.getTitle(),
                p.getSubtitle(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getOwner() != null ? p.getOwner().getId() : null,
                p.getOwner() != null ? p.getOwner().getEmail() : null
        );
    }
}