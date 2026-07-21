package com.mealcircle2.mealcircle2.service;

public interface EmailService {

    /**
     * Sent to a customer when they successfully subscribe to a mess.
     */
    void sendWelcomeEmail(String customerEmail, String messName, String joiningDate, String endingDate);

    /**
     * Sent to a customer when their attendance is marked as absent.
     */
    void sendAbsentEmail(String customerEmail, String messName, String date);

    /**
     * Sent to a customer when their attendance is marked as present.
     */
    void sendPresentEmail(String customerEmail, String messName, String date);
}