package com.mealcircle2.mealcircle2.repository;

import com.mealcircle2.mealcircle2.model.Mess;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MessRepository extends MongoRepository<Mess, String> {

    Optional<Mess> findByOwnerId(String ownerId);
}