package com.mealcircle2.mealcircle2.dto;

import lombok.Data;

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

    }