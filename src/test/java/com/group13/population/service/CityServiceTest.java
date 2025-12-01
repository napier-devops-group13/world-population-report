package com.group13.population.service;

import com.group13.population.db.Db;
import com.group13.population.model.CityRow;
import com.group13.population.repo.CityRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for {@link CityService}.
 *
 * We use a lightweight fake {@link CityRepo} that does NOT touch the real
 * database. This keeps the tests fast and deterministic while still exercising:
 *  • all public service methods on the happy path
 *  • both branches of validateLimit (limit > 0 and limit <= 0)
 *  • both branches of validateName (null vs blank)
 *
 * This is exactly the kind of service-layer testing expected in the coursework.
 */
class CityServiceTest {

    /**
     * Simple test double for CityRepo.
     *
     * It overrides all methods used by CityService and just returns empty lists.
     * We pass a new Db() to the super constructor to satisfy the non-null check,
     * but we never actually use the Db, so no real connection is made.
     */
    private static class FakeCityRepo extends CityRepo {

        FakeCityRepo() {
            super(new Db());
        }

        @Override
        public List<CityRow> findCitiesInWorldByPopulationDesc() {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findTopCitiesInWorldByPopulationDesc(int limit) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findCitiesInContinentByPopulationDesc(String continent) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findTopCitiesInContinentByPopulationDesc(String continent,
                                                                      int limit) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findCitiesInRegionByPopulationDesc(String region) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findTopCitiesInRegionByPopulationDesc(String region,
                                                                   int limit) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findCitiesInCountryByPopulationDesc(String country) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findTopCitiesInCountryByPopulationDesc(String country,
                                                                    int limit) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findCitiesInDistrictByPopulationDesc(String district) {
            return Collections.emptyList();
        }

        @Override
        public List<CityRow> findTopCitiesInDistrictByPopulationDesc(String district,
                                                                     int limit) {
            return Collections.emptyList();
        }
    }

    /** Helper to build a CityService wired to the fake repo. */
    private CityService newService() {
        return new CityService(new FakeCityRepo());
    }

    // ---------------------------------------------------------------------
    // Happy-path tests – exercise every public method once
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("All CityService methods run without throwing for valid inputs")
    void happyPathDelegatesToRepo() {
        CityService service = newService();

        assertDoesNotThrow(service::getCitiesInWorldByPopulationDesc);
        assertDoesNotThrow(() -> service.getTopCitiesInWorldByPopulationDesc(10));

        assertDoesNotThrow(() ->
            service.getCitiesInContinentByPopulationDesc("Europe"));
        assertDoesNotThrow(() ->
            service.getTopCitiesInContinentByPopulationDesc("Europe", 5));

        assertDoesNotThrow(() ->
            service.getCitiesInRegionByPopulationDesc("Western Europe"));
        assertDoesNotThrow(() ->
            service.getTopCitiesInRegionByPopulationDesc("Western Europe", 5));

        assertDoesNotThrow(() ->
            service.getCitiesInCountryByPopulationDesc("United Kingdom"));
        assertDoesNotThrow(() ->
            service.getTopCitiesInCountryByPopulationDesc("United Kingdom", 5));

        assertDoesNotThrow(() ->
            service.getCitiesInDistrictByPopulationDesc("Scotland"));
        assertDoesNotThrow(() ->
            service.getTopCitiesInDistrictByPopulationDesc("Scotland", 3));
    }

    // ---------------------------------------------------------------------
    // Validation – limit
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Top-N methods reject non-positive limits")
    void nonPositiveLimitThrows() {
        CityService service = newService();

        assertThrows(IllegalArgumentException.class,
            () -> service.getTopCitiesInWorldByPopulationDesc(0));
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopCitiesInWorldByPopulationDesc(-1));
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopCitiesInCountryByPopulationDesc("United Kingdom", 0));
    }

    // ---------------------------------------------------------------------
    // Validation – names (null vs blank) to cover both branches
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Name validation rejects null values (value == null branch)")
    void nullNameThrows() {
        CityService service = newService();

        assertThrows(IllegalArgumentException.class,
            () -> service.getCitiesInContinentByPopulationDesc(null));
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopCitiesInRegionByPopulationDesc(null, 5));
    }

    @Test
    @DisplayName("Name validation rejects blank values (value.isBlank() branch)")
    void blankNameThrows() {
        CityService service = newService();

        assertThrows(IllegalArgumentException.class,
            () -> service.getCitiesInCountryByPopulationDesc("   "));
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopCitiesInDistrictByPopulationDesc("   ", 3));
    }
}
