/**
 * Sponsorship Campaign feature - manages advertising and sponsorship campaigns.
 * 
 * <h2>Package Structure:</h2>
 * 
 * <pre>
 * com.mmva.newsapp.infrastructure.monetization.campaign/
 * ├── model/
 * │   └── SponsorshipCampaign.java           # Campaign entity
 * ├── repository/
 * │   └── SponsorshipCampaignRepository.java # Data access
 * ├── dto/
 * │   ├── SponsorshipCampaignRequestDto.java     # Create/Update request
 * │   ├── SponsorshipCampaignResponseDto.java    # API response with computed fields
 * │   └── SponsorshipCampaignApprovalRequestDto.java # Approval workflow
 * ├── mapper/
 * │   └── SponsorshipCampaignMapper.java     # Entity ↔ DTO mapping
 * ├── enums/
 * │   ├── SponsorshipCampaignStatus.java     # Campaign lifecycle states
 * │   └── SponsorshipCampaignType.java       # Types of campaigns
 * ├── exception/
 * │   └── SponsorshipCampaignNotFoundException.java # Feature-specific exception
 * └── service/
 *     ├── SponsorshipCampaignService.java     # Service interface
 *     └── SponsorshipCampaignServiceImpl.java # Service implementation
 * </pre>
 * 
 * <h2>Controller Location:</h2>
 * <p>
 * Per PROJECT_PRINCIPLES.md, controllers are in:
 * com.mmva.newsapp.controller.admindashboard.monetization.AdminSponsorshipCampaignController
 * </p>
 * 
 * <h2>Naming Convention:</h2>
 * <p>
 * All classes use "SponsorshipCampaign" prefix per §6.1 Feature-Contextual
 * Naming
 * to ensure consistency and self-documenting code.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.monetization.campaign;
