package com.mealcircle2.mealcircle2.service.serviceImpl;

import com.mealcircle2.mealcircle2.dto.SubscriptionResponse;
import com.mealcircle2.mealcircle2.model.Subscription;
import com.mealcircle2.mealcircle2.repository.SubscriptionRepository;
import com.mealcircle2.mealcircle2.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final LocalTime ABSENT_CUTOFF_TIME = LocalTime.of(15, 0);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public Subscription addAbsentDate(String subscriptionId, LocalDate absentDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        initializeAttendanceFields(subscription);

        if (subscription.getAbsentDates().contains(absentDate)) {
            return subscription;
        }

        if (isToday(absentDate) && isAfterOrAtCutoff()) {
            markPresentForDate(subscription, absentDate);
            return subscriptionRepository.save(subscription);
        }

        if (subscription.getBuffer() <= 0) {
            throw new RuntimeException("No buffer left. Cannot mark absent.");
        }

        subscription.getAbsentDates().add(absentDate);
        subscription.getPresentDates().remove(absentDate);
        subscription.setBuffer(subscription.getBuffer() - 1);
        subscription.setMessEndingDate(subscription.getMessEndingDate().plusDays(1));

        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription addPresentDate(String subscriptionId, LocalDate presentDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        initializeAttendanceFields(subscription);
        markPresentForDate(subscription, presentDate);

        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription removeAbsentDate(String subscriptionId, LocalDate absentDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        initializeAttendanceFields(subscription);

        if (subscription.getAbsentDates().remove(absentDate)) {
            subscription.setBuffer(subscription.getBuffer() + 1);
        }

        return subscriptionRepository.save(subscription);
    }

    public void autoMarkPresentForTodayAfterCutoff() {
        if (!isAfterOrAtCutoff()) {
            return;
        }

        LocalDate today = LocalDate.now();
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        List<Subscription> updatedSubscriptions = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            initializeAttendanceFields(subscription);

            if (markPresentForTodayIfRequired(subscription)) {
                updatedSubscriptions.add(subscription);
            }
        }

        if (!updatedSubscriptions.isEmpty()) {
            subscriptionRepository.saveAll(updatedSubscriptions);
        }
    }

    @Override
    public Subscription updateMoneyLeftToPay(String subscriptionId, double money) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setMoneyLeftToPay(money);
        return subscriptionRepository.save(subscription);
    }

    @Override
    public SubscriptionResponse getSubscriptionDetails(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        initializeAttendanceFields(subscription);
        if (markPresentForTodayIfRequired(subscription)) {
            subscription = subscriptionRepository.save(subscription);
        }

        return convertToResponse(subscription);
    }

    @Override
    public List<SubscriptionResponse> getSubscriptionsForMess(String messId) {
        List<Subscription> subscriptions = subscriptionRepository.findByMessId(messId);
        List<Subscription> updatedSubscriptions = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            initializeAttendanceFields(subscription);
            if (markPresentForTodayIfRequired(subscription)) {
                updatedSubscriptions.add(subscription);
            }
        }

        if (!updatedSubscriptions.isEmpty()) {
            subscriptionRepository.saveAll(updatedSubscriptions);
        }

        return subscriptions.stream().map(this::convertToResponse).toList();
    }

    @Override
    public List<SubscriptionResponse> getSubscriptionsForCustomer(String customerId) {
        List<Subscription> subscriptions = subscriptionRepository.findByCustomerId(customerId);
        List<Subscription> updatedSubscriptions = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            initializeAttendanceFields(subscription);
            if (markPresentForTodayIfRequired(subscription)) {
                updatedSubscriptions.add(subscription);
            }
        }

        if (!updatedSubscriptions.isEmpty()) {
            subscriptionRepository.saveAll(updatedSubscriptions);
        }

        return subscriptions.stream().map(this::convertToResponse).toList();
    }

    private SubscriptionResponse convertToResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .customerId(subscription.getCustomerId())
                .messId(subscription.getMessId())
                .joiningDate(subscription.getJoiningDate().toString())
                .messEndingDate(subscription.getMessEndingDate())
                .absentDates(subscription.getAbsentDates())
                .buffer(subscription.getBuffer())
                .presentDates(subscription.getPresentDates())
                .moneyLeftToPay(subscription.getMoneyLeftToPay())
                .build();
    }

    private void initializeAttendanceFields(Subscription subscription) {
        if (subscription.getAbsentDates() == null) {
            subscription.setAbsentDates(new ArrayList<>());
        }
        if (subscription.getPresentDates() == null) {
            subscription.setPresentDates(new ArrayList<>());
        }
        if (subscription.getMessEndingDate() == null && subscription.getJoiningDate() != null) {
            subscription.setMessEndingDate(subscription.getJoiningDate().toLocalDate().plusDays(30));
        }
    }

    private boolean markPresentForTodayIfRequired(Subscription subscription) {
        if (!isAfterOrAtCutoff()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        boolean alreadyAbsent = subscription.getAbsentDates().contains(today);
        boolean alreadyPresent = subscription.getPresentDates().contains(today);

        if (!alreadyAbsent && !alreadyPresent) {
            markPresentForDate(subscription, today);
            return true;
        }

        return false;
    }

    private void markPresentForDate(Subscription subscription, LocalDate presentDate) {
        if (!subscription.getPresentDates().contains(presentDate)) {
            subscription.getPresentDates().add(presentDate);
        }

        if (subscription.getAbsentDates().remove(presentDate)) {
            subscription.setBuffer(subscription.getBuffer() + 1);
        }
    }

    private boolean isAfterOrAtCutoff() {
        return !LocalTime.now().isBefore(ABSENT_CUTOFF_TIME);
    }

    private boolean isToday(LocalDate date) {
        return LocalDate.now().equals(date);
    }
}
