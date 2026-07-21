package com.mealcircle2.mealcircle2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mealcircle2.mealcircle2.dto.MenuNoticeUpdateRequest;
import com.mealcircle2.mealcircle2.dto.MessRequest;
import com.mealcircle2.mealcircle2.dto.NearbyMessResponse;
import com.mealcircle2.mealcircle2.service.MessService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/mess")
public class MessController {

    private final MessService messService;
    private final ObjectMapper objectMapper;

    public MessController(MessService messService, ObjectMapper objectMapper) {
        this.messService = messService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMess(
            @RequestPart(value = "data", required = false) String data,
            @RequestPart(value = "image", required = false) MultipartFile file,
            @RequestParam(required = false) Map<String, String> formFields,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Image file is required");
        }

        try {
            MessRequest request = null;

            // Accept JSON sent in `data` form field.
            if (data != null && !data.isBlank()) {
                JsonNode root = objectMapper.readTree(data);
                if (root.isObject()) {
                    normalizeArrayField((ObjectNode) root, "todaysMenu");
                    normalizeArrayField((ObjectNode) root, "notices");
                }
                request = objectMapper.treeToValue(root, MessRequest.class);
            }

            // Fallback: accept direct form-data keys (messName, email, etc.).
            if (request == null && formFields != null && !formFields.isEmpty()) {
                formFields.remove("data");
                request = objectMapper.convertValue(formFields, MessRequest.class);
            }

            if (request == null || request.getMessName() == null || request.getMessName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Invalid request payload: send either `data` JSON or direct form fields (messName, email, address, latitude, longitude, type, todaysMenu, notices, ownerPhone, pricePerMonth)");
            }

            String ownerId = authentication.getName();
            return ResponseEntity.ok(messService.createMess(request, ownerId, file));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid request payload: " + e.getOriginalMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
    }

    private void normalizeArrayField(ObjectNode root, String fieldName) {
        JsonNode field = root.get(fieldName);
        if (field != null && field.isArray()) {
            String merged = StreamSupport.stream(field.spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.joining(", "));
            root.put(fieldName, merged);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllMesses() {
        return ResponseEntity.ok(messService.getAllMesses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMessById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(messService.getMessById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/owner/my-mess")
    public ResponseEntity<?> getOwnerMess(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            return ResponseEntity.ok(messService.getMessByOwner(authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{messId}/join")
    public ResponseEntity<?> joinMess(@PathVariable String messId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Direct join is disabled. Complete payment using /api/payment/razorpay/verify-and-join");
    }

    @GetMapping("/customer/my-mess")
    public ResponseEntity<?> getMyMess(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String customerId = authentication.getName();
            return ResponseEntity.ok(messService.getMessByCustomer(customerId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/owner/menu-notice")
    public ResponseEntity<?> updateMenuAndNotice(
            @RequestBody MenuNoticeUpdateRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        if ((request.getTodaysMenu() == null || request.getTodaysMenu().isBlank())
                && (request.getNotices() == null || request.getNotices().isBlank())) {
            return ResponseEntity.badRequest().body("Provide at least one field: todaysMenu or notices");
        }

        try {
            String ownerId = authentication.getName();
            return ResponseEntity.ok(
                    messService.updateMenuAndNoticeByOwner(ownerId, request.getTodaysMenu(), request.getNotices()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMess(
            @PathVariable String id,
            @RequestPart(value = "data", required = false) String data,
            @RequestPart(value = "image", required = false) MultipartFile file,
            @RequestParam(required = false) Map<String, String> formFields,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            MessRequest request = null;

            if (data != null && !data.isBlank()) {
                JsonNode root = objectMapper.readTree(data);
                if (root.isObject()) {
                    normalizeArrayField((ObjectNode) root, "todaysMenu");
                    normalizeArrayField((ObjectNode) root, "notices");
                }
                request = objectMapper.treeToValue(root, MessRequest.class);
            }

            if (request == null && formFields != null && !formFields.isEmpty()) {
                formFields.remove("data");
                request = objectMapper.convertValue(formFields, MessRequest.class);
            }

            if (request == null || request.getMessName() == null || request.getMessName().isBlank()) {
                return ResponseEntity.badRequest().body("Invalid request payload for mess update");
            }

            return ResponseEntity.ok(messService.updateMess(id, request, authentication.getName(), file));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid request payload: " + e.getOriginalMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMess(@PathVariable String id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            messService.deleteMess(id, authentication.getName());
            return ResponseEntity.ok("Mess deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/mess/nearby?lat={lat}&lng={lng}&radius={radiusKm}
     *
     * Returns messes within {@code radius} km (default 5 km) of the given
     * coordinates, sorted nearest-first, each enriched with distanceKm.
     */
    @GetMapping("/nearby")
    public ResponseEntity<?> getMessesNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {

        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            return ResponseEntity.badRequest().body("Invalid coordinates");
        }
        if (radius <= 0 || radius > 100) {
            return ResponseEntity.badRequest().body("Radius must be between 0 and 100 km");
        }

        try {
            List<NearbyMessResponse> results = messService.getMessesNearby(lat, lng, radius);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}