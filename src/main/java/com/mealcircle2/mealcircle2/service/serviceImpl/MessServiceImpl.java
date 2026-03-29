package com.mealcircle2.mealcircle2.service.serviceImpl;

import com.mealcircle2.mealcircle2.dto.MessRequest;
import com.mealcircle2.mealcircle2.model.Mess;
import com.mealcircle2.mealcircle2.repository.MessRepository;
import com.mealcircle2.mealcircle2.service.MessService;
import com.mealcircle2.mealcircle2.util.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessServiceImpl implements MessService {

    @Autowired
    private MessRepository messRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public Mess createMess(MessRequest request, String ownerId, MultipartFile file) {

        String imageUrl = null;

        // ✅ Upload file instead of base64
        if (file != null && !file.isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(file);
        }

        Mess mess = Mess.builder()
                .messName(request.getMessName())
                .email(request.getEmail())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .type(request.getType())
                .todaysMenu(request.getTodaysMenu())
                .notices(request.getNotices())
                .ownerPhone(request.getOwnerPhone())
                .pricePerMonth(request.getPricePerMonth())
                .ownerId(ownerId)
                .customers(new ArrayList<>())
                .imageUrl(imageUrl)
                .build();

        return messRepository.save(mess);
    }

    @Override
    public List<Mess> getAllMesses() {
        return messRepository.findAll();
    }

    @Override
    public Mess getMessById(String id) {
        return messRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mess not found"));
    }

    @Override
    public Mess getMessByOwner(String ownerId) {
        return messRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new RuntimeException("No mess found"));
    }

    @Override
    public Mess updateMess(String id, MessRequest request, String ownerId, MultipartFile file) {

        Mess mess = getMessById(id);

        if (!mess.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        // ✅ Update image if new file provided
        if (file != null && !file.isEmpty()) {
            mess.setImageUrl(cloudinaryService.uploadFile(file));
        }

        mess.setMessName(request.getMessName());
        mess.setEmail(request.getEmail());
        mess.setAddress(request.getAddress());
        mess.setLatitude(request.getLatitude());
        mess.setLongitude(request.getLongitude());
        mess.setType(request.getType());
        mess.setTodaysMenu(request.getTodaysMenu());
        mess.setNotices(request.getNotices());
        mess.setOwnerPhone(request.getOwnerPhone());
        mess.setPricePerMonth(request.getPricePerMonth());

        return messRepository.save(mess);
    }

    @Override
    public void deleteMess(String id, String ownerId) {

        Mess mess = getMessById(id);

        if (!mess.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        messRepository.delete(mess);
    }

    @Override
    public Mess joinMess(String messId, String userId) {

        Mess mess = getMessById(messId);

        if (!mess.getCustomers().contains(userId)) {
            mess.getCustomers().add(userId);
        }

        return messRepository.save(mess);
    }
}