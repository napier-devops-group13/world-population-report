package com.group13.population.service;

import com.group13.population.model.City;
import com.group13.population.repo.CityRepo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CityService}.
 *
 * Mirrors the style used for CountryService / CapitalService tests:
 *  - verifies delegation to the repository
 *  - checks that helper logic (safe / positive) behaves as expected
 *  - uses a simple recording fake instead of real JDBC
 */
class CityServiceTest {

    private static final City SAMPLE_CITY =
        new City("TestCity", "TestCountry", "TestDistrict", 1234L);

    @Test
    void constructorRejectsNullRepo() {
        assertThrows(NullPointerException.class, () -> new CityService(null));
    }

    @Test
    void worldAllDelegatesToRepoAndReturnsRows() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);

        List<City> result = service.worldAll();

        assertEquals(List.of(SAMPLE_CITY), result);
        assertEquals("worldAll", repo.lastMethod);
    }

    @Test
    void continentAllTrimsWhitespaceBeforeDelegating() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);

        service.continentAll("  Europe  ");

        assertEquals("continentAll", repo.lastMethod);
        assertEquals("Europe", repo.lastContinent);
    }

    @Test
    void continentAllTreatsNullAsEmptyString() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);

        service.continentAll(null);

        assertEquals("continentAll", repo.lastMethod);
        assertEquals("", repo.lastContinent,
            "safe(null) should become an empty string for the repo");
    }

    @Test
    void worldTopNUsesPositiveHelperForNonPositiveValues() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);

        List<City> result = service.worldTopN(0); // non-positive

        assertEquals(List.of(SAMPLE_CITY), result);
        assertEquals("worldTopN", repo.lastMethod);
        assertEquals(1, repo.lastN,
            "positive(0) should result in the repo being called with n = 1");
    }

    @Test
    void continentTopNTrimsAndUsesPositiveHelper() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);

        List<City> result = service.continentTopN("  Asia  ", -5);

        assertEquals(List.of(SAMPLE_CITY), result);
        assertEquals("continentTopN", repo.lastMethod);
        assertEquals("Asia", repo.lastContinent);
        assertEquals(1, repo.lastN,
            "negative n should be normalised to 1 before calling the repo");
    }

    // ---------------------------------------------------------------------
    // Simple recording fake for CityRepo used by the tests above.
    // ---------------------------------------------------------------------

    private static final class RecordingCityRepo implements CityRepo {

        private final List<City> result;

        String lastMethod;
        String lastContinent;
        String lastRegion;
        String lastCountry;
        String lastDistrict;
        Integer lastN;

        RecordingCityRepo(List<City> result) {
            this.result = result;
        }

        @Override
        public List<City> worldAll() {
            lastMethod = "worldAll";
            return result;
        }

        @Override
        public List<City> continentAll(String continent) {
            lastMethod = "continentAll";
            lastContinent = continent;
            return result;
        }

        @Override
        public List<City> regionAll(String region) {
            lastMethod = "regionAll";
            lastRegion = region;
            return result;
        }

        @Override
        public List<City> countryAll(String country) {
            lastMethod = "countryAll";
            lastCountry = country;
            return result;
        }

        @Override
        public List<City> districtAll(String district) {
            lastMethod = "districtAll";
            lastDistrict = district;
            return result;
        }

        @Override
        public List<City> worldTopN(int n) {
            lastMethod = "worldTopN";
            lastN = n;
            return result;
        }

        @Override
        public List<City> continentTopN(String continent, int n) {
            lastMethod = "continentTopN";
            lastContinent = continent;
            lastN = n;
            return result;
        }

        @Override
        public List<City> regionTopN(String region, int n) {
            lastMethod = "regionTopN";
            lastRegion = region;
            lastN = n;
            return result;
        }

        @Override
        public List<City> countryTopN(String country, int n) {
            lastMethod = "countryTopN";
            lastCountry = country;
            lastN = n;
            return result;
        }

        @Override
        public List<City> districtTopN(String district, int n) {
            lastMethod = "districtTopN";
            lastDistrict = district;
            lastN = n;
            return result;
        }
    }
}
