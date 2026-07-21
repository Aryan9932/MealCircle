package com.mealcircle2.mealcircle2.dto;

import com.mealcircle2.mealcircle2.model.Mess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyMessResponse {

    // Full mess details
    private Mess mess;

    // Distance from user's location
    private double distanceMeters;
    private double distanceKm;
}
