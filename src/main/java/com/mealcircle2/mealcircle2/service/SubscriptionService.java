package com.mealcircle2.mealcircle2.service;

import com.mealcircle2.mealcircle2.dto.SubscriptionResponse;
import com.mealcircle2.mealcircle2.model.Subscription;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionService {

    Subscription addAbsentDate(String subscriptionId, LocalDate absentDate);

    Subscription addPresentDate(String subscriptionId, LocalDate presentDate);

    Subscription removeAbsentDate(String subscriptionId, LocalDate absentDate);

    Subscription updateMoneyLeftToPay(String subscriptionId, double money);

    SubscriptionResponse getSubscriptionDetails(String subscriptionId);

    List<SubscriptionResponse> getSubscriptionsForMess(String messId);

    List<SubscriptionResponse> getSubscriptionsForCustomer(String customerId);
}
