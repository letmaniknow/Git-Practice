package com.mmva.newsapp.infrastructure.push.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditLogEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Audit log entity for tracking push notification admindashboard actions.
 * 
 * <p>
 * Records admindashboard operations on push notifications for compliance
 * and traceability purposes. This is an append-only table.
 * </p>
 * 
 * <h2>Tracked Actions:</h2>
 * <ul>
 * <li>SEND - Notification sent</li>
 * <li>SCHEDULE - Notification scheduled for later</li>
 * <li>CANCEL - Scheduled notification cancelled</li>
 * <li>RETRY - Failed notification retried</li>
 * <li>BREAKING_NEWS - Breaking newsapp alert sent</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "push_notification_audit_log", indexes = {
        @Index(name = "idx_push_audit_notification_id", columnList = "notification_id"),
        @Index(name = "idx_push_audit_actor_id", columnList = "actor_id"),
        @Index(name = "idx_push_audit_action", columnList = "action"),
        @Index(name = "idx_push_audit_created_at", columnList = "created_at")
})
public class PushNotificationAuditLog extends BaseAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The notification this audit entry relates to.
     * May be null for failed send attempts before notification creation.
     */
    @Column(name = "notification_id")
    private UUID notificationId;

    /**
     * The action performed.
     * Values: SEND, SCHEDULE, CANCEL, RETRY, BREAKING_NEWS
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Target type for the notification.
     * Values: ALL, TOPIC, DEVICE, USER, SEGMENT
     */
    @Column(name = "target_type", length = 20)
    private String targetType;

    /**
     * Target value (topic name, user ID, segment name, etc.)
     */
    @Column(name = "target_value", length = 255)
    private String targetValue;

    /**
     * Notification title (for audit reference).
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * Number of devices/recipients targeted.
     */
    @Column(name = "recipient_count")
    private Integer recipientCount;

    /**
     * Whether the operation was successful.
     */
    @Column(name = "success")
    private Boolean success;

    /**
     * Error message if the operation failed.
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * Additional details in JSON format.
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * IP address of the admindashboard who performed the action.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of the admindashboard client.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
