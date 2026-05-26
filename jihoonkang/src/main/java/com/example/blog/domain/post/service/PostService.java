package com.example.blog.domain.post.service;

import com.example.blog.domain.post.dto.PostCreateRequest;
import com.example.blog.domain.post.dto.PostListResponse;
import com.example.blog.domain.post.dto.PostResponse;
import com.example.blog.domain.post.dto.PostUpdateRequest;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.post.entity.PostStatus;
import com.example.blog.domain.post.repository.PostRepository;
import com.example.blog.domain.user.entity.User;
import com.example.blog.domain.user.repository.UserRepository;
import com.example.blog.global.exception.BusinessException;
import com.example.blog.global.exception.ErrorCode;
import com.example.blog.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostResponse create(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        PostStatus status = parseStatus(request.status());
        Post post = Post.of(user, request.title(), request.content(), status);
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public PostListResponse findAll(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts;
        if (status != null && !status.isBlank()) {
            posts = postRepository.findByStatus(PostStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            posts = postRepository.findAll(pageable);
        }
        List<PostResponse> items = posts.getContent().stream()
            .map(PostResponse::from)
            .toList();
        return new PostListResponse(items, page, size, posts.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PostResponse findById(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        return PostResponse.from(post);
    }

    public PostResponse update(Long userId, Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        PostStatus status = parseStatus(request.status());
        post.update(request.title(), request.content(), status);
        return PostResponse.from(post);
    }

    public void delete(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        postRepository.delete(post);
    }

    public PostResponse hidePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        post.hide();
        return PostResponse.from(post);
    }

    private PostStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return PostStatus.valueOf(status.toUpperCase());
    }
}
