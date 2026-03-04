package com.example.com.posts_rest_api.repository;

import com.example.com.posts_rest_api.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long>  {
    List<Post> findByOwnerId(UUID ownerId);
    List<Post> findByOwnerEmail(String email);
}
