package com.mmva.newsapp.infrastructure.push.service;

import com.mmva.newsapp.infrastructure.push.model.PushNotification;
import com.mmva.newsapp.infrastructure.push.model.PushNotificationAuditLog;
import com.mmva.newsapp.infrastructure.push.repository.PushNotificationAuditLogRepository;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing push notification audit logs.
 * 
 * <p>
 * Provides methods to log admindashboard actions on push notifications
 * and query audit history for compliance and debugging.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationAuditLogService {

    private final PushNotificationAuditLogRepository auditLogRepository;

    // ========================================
    // Audit Actions
    // ========================================

    public static final String ACTION_SEND = "SEND";
    public static final String ACTION_SCHEDULE = "SCHEDULE";
    public static final String ACTION_CANCEL = "CANCEL";
    public static final String ACTION_RETRY = "RETRY";
    public static final String ACTION_BREAKING_NEWS = "BREAKING_NEWS";

    // ========================================
    // Log Operations
    // ========================================

    /**
     * Logs a push notification action.
     * Runs in a separate transaction to ensure audit is saved even if main
     * transaction fails.
     * 
     * @param notification the notification entity (may be null for pre-creation
     *                     failures)
     * @param action       the action performed
     * @param actorId      the admindashboard who performed the action
     * @param success      whether the operation succeeded
     * @param errorMessage error message if failed
     * @param ipAddress    client IP address
     * @param userAgent    client user agent
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(
            PushNotification notification,
            String action,
            UUID actorId,
            Boolean success,
            String errorMessage,
            String ipAddress,
            String userAgent) {

        try {
            PushNotificationAuditLog auditLog = PushNotificationAuditLog.builder()
                    .notificationId(notification != null ? notification.getNotificationId() : null)
                    .action(action)
                    .targetType(notification != null ? notification.getTargetType().name() : null)
                    .targetValue(notification != null ? notification.getTargetValue() : null)
                    .title(notification != null ? notification.getTitle() : null)
                    .recipientCount(notification != null ? notification.getSentCount() : null)
                    .success(success)
                    .errorMessage(errorMessage)
                    .actorId(actorId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("PushNotificationAuditLog: Logged action={} notificationId={} actorId={} success={}",
                    action, auditLog.getNotificationId(), actorId, success);

        } catch (Exception e) {
            // Never fail the main operation due to audit logging failure
            log.error("PushNotificationAuditLog: Failed to log action={} - {}", action, e.getMessage());
        }
    }

    /**
     * Logs a successful send operation.
     */
    public void logSend(PushNotification notification, UUID actorId, String ipAddress, String userAgent) {
        logAction(notification, ACTION_SEND, actorId, true, null, ipAddress, userAgent);
    }

    /**
     * Logs a scheduled notification.
     */
    public void logSchedule(PushNotification notification, UUID actorId, String ipAddress, String userAgent) {
        logAction(notification, ACTION_SCHEDULE, actorId, true, null, ipAddress, userAgent);
    }

    /**
     * Logs a cancelled notification.
     */
    public void logCancel(PushNotification notification, UUID actorId, String ipAddress, String userAgent) {
        logAction(notification, ACTION_CANCEL, actorId, true, null, ipAddress, userAgent);
    }

    /**
     * Logs a retry operation.
     */
    public void logRetry(PushNotification notification, UUID actorId, String ipAddress, String userAgent) {
        logAction(notification, ACTION_RETRY, actorId, true, null, ipAddress, userAgent);
    }

    /**
     * Logs a breaking newsapp send.
     */
    public void logBreakingNews(PushNotification notification, UUID actorId, String ipAddress, String userAgent) {
        logAction(notification, ACTION_BREAKING_NEWS, actorId, true, null, ipAddress, userAgent);
    }

    /**
     * Logs a failed operation.
     */
    public void logFailure(String action, UUID actorId, String errorMessage, String ipAddress, String userAgent) {
        logAction(null, action, actorId, false, errorMessage, ipAddress, userAgent);
    }

    // ========================================
    // Overloaded Methods with ClientInfo
    // ========================================

    /**
     * Logs a push notification action using ClientInfo.
     * 
     * @param notification the notification entity
     * @param action       the action performed
     * @param actorId      the admindashboard who performed the action
     * @param success      whether the operation succeeded
     * @param errorMessage error message if failed
     * @param clientInfo   client request information
     */
    public void logAction(
            PushNotification notification,
            String action,
            UUID actorId,
            Boolean success,
            String errorMessage,
            RequestClientInfoDto clientInfo) {
        logAction(notification, action, actorId, success, errorMessage,
                clientInfo != null ? clientInfo.ipAddress() : null,
                clientInfo != null ? clientInfo.userAgent() : null);
    }

    /**
     * Logs a successful send operation using ClientInfo.
     */
    public void logSend(PushNotification notification, UUID actorId, RequestClientInfoDto clientInfo) {
        logAction(notification, ACTION_SEND, actorId, true, null, clientInfo);
    }

    /**
     * Logs a scheduled notification using ClientInfo.
     */
    public void logSchedule(PushNotification notification, UUID actorId, RequestClientInfoDto clientInfo) {
        logAction(notification, ACTION_SCHEDULE, actorId, true, null, clientInfo);
    }

    /**
     * Logs a cancelled notification using ClientInfo.
     */
    public void logCancel(PushNotification notification, UUID actorId, RequestClientInfoDto clientInfo) {
        logAction(notification, ACTION_CANCEL, actorId, true, null, clientInfo);
    }

    /**
     * Logs a retry operation using ClientInfo.
     */
    public void logRetry(PushNotification notification, UUID actorId, RequestClientInfoDto clientInfo) {
        logAction(notification, ACTION_RETRY, actorId, true, null, clientInfo);
    }

    /**
     * Logs a breaking newsapp send using ClientInfo.
     */
    public void logBreakingNews(PushNotification notification, UUID actorId, RequestClientInfoDto clientInfo) {
        logAction(notification, ACTION_BREAKING_NEWS, actorId, true, null, clientInfo);
    }

    /**
     * Logs a failed operation using ClientInfo.
     */
    public void logFailure(String action, UUID actorId, String errorMessage, RequestClientInfoDto clientInfo) {
        logAction(null, action, actorId, false, errorMessage, clientInfo);
    }

    // ========================================
    // Query Operations
    // ========================================

    /**
     * Gets audit logs for a specific notification.
     */
    public List<PushNotificationAuditLog> findByNotificationId(UUID notificationId) {
        return auditLogRepository.findByNotificationIdOrderByCreatedAtDesc(notificationId);
    }

    /**
     * Gets audit logs by actor with pagination.
     */
    public Page<PushNotificationAuditLog> findByActorId(UUID actorId, Pageable pageable) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable);
    }

    /**
     * Gets audit logs by action type.
     */
    public Page<PushNotificationAuditLog> findByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }

    /**
     * Gets audit logs within a date range.
     */
    public Page<PushNotificationAuditLog> findByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * Gets audit logs with filters.
     */
    public Page<PushNotificationAuditLog> findWithFilters(
            UUID actorId,
            String action,
            String targetType,
            Instant startDate,
            Instant endDate,
            Pageable pageable) {
        return auditLogRepository.findWithFilters(actorId, action, targetType, startDate, endDate, pageable);
    }

    /**
     * Counts actions by an actor since a given time.
     */
    public long countByActorSince(UUID actorId, Instant since) {
        return auditLogRepository.countByActorSince(actorId, since);
    }

    /**
     * Counts failed operations since a given time.
     */
    public long countFailedSince(Instant since) {
        return auditLogRepository.countFailedSince(since);
    }
}
