package com.mealcircle2.mealcircle2.strategy.impl;

import com.mealcircle2.mealcircle2.model.Subscription;
import com.mealcircle2.mealcircle2.repository.SubscriptionRepository;
import com.mealcircle2.mealcircle2.strategy.MessAttendancePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <b>Coupon Sharing Policy</b>
 *
 * <p>Instead of individual absence tracking, this mess operates a shared daily
 * coupon pool. On any given day, only {@code dailyCouponLimit} customers may
 * claim a coupon (i.e. mark themselves present via an "absent" skip or explicit
 * present mark). Once the pool for the day is exhausted no further claims are
 * accepted.</p>
 *
 * <p>Suitable for mess #3 where the mess has a limited kitchen capacity per day
 * and customers share slots.</p>
 *
 * <p>Semantics in terms of the existing data model:
 * <ul>
 *   <li><b>applyAbsent</b> — the customer is opting <em>out</em> today; this
 *       frees up a coupon slot for others. No pool check needed.</li>
 *   <li><b>applyPresent</b> — the customer is claiming a coupon for today.
 *       The pool across all subscriptions for the same mess on the same date
 *       is checked; if full, the claim is rejected.</li>
 * </ul>
 * </p>
 *
 * <p>Config keys:
 * <ul>
 *   <li>{@code dailyCouponLimit} — maximum customers who can be present on a single day (default: 5)</li>
 * </ul>
 * </p>
 */
@Component
public class CouponSharingPolicy implements MessAttendancePolicy {

    private static final int DEFAULT_DAILY_COUPON_LIMIT = 5;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public String getPolicyName() {
        return "COUPON";
    }

    /**
     * Opting out (absent) releases a slot — always allowed.
     * Removes the date from presentDates if it was already claimed.
     */
    @Override
    public void applyAbsent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        if (subscription.getAbsentDates() != null && !subscription.getAbsentDates().contains(date)) {
            subscription.getAbsentDates().add(date);
        }
        if (subscription.getPresentDates() != null) {
            subscription.getPresentDates().remove(date);
        }
    }

    /**
     * Claiming a coupon (present) requires a free slot in the daily pool.
     * Counts how many other subscriptions for the same mess already have {@code date}
     * in their {@code presentDates} list.
     *
     * @throws RuntimeException if the daily coupon pool for this mess is exhausted
     */
    @Override
    public void applyPresent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        int limit = resolveDailyCouponLimit(policyConfig);

        // Count how many subscriptions for this mess already claimed a coupon today
        List<Subscription> messSubscriptions = subscriptionRepository.findByMessId(subscription.getMessId());
        long claimedCount = messSubscriptions.stream()
                .filter(s -> !s.getId().equals(subscription.getId())) // exclude self
                .filter(s -> s.getPresentDates() != null && s.getPresentDates().contains(date))
                .count();

        if (claimedCount >= limit) {
            throw new RuntimeException(
                    "Coupon Sharing Policy: daily coupon limit of " + limit +
                    " has been reached for " + date + ". No slots remaining."
            );
        }

        if (subscription.getPresentDates() != null && !subscription.getPresentDates().contains(date)) {
            subscription.getPresentDates().add(date);
        }
        if (subscription.getAbsentDates() != null) {
            subscription.getAbsentDates().remove(date);
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private int resolveDailyCouponLimit(Map<String, String> policyConfig) {
        if (policyConfig != null && policyConfig.containsKey("dailyCouponLimit")) {
            try {
                return Integer.parseInt(policyConfig.get("dailyCouponLimit"));
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return DEFAULT_DAILY_COUPON_LIMIT;
    }
}
