package com.group13.population.service;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;
import com.group13.population.repo.PopulationRepo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that PopulationService correctly delegates to PopulationRepo.
 */
class PopulationServiceTest {

    @Test
    void regionReportDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        List<PopulationRow> result = service.getRegionPopulationInOutCities();

        assertEquals(1, repo.regionCalls);
        assertSame(repo.regionRows, result);
    }

    @Test
    void countryReportDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        List<PopulationRow> result = service.getCountryPopulationInOutCities();

        assertEquals(1, repo.countryCalls);
        assertSame(repo.countryRows, result);
    }

    @Test
    void worldPopulationDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        long world = service.getWorldPopulation();

        assertEquals(1, repo.worldCalls);
        assertEquals(repo.worldPopulation, world);
    }

    // --- Stub repo used by tests ----------------------------------------

    private static class StubPopulationRepo extends PopulationRepo {
        final List<PopulationRow> regionRows =
            List.of(PopulationRow.fromTotals("RegionA", 1000, 400));
        final List<PopulationRow> countryRows =
            List.of(PopulationRow.fromTotals("CountryA", 500, 150));
        final long worldPopulation = 123456789L;

        int regionCalls = 0;
        int countryCalls = 0;
        int worldCalls = 0;

        StubPopulationRepo() {
            // Pass a Db instance but we never use it because we override methods.
            super(new Db());
        }

        @Override
        public List<PopulationRow> findPopulationByRegionInOutCities() {
            regionCalls++;
            return regionRows;
        }

        @Override
        public List<PopulationRow> findPopulationByCountryInOutCities() {
            countryCalls++;
            return countryRows;
        }

        @Override
        public long findWorldPopulation() {
            worldCalls++;
            return worldPopulation;
        }
    }
}
