package com.mealcircle2.mealcircle2.controller;

import com.mealcircle2.mealcircle2.dto.SubscriptionResponse;
import com.mealcircle2.mealcircle2.model.Mess;
import com.mealcircle2.mealcircle2.model.Subscription;
import com.mealcircle2.mealcircle2.service.MessService;
import com.mealcircle2.mealcircle2.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private MessService messService;

    @PostMapping("/{subscriptionId}/absent")
    public ResponseEntity<?> addAbsentDate(
            @PathVariable String subscriptionId,
            @RequestParam LocalDate date) {
        try {
            Subscription subscription = subscriptionService.addAbsentDate(subscriptionId, date);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{subscriptionId}/present")
    public ResponseEntity<?> addPresentDate(
            @PathVariable String subscriptionId,
            @RequestParam LocalDate date) {
        try {
            Subscription subscription = subscriptionService.addPresentDate(subscriptionId, date);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{subscriptionId}/absent")
    public ResponseEntity<?> removeAbsentDate(
            @PathVariable String subscriptionId,
            @RequestParam LocalDate date) {
        try {
            Subscription subscription = subscriptionService.removeAbsentDate(subscriptionId, date);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{subscriptionId}/money")
    public ResponseEntity<?> updateMoneyLeftToPay(
            @PathVariable String subscriptionId,
            @RequestParam double money) {
        try {
            Subscription subscription = subscriptionService.updateMoneyLeftToPay(subscriptionId, money);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionDetails(@PathVariable String subscriptionId) {
        try {
            SubscriptionResponse response = subscriptionService.getSubscriptionDetails(subscriptionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mess/{messId}")
    public ResponseEntity<?> getSubscriptionsForMess(@PathVariable String messId) {
        try {
            List<SubscriptionResponse> responses = subscriptionService.getSubscriptionsForMess(messId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getSubscriptionsForCustomer(@PathVariable String customerId) {
        try {
            List<SubscriptionResponse> responses = subscriptionService.getSubscriptionsForCustomer(customerId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/customer/me")
    public ResponseEntity<?> getMySubscriptions(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            List<SubscriptionResponse> responses = subscriptionService
                    .getSubscriptionsForCustomer(authentication.getName());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/owner/my-mess")
    public ResponseEntity<?> getOwnerMessSubscriptions(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            Mess mess = messService.getMessByOwner(authentication.getName());
            List<SubscriptionResponse> responses = subscriptionService.getSubscriptionsForMess(mess.getId());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
