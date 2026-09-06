package com.mealcircle2.mealcircle2.service.serviceImpl;

import com.mealcircle2.mealcircle2.dto.MessRequest;
import com.mealcircle2.mealcircle2.dto.NearbyMessResponse;
import com.mealcircle2.mealcircle2.dto.SubscriptionEmailEvent;
import com.mealcircle2.mealcircle2.messaging.SubscriptionEventProducer;
import com.mealcircle2.mealcircle2.model.Mess;
import com.mealcircle2.mealcircle2.model.Subscription;
import com.mealcircle2.mealcircle2.repository.MessRepository;
import com.mealcircle2.mealcircle2.repository.SubscriptionRepository;
import com.mealcircle2.mealcircle2.service.MessService;
import com.mealcircle2.mealcircle2.util.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MessServiceImpl implements MessService {

    @Autowired
    private MessRepository messRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SubscriptionEventProducer subscriptionEventProducer;

    // ── Helpers ─────────────────────────────────────────────────────────────────

    /** Build a GeoJsonPoint from a lat/lng pair (GeoJSON order: lon, lat). */
    private GeoJsonPoint toGeoJsonPoint(double latitude, double longitude) {
        return new GeoJsonPoint(longitude, latitude);
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────────

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
                .location(toGeoJsonPoint(request.getLatitude(), request.getLongitude()))
                .type(request.getType())
                .todaysMenu(request.getTodaysMenu())
                .notices(request.getNotices())
                .ownerPhone(request.getOwnerPhone())
                .pricePerMonth(request.getPricePerMonth())
                .ownerId(ownerId)
                .subscriptionIds(new ArrayList<>())
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
        mess.setLocation(toGeoJsonPoint(request.getLatitude(), request.getLongitude()));
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

        // Check if user already subscribed
        if (subscriptionRepository.findByCustomerIdAndMessId(userId, messId).isPresent()) {
            throw new RuntimeException("User already subscribed to this mess");
        }

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .customerId(userId)
                .messId(messId)
                .joiningDate(LocalDateTime.now())
                .messEndingDate(LocalDateTime.now().toLocalDate().plusDays(30))
                .absentDates(new ArrayList<>())
                .buffer(10) // default 10 days
                .presentDates(new ArrayList<>())
                .moneyLeftToPay(0.0)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Add subscription ID to mess
        if (mess.getSubscriptionIds() == null) {
            mess.setSubscriptionIds(new ArrayList<>());
        }

        mess.getSubscriptionIds().add(savedSubscription.getId());

        Mess savedMess = messRepository.save(mess);

        // Publish subscription event to RabbitMQ — email consumer handles the actual send
        try {
            SubscriptionEmailEvent event = SubscriptionEmailEvent.builder()
                    .customerEmail(userId)
                    .messName(mess.getMessName())
                    .joiningDate(savedSubscription.getJoiningDate().toLocalDate().toString())
                    .endingDate(savedSubscription.getMessEndingDate().toString())
                    .build();
            subscriptionEventProducer.publishSubscriptionEvent(event);
        } catch (Exception e) {
            System.err.println("[MessService] Could not publish subscription event to RabbitMQ: " + e.getMessage());
        }

        return savedMess;
    }

    @Override
    public Mess getMessByCustomer(String customerId) {
        Subscription subscription = subscriptionRepository.findByCustomerId(customerId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No mess assigned to this customer"));

        return getMessById(subscription.getMessId());
    }

    @Override
    public Mess updateMenuAndNoticeByOwner(String ownerId, String todaysMenu, String notices) {
        Mess mess = getMessByOwner(ownerId);

        if (todaysMenu != null) {
            mess.setTodaysMenu(todaysMenu);
        }

        if (notices != null) {
            mess.setNotices(notices);
        }

        return messRepository.save(mess);
    }

    // ── Geospatial ───────────────────────────────────────────────────────────────

    @Override
    public List<NearbyMessResponse> getMessesNearby(double lat, double lng, double radiusKm) {

        // NearQuery uses Spring Data's Point (x = longitude, y = latitude)
        Point userPoint = new Point(lng, lat);

        NearQuery nearQuery = NearQuery.near(userPoint, Metrics.KILOMETERS)
                .maxDistance(new Distance(radiusKm, Metrics.KILOMETERS))
                .spherical(true);

        // GeoResults is Iterable, not a Stream — use StreamSupport
        GeoResults<Mess> geoResults = mongoTemplate.geoNear(nearQuery, Mess.class);

        return StreamSupport.stream(geoResults.spliterator(), false)
                .map(geoResult -> {
                    double distanceKm = geoResult.getDistance().getValue(); // already in km
                    double distanceMeters = distanceKm * 1000.0;
                    return NearbyMessResponse.builder()
                            .mess(geoResult.getContent())
                            .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                            .distanceMeters(Math.round(distanceMeters))
                            .build();
                })
                .collect(Collectors.toList());
    }
}