package com.mealcircle2.mealcircle2.strategy;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Spring-managed registry that maps policy names to their {@link MessAttendancePolicy}
 * implementations.
 *
 * <p>All {@link MessAttendancePolicy} beans in the application context are
 * auto-discovered and indexed by {@link MessAttendancePolicy#getPolicyName()}.
 * Adding a new policy is as simple as creating a new {@code @Component} that
 * implements the interface — no changes here are needed.</p>
 *
 * <p>The factory falls back to {@code "BUFFERED"} when a mess's
 * {@code attendancePolicyType} is {@code null} or empty, preserving
 * backward-compatibility with existing Mess documents that predate the
 * strategy feature.</p>
 *
 * <p>Usage:
 * <pre>{@code
 *   MessAttendancePolicy policy = policyFactory.getPolicy(mess.getAttendancePolicyType());
 *   policy.applyAbsent(subscription, date, mess.getPolicyConfig());
 * }</pre>
 * </p>
 */
@Component
public class MessPolicyFactory {

    private static final String DEFAULT_POLICY = "BUFFERED";

    /** Injected by Spring — all beans that implement MessAttendancePolicy */
    @Autowired
    private List<MessAttendancePolicy> allPolicies;

    /** Populated on startup — key: policyName, value: policy instance */
    private Map<String, MessAttendancePolicy> registry;

    @PostConstruct
    private void buildRegistry() {
        registry = allPolicies.stream()
                .collect(Collectors.toMap(
                        MessAttendancePolicy::getPolicyName,
                        Function.identity()
                ));
    }

    /**
     * Return the policy for the given name.
     * Falls back to {@code "BUFFERED"} if {@code policyType} is blank or unknown.
     *
     * @param policyType the value stored in {@code Mess.attendancePolicyType}
     * @return the matching policy (never {@code null})
     * @throws IllegalStateException if no policy is registered for the requested name
     *         AND no fallback could be found (should never happen in practice)
     */
    public MessAttendancePolicy getPolicy(String policyType) {
        String key = (policyType == null || policyType.isBlank()) ? DEFAULT_POLICY : policyType.toUpperCase();

        MessAttendancePolicy policy = registry.get(key);
        if (policy == null) {
            // Unknown policy name — fall back to BUFFERED rather than crashing
            policy = registry.get(DEFAULT_POLICY);
            if (policy == null) {
                throw new IllegalStateException(
                        "MessPolicyFactory: no policy registered for '" + key +
                        "' and fallback policy '" + DEFAULT_POLICY + "' is also missing. " +
                        "Check that BufferedAbsencePolicy is on the classpath."
                );
            }
        }
        return policy;
    }

    /**
     * Returns an unmodifiable view of all registered policy names.
     * Useful for validation when creating/updating a mess.
     */
    public java.util.Set<String> registeredPolicyNames() {
        return java.util.Collections.unmodifiableSet(registry.keySet());
    }
}
