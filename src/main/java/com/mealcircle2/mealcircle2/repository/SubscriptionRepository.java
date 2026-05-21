package com.mealcircle2.mealcircle2.repository;

import com.mealcircle2.mealcircle2.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Optional<Subscription> findByCustomerIdAndMessId(String customerId, String messId);

    List<Subscription> findByMessId(String messId);

    List<Subscription> findByCustomerId(String customerId);
}
