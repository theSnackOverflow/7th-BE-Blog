package com.example.blog.domain.comment.repository;

import com.example.blog.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost_IdAndParentCommentIsNull(Long postId);

    Optional<Comment> findByPost_IdAndAcceptedTrue(Long postId);
}
