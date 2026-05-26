package com.example.blog.domain.comment.service;

import com.example.blog.domain.comment.dto.CommentCreateRequest;
import com.example.blog.domain.comment.dto.CommentResponse;
import com.example.blog.domain.comment.dto.CommentUpdateRequest;
import com.example.blog.domain.comment.entity.Comment;
import com.example.blog.domain.comment.repository.CommentRepository;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.post.repository.PostRepository;
import com.example.blog.domain.user.entity.User;
import com.example.blog.domain.user.repository.UserRepository;
import com.example.blog.global.exception.BusinessException;
import com.example.blog.global.exception.ErrorCode;
import com.example.blog.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentResponse create(Long userId, Long postId, CommentCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        Comment parentComment = null;
        if (request.parentCommentId() != null) {
            parentComment = commentRepository.findById(request.parentCommentId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
            }
            if (parentComment.getParentComment() != null) {
                throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
            }
        }

        Comment comment = Comment.of(user, post, request.content(), parentComment);
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findByPostId(Long postId) {
        postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        return commentRepository.findByPost_IdAndParentCommentIsNull(postId).stream()
            .map(CommentResponse::from)
            .toList();
    }

    public CommentResponse update(Long userId, Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        commentRepository.delete(comment);
    }

    public CommentResponse acceptComment(Long userId, Long postId, Long commentId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
        }

        if (comment.isAccepted()) {
            return CommentResponse.from(comment);
        }

        commentRepository.findByPost_IdAndAcceptedTrue(postId)
            .ifPresent(Comment::unaccept);

        comment.accept();
        return CommentResponse.from(comment);
    }
}
