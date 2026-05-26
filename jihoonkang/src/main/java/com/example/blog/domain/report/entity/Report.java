package com.example.blog.domain.report.entity;

import com.example.blog.domain.comment.entity.Comment;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.user.entity.User;
import com.example.blog.global.entity.BaseEntity;
import com.example.blog.global.exception.BusinessException;
import com.example.blog.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reports",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reporter_id", "post_id"}),
        @UniqueConstraint(columnNames = {"reporter_id", "comment_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column
    private LocalDateTime resolvedAt;

    public static Report ofPost(User reporter, Post post, String reason) {
        Report report = new Report();
        report.reporter = reporter;
        report.post = post;
        report.reason = reason;
        report.status = ReportStatus.PENDING;
        return report;
    }

    public static Report ofComment(User reporter, Comment comment, String reason) {
        Report report = new Report();
        report.reporter = reporter;
        report.comment = comment;
        report.reason = reason;
        report.status = ReportStatus.PENDING;
        return report;
    }

    public void resolve(User handler) {
        if (this.status == ReportStatus.RESOLVED) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_RESOLVED);
        }
        this.status = ReportStatus.RESOLVED;
        this.resolvedBy = handler;
        this.resolvedAt = LocalDateTime.now();
    }
}
