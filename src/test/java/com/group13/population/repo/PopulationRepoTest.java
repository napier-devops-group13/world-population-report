package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Happy-path tests for {@link PopulationRepo}.
 *
 * These connect to the real MySQL "world" database via Docker and
 * exercise the normal success paths of the repository methods.
 *
 * Combined with PopulationRepoGuardTest (error paths) this should
 * give very high coverage (>90%) for PopulationRepo.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PopulationRepoTest {

    private Db db;
    private PopulationRepo repo;

    @BeforeAll
    void setUp() {
        db = new Db();

        // 🔴 Use the same host:port as your docker-compose for MySQL.
        // From your earlier setup this should be 43306 -> 3306:
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
    void findPopulationByRegionInOutCities_returnsNonEmptyList() {
        List<PopulationRow> regions = repo.findPopulationByRegionInOutCities();

        assertNotNull(regions, "Regions list must not be null");
        assertFalse(regions.isEmpty(), "Expected at least one region row");

        // If PopulationRow has getters you can uncomment to hit more code:
        // for (PopulationRow row : regions) {
        //     assertTrue(row.getTotalPopulation() > 0);
        //     assertEquals(row.getInCities() + row.getNotInCities(),
        //                  row.getTotalPopulation());
        // }
    }

    @Test
    void findPopulationByCountryInOutCities_returnsNonEmptyList() {
        List<PopulationRow> countries = repo.findPopulationByCountryInOutCities();

        assertNotNull(countries, "Countries list must not be null");
        assertFalse(countries.isEmpty(), "Expected at least one country row");
    }

    @Test
    void findWorldPopulation_returnsPositiveValue() {
        long world = repo.findWorldPopulation();
        assertTrue(world > 0, "World population should be positive");
    }
}
