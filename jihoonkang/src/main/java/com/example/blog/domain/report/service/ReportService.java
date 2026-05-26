package com.example.blog.domain.report.service;

import com.example.blog.domain.comment.entity.Comment;
import com.example.blog.domain.comment.repository.CommentRepository;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.post.repository.PostRepository;
import com.example.blog.domain.report.dto.ReportCommentRequest;
import com.example.blog.domain.report.dto.ReportPostRequest;
import com.example.blog.domain.report.dto.ReportResponse;
import com.example.blog.domain.report.entity.Report;
import com.example.blog.domain.report.repository.ReportRepository;
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
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public ReportResponse reportPost(Long reporterId, Long postId, ReportPostRequest request) {
        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        if (post.getUser().getId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.CANNOT_REPORT_SELF);
        }
        if (reportRepository.existsByReporter_IdAndPost_Id(reporterId, postId)) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = Report.ofPost(reporter, post, request.reason());
        return ReportResponse.from(reportRepository.save(report));
    }

    public ReportResponse reportComment(Long reporterId, Long commentId, ReportCommentRequest request) {
        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getUser().getId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.CANNOT_REPORT_SELF);
        }
        if (reportRepository.existsByReporter_IdAndComment_Id(reporterId, commentId)) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = Report.ofComment(reporter, comment, request.reason());
        return ReportResponse.from(reportRepository.save(report));
    }

    public ReportResponse resolveReport(Long handlerId, Long reportId) {
        User handler = userRepository.findById(handlerId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.INVALID_REPORT_TARGET));

        report.resolve(handler);
        return ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findAll() {
        return reportRepository.findAll().stream()
            .map(ReportResponse::from)
            .toList();
    }
}
