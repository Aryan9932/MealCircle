package com.mealcircle2.mealcircle2.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "mess")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mess {

    @Id
    private String id;

    private String messName;
    private String email;
    private String address;

    // Raw doubles kept for backward compatibility
    private double latitude;
    private double longitude;

    // GeoJSON Point for geospatial queries (coordinates: [longitude, latitude])
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    private String type;

    private String imageUrl;

    private String todaysMenu;
    private String notices;

    private String ownerId;
    private String ownerPhone;

    private List<String> subscriptionIds;

    private double pricePerMonth;
}