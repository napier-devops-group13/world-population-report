package com.group13.population.service;

import com.group13.population.db.Db;
import com.group13.population.model.LanguagePopulationRow;
import com.group13.population.model.PopulationLookupRow;
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

    // ---------------------------------------------------------------------
    // New tests: R27–R31 – lookup reports
    // ---------------------------------------------------------------------

    @Test
    void continentPopulationDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        PopulationLookupRow row = service.getContinentPopulation("Asia");

        assertEquals(1, repo.continentCalls);
        assertEquals("Asia", repo.lastContinentArg);
        assertSame(repo.continentRow, row);
    }

    @Test
    void regionPopulationDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        PopulationLookupRow row = service.getRegionPopulation("Southeast Asia");

        assertEquals(1, repo.regionLookupCalls);
        assertEquals("Southeast Asia", repo.lastRegionArg);
        assertSame(repo.regionLookupRow, row);
    }

    @Test
    void countryPopulationDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        PopulationLookupRow row = service.getCountryPopulation("Myanmar");

        assertEquals(1, repo.countryLookupCalls);
        assertEquals("Myanmar", repo.lastCountryArg);
        assertSame(repo.countryLookupRow, row);
    }

    @Test
    void districtPopulationDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        PopulationLookupRow row = service.getDistrictPopulation("Yangon");

        assertEquals(1, repo.districtCalls);
        assertEquals("Yangon", repo.lastDistrictArg);
        assertSame(repo.districtRow, row);
    }

    @Test
    void cityPopulationDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        PopulationLookupRow row = service.getCityPopulation("Yangon");

        assertEquals(1, repo.cityCalls);
        assertEquals("Yangon", repo.lastCityArg);
        assertSame(repo.cityRow, row);
    }

    // ---------------------------------------------------------------------
    // New test: R32 – language report
    // ---------------------------------------------------------------------

    @Test
    void languageReportDelegatesToRepo() {
        StubPopulationRepo repo = new StubPopulationRepo();
        PopulationService service = new PopulationService(repo);

        List<LanguagePopulationRow> result = service.getLanguagePopulations();

        assertEquals(1, repo.languageCalls);
        assertSame(repo.languageRows, result);
    }

    // --- Stub repo used by tests ----------------------------------------

    private static class StubPopulationRepo extends PopulationRepo {
        // Existing rows for R24–R26
        final List<PopulationRow> regionRows =
            List.of(PopulationRow.fromTotals("RegionA", 1000, 400));
        final List<PopulationRow> countryRows =
            List.of(PopulationRow.fromTotals("CountryA", 500, 150));
        final long worldPopulation = 123456789L;

        int regionCalls = 0;
        int countryCalls = 0;
        int worldCalls = 0;

        // Lookup rows for R27–R31
        final PopulationLookupRow continentRow =
            PopulationLookupRow.of("Asia", 1000L);
        final PopulationLookupRow regionLookupRow =
            PopulationLookupRow.of("Southeast Asia", 800L);
        final PopulationLookupRow countryLookupRow =
            PopulationLookupRow.of("Myanmar", 600L);
        final PopulationLookupRow districtRow =
            PopulationLookupRow.of("Yangon", 400L);
        final PopulationLookupRow cityRow =
            PopulationLookupRow.of("Yangon", 300L);

        int continentCalls = 0;
        int regionLookupCalls = 0;
        int countryLookupCalls = 0;
        int districtCalls = 0;
        int cityCalls = 0;

        String lastContinentArg;
        String lastRegionArg;
        String lastCountryArg;
        String lastDistrictArg;
        String lastCityArg;

        // Language rows for R32
        final List<LanguagePopulationRow> languageRows =
            List.of(
                LanguagePopulationRow.fromWorldTotal("Chinese", 4000L, 8000L),
                LanguagePopulationRow.fromWorldTotal("English", 2000L, 8000L)
            );
        int languageCalls = 0;

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

        // ---- Lookup overrides (R27–R31) ---------------------------------

        @Override
        public PopulationLookupRow findContinentPopulation(String continent) {
            continentCalls++;
            lastContinentArg = continent;
            return continentRow;
        }

        @Override
        public PopulationLookupRow findRegionPopulation(String region) {
            regionLookupCalls++;
            lastRegionArg = region;
            return regionLookupRow;
        }

        @Override
        public PopulationLookupRow findCountryPopulation(String countryName) {
            countryLookupCalls++;
            lastCountryArg = countryName;
            return countryLookupRow;
        }

        @Override
        public PopulationLookupRow findDistrictPopulation(String district) {
            districtCalls++;
            lastDistrictArg = district;
            return districtRow;
        }

        @Override
        public PopulationLookupRow findCityPopulation(String cityName) {
            cityCalls++;
            lastCityArg = cityName;
            return cityRow;
        }

        // ---- Language override (R32) ------------------------------------

        @Override
        public List<LanguagePopulationRow> findLanguagePopulations() {
            languageCalls++;
            return languageRows;
        }
    }
}
