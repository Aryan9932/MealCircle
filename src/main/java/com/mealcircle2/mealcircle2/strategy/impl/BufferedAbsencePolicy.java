package com.mealcircle2.mealcircle2.strategy.impl;

import com.mealcircle2.mealcircle2.model.Subscription;
import com.mealcircle2.mealcircle2.strategy.MessAttendancePolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * <b>Buffered Absence Policy</b>
 *
 * <p>The default policy (backward-compatible). Each student is allowed up to
 * {@code maxBuffer} absent days per subscription period. Each absence decrements
 * the buffer by 1 and extends {@code messEndingDate} by 1 day to compensate.
 * When the buffer reaches 0, further absences are rejected.</p>
 *
 * <p>Suitable for mess #2 that allows a 10-day absence buffer.</p>
 *
 * <p>Config keys:
 * <ul>
 *   <li>{@code maxBuffer} — maximum absent days allowed (default: 10)</li>
 * </ul>
 * </p>
 */
@Component
public class BufferedAbsencePolicy implements MessAttendancePolicy {

    private static final int DEFAULT_MAX_BUFFER = 10;

    @Override
    public String getPolicyName() {
        return "BUFFERED";
    }

    @Override
    public void applyAbsent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        // Already absent — idempotent
        if (subscription.getAbsentDates() != null && subscription.getAbsentDates().contains(date)) {
            return;
        }

        int maxBuffer = resolveMaxBuffer(policyConfig);

        if (subscription.getBuffer() <= 0) {
            throw new RuntimeException(
                    "Buffered Absence Policy: absence buffer exhausted (max " + maxBuffer + " days). " +
                    "No further absences can be recorded."
            );
        }

        subscription.getAbsentDates().add(date);
        subscription.getPresentDates().remove(date);
        subscription.setBuffer(subscription.getBuffer() - 1);
        // Compensate: extend the subscription end date by 1 day
        subscription.setMessEndingDate(subscription.getMessEndingDate().plusDays(1));
    }

    @Override
    public void applyPresent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        if (!subscription.getPresentDates().contains(date)) {
            subscription.getPresentDates().add(date);
        }
        // If was previously absent, restore 1 buffer day and undo the end-date extension
        if (subscription.getAbsentDates().remove(date)) {
            subscription.setBuffer(subscription.getBuffer() + 1);
            subscription.setMessEndingDate(subscription.getMessEndingDate().minusDays(1));
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private int resolveMaxBuffer(Map<String, String> policyConfig) {
        if (policyConfig != null && policyConfig.containsKey("maxBuffer")) {
            try {
                return Integer.parseInt(policyConfig.get("maxBuffer"));
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return DEFAULT_MAX_BUFFER;
    }
}
