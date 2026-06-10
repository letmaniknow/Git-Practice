/**
 * Subscription feature - manages subscription plans, user subscriptions, and
 * transactions.
 * 
 * <h2>Package Structure:</h2>
 * 
 * <pre>
 * com.mmva.newsapp.infrastructure.monetization.subscription/
 * ├── model/
 * │   ├── SubscriptionPlan.java               # Plan/tier definitions
 * │   ├── UserSubscription.java               # User's active subscription
 * │   └── SubscriptionTransaction.java        # Payment transactions
 * ├── repository/
 * │   ├── SubscriptionPlanRepository.java
 * │   ├── UserSubscriptionRepository.java
 * │   └── SubscriptionTransactionRepository.java
 * ├── dto/
 * │   ├── SubscriptionPlanRequestDto.java     # Plan create/update
 * │   ├── SubscriptionPlanResponseDto.java    # Plan response
 * │   ├── UserSubscriptionRequestDto.java     # Subscribe request
 * │   ├── UserSubscriptionResponseDto.java    # Subscription response
 * │   ├── UserSubscriptionCancelRequestDto.java # Cancel request
 * │   ├── SubscriptionTransactionRequestDto.java  # Transaction create
 * │   └── SubscriptionTransactionResponseDto.java # Transaction response
 * ├── mapper/
 * │   ├── SubscriptionPlanMapper.java
 * │   ├── UserSubscriptionMapper.java
 * │   └── SubscriptionTransactionMapper.java
 * ├── enums/
 * │   ├── SubscriptionPlanBillingCycle.java   # Billing frequency
 * │   ├── UserSubscriptionStatus.java         # Subscription lifecycle
 * │   ├── SubscriptionTransactionType.java    # Transaction types
 * │   └── SubscriptionTransactionPaymentStatus.java # Payment status
 * ├── exception/
 * │   ├── SubscriptionPlanNotFoundException.java
 * │   ├── UserSubscriptionNotFoundException.java
 * │   └── SubscriptionTransactionNotFoundException.java
 * └── service/
 *     ├── SubscriptionPlanService.java
 *     ├── SubscriptionPlanServiceImpl.java
 *     ├── UserSubscriptionService.java
 *     ├── UserSubscriptionServiceImpl.java
 *     ├── SubscriptionTransactionService.java
 *     └── SubscriptionTransactionServiceImpl.java
 * </pre>
 * 
 * <h2>Controller Location:</h2>
 * <p>
 * Per PROJECT_PRINCIPLES.md, controllers are in:
 * {@code com.mmva.newsapp.controller.admindashboard.monetization.AdminSubscriptionController}
 * </p>
 * 
 * <h2>Entity Relationships:</h2>
 * <ul>
 * <li>{@code SubscriptionPlan} - defines available plans/tiers</li>
 * <li>{@code UserSubscription} - links user to plan with lifecycle</li>
 * <li>{@code SubscriptionTransaction} - payment history for subscription</li>
 * </ul>
 * 
 * <h2>Naming Convention:</h2>
 * <p>
 * All classes use entity-specific prefixes per §6.1 Feature-Contextual Naming:
 * </p>
 * <ul>
 * <li>Plan classes: {@code SubscriptionPlan*}</li>
 * <li>User subscription classes: {@code UserSubscription*}</li>
 * <li>Transaction classes: {@code SubscriptionTransaction*}</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.monetization.subscription;
