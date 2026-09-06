package com.mealcircle2.mealcircle2.dto;

import lombok.Data;

import java.util.Map;

@Data
public class MessRequest {

    private String messName;
    private String email;
    private String address;

    private double latitude;
    private double longitude;

    private String type;

    private String todaysMenu;
    private String notices;

    private String ownerPhone;

    private double pricePerMonth;

    /**
     * Attendance policy type for this mess.
     * Accepted values: STRICT | BUFFERED | COUPON | GRACE_PERIOD
     * Defaults to BUFFERED when not provided.
     */
    private String attendancePolicyType;

    /**
     * Key-value config for the chosen policy.
     * Examples:
     *   BUFFERED     -> {"maxBuffer": "10"}
     *   COUPON       -> {"dailyCouponLimit": "5"}
     *   GRACE_PERIOD -> {"maxBuffer": "5", "penaltyPerDay": "50.0"}
     */
    private Map<String, String> policyConfig;
}