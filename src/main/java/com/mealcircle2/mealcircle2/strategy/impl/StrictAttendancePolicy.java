package com.mealcircle2.mealcircle2.strategy.impl;

import com.mealcircle2.mealcircle2.model.Subscription;
import com.mealcircle2.mealcircle2.strategy.MessAttendancePolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * <b>Strict Attendance Policy</b>
 *
 * <p>Zero tolerance: no absent days are allowed under any circumstances.
 * Any call to {@link #applyAbsent} immediately throws a {@link RuntimeException}.
 * This is suitable for mess #1 where 100 % presence is mandatory.</p>
 *
 * <p>Config keys: <em>none</em></p>
 */
@Component
public class StrictAttendancePolicy implements MessAttendancePolicy {

    @Override
    public String getPolicyName() {
        return "STRICT";
    }

    /**
     * Always throws — this mess does not permit any absences.
     */
    @Override
    public void applyAbsent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        throw new RuntimeException(
                "Strict Attendance Policy: zero absences are allowed in this mess. " +
                "You must be present every day."
        );
    }

    /**
     * Marks the date as present (removes it from absent list if somehow present, adds to present list).
     */
    @Override
    public void applyPresent(Subscription subscription, LocalDate date, Map<String, String> policyConfig) {
        if (subscription.getPresentDates() != null && !subscription.getPresentDates().contains(date)) {
            subscription.getPresentDates().add(date);
        }
        // Strict policy has no buffer concept, but keep state clean
        if (subscription.getAbsentDates() != null) {
            subscription.getAbsentDates().remove(date);
        }
    }
}
