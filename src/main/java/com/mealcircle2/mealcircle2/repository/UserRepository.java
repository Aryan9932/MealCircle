package com.mealcircle2.mealcircle2.repository;

import com.mealcircle2.mealcircle2.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findFirstByEmailOrderByIdDesc(String email);

    boolean existsByEmail(String email);
}