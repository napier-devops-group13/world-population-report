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
 * <p>They exercise the capital-city queries used for R17–R22, including both
 * the current “capitalCities” methods and the older “capitals” methods so
 * that all code paths in {@link CapitalRepo} are covered for the coursework
 * (and for JaCoCo / Codecov evidence).</p>
 */
@DisplayName("CapitalRepoTest – DB-backed queries for capital city reports R17–R22")
class CapitalRepoTest {

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
                        "INFO: CapitalRepoTest connected to DB at '" + successfulLocation + "'.");
                    break;
                } else {
                    System.err.println(
                        "INFO: CapitalRepoTest connected to '" + candidate
                            + "' but probe query returned no rows – trying next candidate.");
                }
            } catch (Exception ex) {
                System.err.println(
                    "INFO: CapitalRepoTest could not connect to DB at '"
                        + candidate + "': " + ex.getMessage());
            }
        }

        if (!dbAvailable) {
            System.err.println(
                "WARNING: CapitalRepoTest could not connect to any DB location with data – "
                    + "all capital-city integration tests will be skipped.");
        }
    }

    // ---------------------------------------------------------------------
    // R17 – R22 (current “capitalCities…” API – already used by routes)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R17 – world capitals query returns some rows (if DB available)")
    void r17_worldCapitals_notEmptyWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");
        List<CityRow> rows = repo.findCapitalCitiesInWorldByPopulationDesc();

        assertNotNull(rows, "World capitals list should not be null");
        assertFalse(rows.isEmpty(), "World capitals list should not be empty");
    }

    @Test
    @DisplayName("R18 – continent capitals (Europe) query returns some rows (if DB available)")
    void r18_continentCapitals_notEmptyWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");
        List<CityRow> rows =
            repo.findCapitalCitiesInContinentByPopulationDesc("Europe");

        assertNotNull(rows, "Europe capitals list should not be null");
        assertFalse(rows.isEmpty(), "Europe capitals list should not be empty");
    }

    @Test
    @DisplayName("R19 – region capitals (Caribbean) query returns some rows (if DB available)")
    void r19_regionCapitals_notEmptyWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");
        List<CityRow> rows =
            repo.findCapitalCitiesInRegionByPopulationDesc("Caribbean");

        assertNotNull(rows, "Caribbean capitals list should not be null");
        assertFalse(rows.isEmpty(), "Caribbean capitals list should not be empty");
    }

    @Test
    @DisplayName("R20 – top-10 world capitals query honours limit (if DB available)")
    void r20_topWorldCapitals_respectsLimitWhenDbAvailable() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");
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
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");
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
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");
        List<CityRow> rows =
            repo.findTopCapitalCitiesInRegionByPopulationDesc("Caribbean", 3);

        assertNotNull(rows, "Top-3 Caribbean capitals list should not be null");
        assertFalse(rows.isEmpty(), "Top-3 Caribbean capitals list should not be empty");
        assertTrue(rows.size() <= 3,
            "Top-3 Caribbean capitals should have at most 3 rows");
    }

    // ---------------------------------------------------------------------
    // Extra coverage: legacy “capitals…” methods (no “Cities” in name)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Legacy: findCapitalsInWorldByPopulationDesc matches capitalCities version")
    void legacy_worldCapitals_matchesNewMethod() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");

        List<CityRow> legacy = repo.findCapitalsInWorldByPopulationDesc();
        List<CityRow> modern = repo.findCapitalCitiesInWorldByPopulationDesc();

        assertNotNull(legacy);
        assertEquals(modern.size(), legacy.size(), "Legacy/new world capitals size mismatch");
        assertFalse(legacy.isEmpty(), "Legacy world capitals should not be empty");
        assertEquals(modern.get(0).getName(), legacy.get(0).getName());
    }

    @Test
    @DisplayName("Legacy: findCapitalsInContinentByPopulationDesc matches capitalCities version")
    void legacy_continentCapitals_matchesNewMethod() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");

        String continent = "Europe";
        List<CityRow> legacy =
            repo.findCapitalsInContinentByPopulationDesc(continent);
        List<CityRow> modern =
            repo.findCapitalCitiesInContinentByPopulationDesc(continent);

        assertNotNull(legacy);
        assertEquals(modern.size(), legacy.size(),
            "Legacy/new continent capitals size mismatch");
        assertFalse(legacy.isEmpty(), "Legacy Europe capitals should not be empty");
        assertEquals(modern.get(0).getName(), legacy.get(0).getName());
    }

    @Test
    @DisplayName("Legacy: findCapitalsInRegionByPopulationDesc matches capitalCities version")
    void legacy_regionCapitals_matchesNewMethod() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");

        String region = "Caribbean";
        List<CityRow> legacy =
            repo.findCapitalsInRegionByPopulationDesc(region);
        List<CityRow> modern =
            repo.findCapitalCitiesInRegionByPopulationDesc(region);

        assertNotNull(legacy);
        assertEquals(modern.size(), legacy.size(),
            "Legacy/new region capitals size mismatch");
        assertFalse(legacy.isEmpty(), "Legacy Caribbean capitals should not be empty");
        assertEquals(modern.get(0).getName(), legacy.get(0).getName());
    }

    @Test
    @DisplayName("Legacy: findTopCapitalsInWorldByPopulationDesc matches capitalCities version")
    void legacy_topWorldCapitals_matchesNewMethod() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");

        int limit = 10;
        List<CityRow> legacy =
            repo.findTopCapitalsInWorldByPopulationDesc(limit);
        List<CityRow> modern =
            repo.findTopCapitalCitiesInWorldByPopulationDesc(limit);

        assertNotNull(legacy);
        assertTrue(legacy.size() <= limit, "Legacy top world capitals exceeds limit");
        assertEquals(modern.size(), legacy.size(),
            "Legacy/new top world capitals size mismatch");
    }

    @Test
    @DisplayName("Legacy: findTopCapitalsInContinentByPopulationDesc matches capitalCities version")
    void legacy_topContinentCapitals_matchesNewMethod() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");

        int limit = 5;
        String continent = "Europe";
        List<CityRow> legacy =
            repo.findTopCapitalsInContinentByPopulationDesc(continent, limit);
        List<CityRow> modern =
            repo.findTopCapitalCitiesInContinentByPopulationDesc(continent, limit);

        assertNotNull(legacy);
        assertTrue(legacy.size() <= limit, "Legacy top Europe capitals exceeds limit");
        assertEquals(modern.size(), legacy.size(),
            "Legacy/new top Europe capitals size mismatch");
    }

    @Test
    @DisplayName("Legacy: findTopCapitalsInRegionByPopulationDesc matches capitalCities version")
    void legacy_topRegionCapitals_matchesNewMethod() {
        assumeTrue(dbAvailable, "DB not available – skipping CapitalRepoTest");

        int limit = 3;
        String region = "Caribbean";
        List<CityRow> legacy =
            repo.findTopCapitalsInRegionByPopulationDesc(region, limit);
        List<CityRow> modern =
            repo.findTopCapitalCitiesInRegionByPopulationDesc(region, limit);

        assertNotNull(legacy);
        assertTrue(legacy.size() <= limit, "Legacy top Caribbean capitals exceeds limit");
        assertEquals(modern.size(), legacy.size(),
            "Legacy/new top Caribbean capitals size mismatch");
    }
}
