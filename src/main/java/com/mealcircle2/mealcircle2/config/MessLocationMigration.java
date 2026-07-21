package com.mealcircle2.mealcircle2.config;

import com.mealcircle2.mealcircle2.model.Mess;
import com.mealcircle2.mealcircle2.repository.MessRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.List;

/**
 * One-time migration: backfills the GeoJsonPoint {@code location} field for
 * any existing Mess documents that have {@code latitude}/{@code longitude} but
 * were saved before the {@code location} field was added to the model.
 *
 * <p>This runner is idempotent — it skips documents that already have a
 * non-null {@code location}.
 */
@Configuration
public class MessLocationMigration {

    @Bean
    public CommandLineRunner backfillMessLocations(MessRepository messRepository) {
        return args -> {
            List<Mess> allMesses = messRepository.findAll();
            int fixed = 0;

            for (Mess mess : allMesses) {
                if (mess.getLocation() == null
                        && (mess.getLatitude() != 0.0 || mess.getLongitude() != 0.0)) {

                    // GeoJSON Point: coordinates are [longitude, latitude]
                    mess.setLocation(new GeoJsonPoint(mess.getLongitude(), mess.getLatitude()));
                    messRepository.save(mess);
                    fixed++;
                    System.out.printf(
                            "[MessLocationMigration] Fixed mess '%s' (id=%s): location set to [%.4f, %.4f]%n",
                            mess.getMessName(), mess.getId(), mess.getLongitude(), mess.getLatitude());
                }
            }

            if (fixed == 0) {
                System.out.println("[MessLocationMigration] All messes already have a location field — nothing to migrate.");
            } else {
                System.out.printf("[MessLocationMigration] Migration complete: %d mess(es) updated.%n", fixed);
            }
        };
    }
}
