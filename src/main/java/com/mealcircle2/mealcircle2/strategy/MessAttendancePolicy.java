package com.mealcircle2.mealcircle2.strategy;

import com.mealcircle2.mealcircle2.model.Subscription;

import java.time.LocalDate;
import java.util.Map;

/**
 * Strategy interface for mess-specific attendance policies.
 *
 * <p>Each concrete implementation encapsulates a single set of attendance rules
 * (e.g. strict zero-tolerance, buffered absences, shared coupons, grace-period penalty).
 * The {@link com.mealcircle2.mealcircle2.strategy.MessPolicyFactory} selects the right
 * implementation at runtime based on {@code Mess.attendancePolicyType}.</p>
 *
 * <p>Implementations should be Spring {@code @Component}s so they are auto-discovered
 * by {@link MessPolicyFactory}.</p>
 */
public interface MessAttendancePolicy {

    /**
     * Validate and apply the "mark absent" operation for the given subscription and date.
     *
     * <p>The implementation is responsible for all mutations to the {@link Subscription}
     * object (absent list, buffer, money penalties, etc.).
     * The calling service will persist the returned/mutated subscription.</p>
     *
     * @param subscription the subscription to update
     * @param date         the date to mark as absent
     * @param policyConfig mess-level config key-value pairs (e.g. maxBuffer, penaltyPerDay)
     * @throws RuntimeException if the policy forbids this absence
     */
    void applyAbsent(Subscription subscription, LocalDate date, Map<String, String> policyConfig);

    /**
     * Validate and apply the "mark present" operation for the given subscription and date.
     *
     * @param subscription the subscription to update
     * @param date         the date to mark as present
     * @param policyConfig mess-level config key-value pairs
     */
    void applyPresent(Subscription subscription, LocalDate date, Map<String, String> policyConfig);

    /**
     * A stable, upper-case identifier for this policy (e.g. {@code "STRICT"}, {@code "BUFFERED"}).
     * This value is stored in {@code Mess.attendancePolicyType} and used as the registry key
     * inside {@link MessPolicyFactory}.
     */
    String getPolicyName();
}
