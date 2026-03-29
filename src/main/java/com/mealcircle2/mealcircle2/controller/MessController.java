package com.mealcircle2.mealcircle2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealcircle2.mealcircle2.dto.MessRequest;
import com.mealcircle2.mealcircle2.service.MessService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mess")
public class MessController {

    @Autowired
    private MessService messService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMess(
            @RequestPart("data") MessRequest request,
            @RequestPart("image") MultipartFile file
    ) {
        String ownerId = "testOwner";
        return ResponseEntity.ok(messService.createMess(request, ownerId, file));
    }
}