package com.mealcircle2.mealcircle2.strategy.impl;

import com.mealcircle2.mealcircle2.model.Subscription;
import com.mealcircle2.mealcircle2.strategy.MessAttendancePolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * <b>Grace Period Policy</b>
 *
 * <p>A lenient policy that never hard-blocks a student from marking absent,
 * even after the standard buffer is exhausted. Once the {@code maxBuffer} is
 * used up, every additional absent day incurs a monetary penalty
 * ({@code penaltyPerDay}) that is added to {@link Subscription#getMoneyLeftToPay()}.
 * The subscription end date is still extended by 1 day per absent as compensation.</p>
 *
 * <p>Suitable for mess #4 where flexibility is preferred over hard cut-offs,
 * but financial accountability is enforced via penalty charges.</p>
 *
 * <p>Config keys:
 * <ul>
 *   <li>{@code maxBuffer}     — free absent days before penalties apply (default: 5)</li>
 *   <li>{@code penaltyPerDay} — amount charged per extra absent day in ₹ (default: 50.0)</li>
 * </ul>
 * </p>
 */
@Component
public class GracePeriodPolicy implements MessAttendancePolicy {

    private static final int    DEFAULT_MAX_BUFFER      = 5;
    private static final double DEFAULT_PENALTY_PER_DAY = 50.0;

    @Override
    public String getPolicyName() {
        return "GRACE_PERIOD";
    }

    @Override
    public void applyAbsent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        // Idempotent — skip if already absent
        if (subscription.getAbsentDates() != null && subscription.getAbsentDates().contains(date)) {
            return;
        }

        int    maxBuffer      = resolveMaxBuffer(policyConfig);
        double penaltyPerDay  = resolvePenaltyPerDay(policyConfig);

        subscription.getAbsentDates().add(date);
        subscription.getPresentDates().remove(date);

        if (subscription.getBuffer() > 0) {
            // Still within the free buffer — consume 1 buffer day, extend end date
            subscription.setBuffer(subscription.getBuffer() - 1);
            subscription.setMessEndingDate(subscription.getMessEndingDate().plusDays(1));
        } else {
            // Buffer exhausted — charge a penalty; end-date still extended as courtesy
            subscription.setMoneyLeftToPay(subscription.getMoneyLeftToPay() + penaltyPerDay);
            subscription.setMessEndingDate(subscription.getMessEndingDate().plusDays(1));
        }
    }

    @Override
    public void applyPresent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        double penaltyPerDay = resolvePenaltyPerDay(policyConfig);

        if (!subscription.getPresentDates().contains(date)) {
            subscription.getPresentDates().add(date);
        }

        if (subscription.getAbsentDates().remove(date)) {
            // Was previously absent — reverse the effect
            if (subscription.getBuffer() < resolveMaxBuffer(policyConfig)) {
                // Was charged against buffer — restore 1 buffer day, undo end-date extension
                subscription.setBuffer(subscription.getBuffer() + 1);
                subscription.setMessEndingDate(subscription.getMessEndingDate().minusDays(1));
            } else {
                // Was a penalty absence — refund the penalty, undo end-date extension
                subscription.setMoneyLeftToPay(
                        Math.max(0, subscription.getMoneyLeftToPay() - penaltyPerDay)
                );
                subscription.setMessEndingDate(subscription.getMessEndingDate().minusDays(1));
            }
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private int resolveMaxBuffer(Map<String, String> policyConfig) {
        if (policyConfig != null && policyConfig.containsKey("maxBuffer")) {
            try {
                return Integer.parseInt(policyConfig.get("maxBuffer"));
            } catch (NumberFormatException ignored) { }
        }
        return DEFAULT_MAX_BUFFER;
    }

    private double resolvePenaltyPerDay(Map<String, String> policyConfig) {
        if (policyConfig != null && policyConfig.containsKey("penaltyPerDay")) {
            try {
                return Double.parseDouble(policyConfig.get("penaltyPerDay"));
            } catch (NumberFormatException ignored) { }
        }
        return DEFAULT_PENALTY_PER_DAY;
    }
}
