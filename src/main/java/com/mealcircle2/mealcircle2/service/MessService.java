package com.mealcircle2.mealcircle2.service;

import com.mealcircle2.mealcircle2.dto.MessRequest;
import com.mealcircle2.mealcircle2.dto.NearbyMessResponse;
import com.mealcircle2.mealcircle2.model.Mess;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MessService {

    Mess createMess(MessRequest request, String ownerId, MultipartFile file);

    List<Mess> getAllMesses();

    Mess getMessById(String id);

    Mess getMessByOwner(String ownerId);

    // ✅ FIXED SIGNATURE
    Mess updateMess(String id, MessRequest request, String ownerId, MultipartFile file);

    void deleteMess(String id, String ownerId);

    Mess joinMess(String messId, String userId);

    Mess getMessByCustomer(String customerId);

    Mess updateMenuAndNoticeByOwner(String ownerId, String todaysMenu, String notices);

    List<NearbyMessResponse> getMessesNearby(double lat, double lng, double radiusKm);
}