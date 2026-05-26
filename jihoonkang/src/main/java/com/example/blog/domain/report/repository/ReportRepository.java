package com.example.blog.domain.report.repository;

import com.example.blog.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporter_IdAndPost_Id(Long reporterId, Long postId);

    boolean existsByReporter_IdAndComment_Id(Long reporterId, Long commentId);
}
