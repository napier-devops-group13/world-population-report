package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for {@link PopulationRepo} using the real {@code world}
 * database.
 *
 * <p>These tests focus on the happy-path population reports for R24–R25.
 * Guard and failure cases are covered in PopulationRepoGuardTest and
 * PopulationRepoNoRowsTest.</p>
 *
 * <p>Connection details are taken from DB_HOST / DB_PORT when present so the
 * tests run both locally (Docker on localhost:43306) and in GitHub Actions
 * (MySQL service on a dynamic port).</p>
 */
class PopulationRepoTest {

    private static Db db;
    private static PopulationRepo repo;

    @BeforeAll
    @DisplayName("Connect to database before running PopulationRepo tests")
    static void setUp() {
        db = new Db();

        String host = getenvOrDefault("DB_HOST", "localhost");
        String port = getenvOrDefault("DB_PORT", "43306");
        String location = host + ":" + port;

        boolean connected = db.connect(location, 30_000);
        assertTrue(connected, "Failed to connect to database at " + location);

        repo = new PopulationRepo(db);
    }

    @AfterAll
    static void tearDown() {
        if (db != null) {
            db.disconnect();
        }
    }

    // -------------------------------------------------------------------------
    // Region report (R24) – population in / out of cities per region
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Region population report returns data ordered by total population DESC")
    void regionReportHasDataAndIsOrdered() {
        List<PopulationRow> rows = repo.findPopulationByRegionInOutCities();

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Expected at least one region row");

        long prev = Long.MAX_VALUE;
        for (PopulationRow row : rows) {
            assertNotNull(row.getName(), "Region name should be set");
            assertTrue(row.getTotalPopulation() >= 0,
                    "Total population should be non-negative");
            assertTrue(row.getLivingInCities() >= 0);
            assertTrue(row.getNotLivingInCities() >= 0);

            assertTrue(
                    row.getTotalPopulation() <= prev,
                    "Rows should be ordered by total population DESC"
            );
            prev = row.getTotalPopulation();
        }
    }

    // -------------------------------------------------------------------------
    // Country report (R25) – population in / out of cities per country
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Country population report returns data ordered by total population DESC")
    void countryReportHasDataAndIsOrdered() {
        List<PopulationRow> rows = repo.findPopulationByCountryInOutCities();

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Expected at least one country row");

        long prev = Long.MAX_VALUE;
        for (PopulationRow row : rows) {
            assertNotNull(row.getName(), "Country name should be set");
            assertTrue(row.getTotalPopulation() >= 0,
                    "Total population should be non-negative");
            assertTrue(row.getLivingInCities() >= 0);
            assertTrue(row.getNotLivingInCities() >= 0);

            assertTrue(
                    row.getTotalPopulation() <= prev,
                    "Rows should be ordered by total population DESC"
            );
            prev = row.getTotalPopulation();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
