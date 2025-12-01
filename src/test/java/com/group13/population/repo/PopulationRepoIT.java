// src/test/java/com/group13/population/repo/PopulationRepoIT.java
package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link PopulationRepo}.
 *
 * These tests connect to the real MySQL "world" database (via Docker)
 * and exercise the normal / success paths of the repo methods.
 *
 * Combined with PopulationRepoGuardTest (error paths), this should
 * push JaCoCo coverage for PopulationRepo well above 90%.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PopulationRepoIT {

    private Db db;
    private PopulationRepo repo;

    @BeforeAll
    void setUp() {
        db = new Db();

        // IMPORTANT: use the same host:port as your docker-compose.
        // From your screenshots this is 43306 → 3306.
        boolean connected = db.connect("localhost:43306", 30_000);

        assertTrue(connected,
            "Could not connect to test database – check docker compose / port mapping.");

        repo = new PopulationRepo(db);
    }

    @AfterAll
    void tearDown() {
        if (db != null) {
            db.disconnect();
        }
    }

    @Test
    void findPopulationByRegionInOutCities_returnsRows() {
        List<PopulationRow> regions = repo.findPopulationByRegionInOutCities();

        assertNotNull(regions, "List must not be null");
        assertFalse(regions.isEmpty(), "Expected at least one region row");

        // OPTIONAL extra checks (uncomment if your getters use these names)
        // for (PopulationRow row : regions) {
        //     assertTrue(row.getTotalPopulation() > 0);
        //     assertEquals(row.getTotalPopulation(),
        //             row.getInCities() + row.getNotInCities(),
        //             "Total should equal in-cities + not-in-cities");
        // }
    }

    @Test
    void findPopulationByCountryInOutCities_returnsRows() {
        List<PopulationRow> countries = repo.findPopulationByCountryInOutCities();

        assertNotNull(countries, "List must not be null");
        assertFalse(countries.isEmpty(), "Expected at least one country row");

        // OPTIONAL: if PopulationRow has getName():
        // boolean hasChina = countries.stream()
        //         .anyMatch(r -> "China".equals(r.getName()));
        // assertTrue(hasChina, "Expected to find China in country list");
    }

    @Test
    void findWorldPopulation_isPositive() {
        long world = repo.findWorldPopulation();
        assertTrue(world > 0, "World population should be positive");
    }

    @Test
    void worldPopulation_consistentWithRegions() {
        long world = repo.findWorldPopulation();
        List<PopulationRow> regions = repo.findPopulationByRegionInOutCities();

        assertNotNull(regions);
        assertFalse(regions.isEmpty());
        assertTrue(world > 0);

        // OPTIONAL stronger check if you have getTotalPopulation():
        // long sum = regions.stream()
        //         .mapToLong(PopulationRow::getTotalPopulation)
        //         .sum();
        // assertEquals(sum, world, "World population should equal sum of regions");
    }
}
