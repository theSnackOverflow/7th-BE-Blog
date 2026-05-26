package com.example.blog.domain.comment.dto;

import com.example.blog.domain.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "댓글 응답")
public record CommentResponse(
    @Schema(description = "댓글 ID", example = "1") Long commentId,
    @Schema(description = "게시글 ID", example = "1") Long postId,
    @Schema(description = "작성자 ID", example = "1") Long userId,
    @Schema(description = "작성자 이름", example = "지훈") String username,
    @Schema(description = "댓글 내용", example = "좋은 글이네요!") String content,
    @Schema(description = "채택 여부", example = "false") boolean accepted,
    @Schema(description = "부모 댓글 ID (최상위 댓글이면 null)", nullable = true, example = "null") Long parentCommentId,
    @Schema(description = "대댓글 목록", nullable = true) List<CommentResponse> replies,
    @Schema(description = "생성 시각", example = "2026-05-04T10:30:00") LocalDateTime createdAt
) {

    public static CommentResponse from(Comment comment) {
        List<CommentResponse> replies = comment.getReplies().stream()
            .map(CommentResponse::from)
            .toList();
        return new CommentResponse(
            comment.getId(),
            comment.getPost().getId(),
            comment.getUser().getId(),
            comment.getUser().getUsername(),
            comment.getContent(),
            comment.isAccepted(),
            comment.getParentComment() != null ? comment.getParentComment().getId() : null,
            replies,
            comment.getCreatedAt()
        );
    }
}
