package com.mealcircle2.mealcircle2.service;

import com.mealcircle2.mealcircle2.service.serviceImpl.SubscriptionServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceScheduler {

  private final SubscriptionServiceImpl subscriptionService;

  public AttendanceScheduler(SubscriptionServiceImpl subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  @Scheduled(cron = "0 0 15 * * *")
  public void markPresentForUnmarkedCustomers() {
    subscriptionService.autoMarkPresentForTodayAfterCutoff();
  }
}
