package com.mmva.newsapp.domain.newsengagement.comments.repository;

import com.mmva.newsapp.domain.newsengagement.comments.model.NewsCommentAuditLog;

//import org.hibernate.validator.constraints.UUID;
import java.util.UUID;
import java.lang.Long;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsCommentAuditLogRepository extends JpaRepository<NewsCommentAuditLog, Long> {
    // Optionally, add methods to query logs by user, comment, or action
}
