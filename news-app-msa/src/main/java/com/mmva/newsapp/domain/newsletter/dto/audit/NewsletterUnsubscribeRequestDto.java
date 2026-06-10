package com.mmva.newsapp.domain.newsletter.dto.audit;

import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterUnsubscribeReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for newsletter unsubscribe requests.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
@Schema(description = "Newsletter unsubscribe request")
public class NewsletterUnsubscribeRequestDto {

    @Schema(description = "Subscriber ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long newsletterSubscriberId;

    @Schema(description = "Unsubscribe reason", example = "NO_LONGER_INTERESTED")
    private NewsletterUnsubscribeReason newsletterUnsubscribeReason;

    @Schema(description = "Additional feedback", example = "Content not relevant to my interests")
    private String newsletterUnsubscribeFeedback;
}