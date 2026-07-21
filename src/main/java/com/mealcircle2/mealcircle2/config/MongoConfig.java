package com.mealcircle2.mealcircle2.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;

@Configuration
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Ensures the 2dsphere index on the `location` field of the `mess` collection
     * exists before any geoNear query runs.
     *
     * Spring Boot 3.x disables auto-index creation by default; this is our
     * belt-and-suspenders guarantee that the index is always present even on
     * cold-start against an empty or freshly migrated collection.
     */
    @PostConstruct
    public void ensureGeoIndex() {
        try {
            mongoTemplate.indexOps("mess")
                    .createIndex(new GeospatialIndex("location").typed(
                            org.springframework.data.mongodb.core.index.GeoSpatialIndexType.GEO_2DSPHERE));
        } catch (Exception e) {
            // Log but do not crash — the index may already exist or the
            // collection may be empty (index will be created on first insert).
            System.err.println("[MongoConfig] Could not ensure 2dsphere index: " + e.getMessage());
        }
    }
}
