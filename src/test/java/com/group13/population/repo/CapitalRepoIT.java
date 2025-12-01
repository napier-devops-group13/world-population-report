package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CityRow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link CapitalRepo} using the real world database.
 *
 * <p>These tests are only meaningful when a MySQL instance is running with the
 * world database. If the DB connection cannot be established, all tests are
 * skipped instead of failing the build.</p>
 *
 * <p>They exercise the capital-city queries used for R17–R22:</p>
 * <ul>
 *   <li>R17 – all capital cities in the world</li>
 *   <li>R18 – all capital cities in a continent</li>
 *   <li>R19 – all capital cities in a region</li>
 *   <li>R20 – top-N capital cities in the world</li>
 *   <li>R21 – top-N capital cities in a continent</li>
 *   <li>R22 – top-N capital cities in a region</li>
 * </ul>
 */
@DisplayName("CapitalRepoIT – DB-backed queries for capital city reports R17–R22")
class CapitalRepoIT {

    private static Db db;
    private static CapitalRepo repo;
    private static boolean dbAvailable;

    @BeforeAll
    static void setUpAll() {
        db = new Db();

        // Allow CI / local overrides:
        //   -Dit.db.location=host:port  OR  IT_DB_LOCATION env var
        String configuredLocation =
            System.getProperty("it.db.location",
                System.getenv("IT_DB_LOCATION"));

        List<String> candidates = new ArrayList<>();
        if (configuredLocation != null && !configuredLocation.isBlank()) {
            candidates.add(configuredLocation.trim());
        } else {
            // Common mappings for the coursework DB
            candidates.add("localhost:43306"); // typical docker-compose mapping
            candidates.add("localhost:33060"); // older lab default
            candidates.add("localhost:3306");  // local MySQL
        }

        String successfulLocation = null;

        // Try each candidate until one really works (returns data)
        for (String candidate : candidates) {
            try {
                db.connect(candidate, 0);

                CapitalRepo probeRepo = new CapitalRepo(db);
                // Small probe query: if this returns at least one row, we
                // assume the DB + world data are available.
                List<CityRow> probeRows =
                    probeRepo.findTopCapitalCitiesInWorldByPopulationDesc(1);

                if (probeRows != null && !probeRows.isEmpty()) {
                    repo = probeRepo;
                    successfulLocation = candidate;
                    dbAvailable = true;
                    System.err.println(
                        "INFO: CapitalRepoIT connected to DB at '" + successfulLocation + "'.");
                    break;
                } else {
                    System.err.println(
                        "INFO: CapitalRepoIT connected to '" + candidate
                            + "' but probe query returned no rows – trying next candidate.");
                }
            } catch (Exception ex) {
                System.err.println(
                    "INFO: CapitalRepoIT could not connect to DB at '"
                        + candidate + "': " + ex.getMessage());
            }
        }

        if (!dbAvailable) {
            System.err.println(
                "WARNING: CapitalRepoIT could not connect to any DB location with data – "
                    + "all capital-city integration tests will be skipped.");
        }
    }

    // ---------------------------------------------------------------------
    // R17 – R22
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R17 – world capitals query returns some rows (if DB available)")
    void r17_worldCapitals_notEmptyWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoIT");
        List<CityRow> rows = repo.findCapitalCitiesInWorldByPopulationDesc();

        assertNotNull(rows, "World capitals list should not be null");
        assertFalse(rows.isEmpty(), "World capitals list should not be empty");
    }

    @Test
    @DisplayName("R18 – continent capitals (Europe) query returns some rows (if DB available)")
    void r18_continentCapitals_notEmptyWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoIT");
        List<CityRow> rows =
            repo.findCapitalCitiesInContinentByPopulationDesc("Europe");

        assertNotNull(rows, "Europe capitals list should not be null");
        assertFalse(rows.isEmpty(), "Europe capitals list should not be empty");
    }

    @Test
    @DisplayName("R19 – region capitals (Caribbean) query returns some rows (if DB available)")
    void r19_regionCapitals_notEmptyWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoIT");
        List<CityRow> rows =
            repo.findCapitalCitiesInRegionByPopulationDesc("Caribbean");

        assertNotNull(rows, "Caribbean capitals list should not be null");
        assertFalse(rows.isEmpty(), "Caribbean capitals list should not be empty");
    }

    @Test
    @DisplayName("R20 – top-10 world capitals query honours limit (if DB available)")
    void r20_topWorldCapitals_respectsLimitWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoIT");
        List<CityRow> rows =
            repo.findTopCapitalCitiesInWorldByPopulationDesc(10);

        assertNotNull(rows, "Top-10 world capitals list should not be null");
        assertFalse(rows.isEmpty(), "Top-10 world capitals list should not be empty");
        assertTrue(rows.size() <= 10,
            "Top-10 world capitals should have at most 10 rows");
    }

    @Test
    @DisplayName("R21 – top-5 Europe capitals query honours limit (if DB available)")
    void r21_topContinentCapitals_respectsLimitWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoIT");
        List<CityRow> rows =
            repo.findTopCapitalCitiesInContinentByPopulationDesc("Europe", 5);

        assertNotNull(rows, "Top-5 Europe capitals list should not be null");
        assertFalse(rows.isEmpty(), "Top-5 Europe capitals list should not be empty");
        assertTrue(rows.size() <= 5,
            "Top-5 Europe capitals should have at most 5 rows");
    }

    @Test
    @DisplayName("R22 – top-3 Caribbean capitals query honours limit (if DB available)")
    void r22_topRegionCapitals_respectsLimitWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoIT");
        List<CityRow> rows =
            repo.findTopCapitalCitiesInRegionByPopulationDesc("Caribbean", 3);

        assertNotNull(rows, "Top-3 Caribbean capitals list should not be null");
        assertFalse(rows.isEmpty(), "Top-3 Caribbean capitals list should not be empty");
        assertTrue(rows.size() <= 3,
            "Top-3 Caribbean capitals should have at most 3 rows");
    }
}
