package com.group13.population.service;

import com.group13.population.model.CountryRow;
import com.group13.population.repo.WorldRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CountryService}.
 *
 * <p>These tests focus on:</p>
 * <ul>
 *   <li>Correct delegation from service to {@link WorldRepo}.</li>
 *   <li>Guard behaviour for null/blank filters.</li>
 *   <li>Guard behaviour and clamping for Top-N limits.</li>
 *   <li>String-based limit parsing used by the web layer.</li>
 *   <li>Coverage of the HTTP alias methods (worldByPopulation, etc.).</li>
 * </ul>
 *
 * <p>A {@link StubWorldRepo} is used instead of a real database so that
 * tests stay fast and deterministic.</p>
 */
public class CountryServiceTest {

    // ---------------------------------------------------------------------
    // R01 – All countries in the world
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R01 – getCountriesInWorldByPopulationDesc delegates to repo")
    void worldCountriesDelegatesToRepo() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getCountriesInWorldByPopulationDesc();

        assertSame(stub.result, rows, "Service should return list from repo");
        assertEquals("worldAll", stub.lastMethod);
        assertNull(stub.lastContinent);
        assertNull(stub.lastRegion);
        assertNull(stub.lastLimit);
    }

    // ---------------------------------------------------------------------
    // R02 – All countries in a continent
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R02 – null continent returns empty list and does not call repo")
    void continentNullReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getCountriesInContinentByPopulationDesc(null);

        assertNotNull(rows);
        assertTrue(rows.isEmpty(), "Expected empty list for null continent");
        assertNull(stub.lastMethod, "Repo should not be called for null continent");
    }

    @Test
    @DisplayName("R02 – blank continent returns empty list and does not call repo")
    void continentBlankReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getCountriesInContinentByPopulationDesc("   ");

        assertNotNull(rows);
        assertTrue(rows.isEmpty(), "Expected empty list for blank continent");
        assertNull(stub.lastMethod, "Repo should not be called for blank continent");
    }

    @Test
    @DisplayName("R02 – valid continent is trimmed and passed to repo")
    void continentTrimmedAndDelegated() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getCountriesInContinentByPopulationDesc(" Europe ");

        assertSame(stub.result, rows);
        assertEquals("continentAll", stub.lastMethod);
        assertEquals("Europe", stub.lastContinent);
        assertNull(stub.lastRegion);
        assertNull(stub.lastLimit);
    }

    // ---------------------------------------------------------------------
    // R03 – All countries in a region
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R03 – null region returns empty list and does not call repo")
    void regionNullReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getCountriesInRegionByPopulationDesc(null);

        assertNotNull(rows);
        assertTrue(rows.isEmpty(), "Expected empty list for null region");
        assertNull(stub.lastMethod, "Repo should not be called for null region");
    }

    @Test
    @DisplayName("R03 – blank region returns empty list and does not call repo")
    void regionBlankReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getCountriesInRegionByPopulationDesc("   ");

        assertNotNull(rows);
        assertTrue(rows.isEmpty(), "Expected empty list for blank region");
        assertNull(stub.lastMethod, "Repo should not be called for blank region");
    }

    @Test
    @DisplayName("R03 – valid region is trimmed and passed to repo")
    void regionTrimmedAndDelegated() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getCountriesInRegionByPopulationDesc(" Western Europe ");

        assertSame(stub.result, rows);
        assertEquals("regionAll", stub.lastMethod);
        assertEquals("Western Europe", stub.lastRegion);
        assertNull(stub.lastContinent);
        assertNull(stub.lastLimit);
    }

    // ---------------------------------------------------------------------
    // R04 – Top-N countries in the world
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R04 – non-positive limit returns empty list and does not call repo")
    void topWorldNonPositiveLimitReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> zero = service.getTopCountriesInWorldByPopulationDesc(0);
        List<CountryRow> negative = service.getTopCountriesInWorldByPopulationDesc(-5);

        assertTrue(zero.isEmpty());
        assertTrue(negative.isEmpty());
        assertNull(stub.lastMethod, "Repo should not be called for non-positive limit");
    }

    @Test
    @DisplayName("R04 – valid limit is passed to repo unchanged")
    void topWorldValidLimitDelegated() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getTopCountriesInWorldByPopulationDesc(10);

        assertSame(stub.result, rows);
        assertEquals("topWorld", stub.lastMethod);
        assertEquals(10, stub.lastLimit);
        assertNull(stub.lastContinent);
        assertNull(stub.lastRegion);
    }

    @Test
    @DisplayName("R04 – very large limit is clamped to MAX_LIMIT (500)")
    void topWorldLargeLimitIsClamped() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getTopCountriesInWorldByPopulationDesc(9999);

        assertSame(stub.result, rows);
        assertEquals("topWorld", stub.lastMethod);
        assertEquals(500, stub.lastLimit, "Expected limit to be clamped to 500");
    }

    @Test
    @DisplayName("R04 – string limit parses and delegates to int-based method")
    void topWorldStringLimitParses() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.getTopCountriesInWorldByPopulationDesc("15");

        assertSame(stub.result, rows);
        assertEquals("topWorld", stub.lastMethod);
        assertEquals(15, stub.lastLimit);
    }

    @Test
    @DisplayName("R04 – invalid string limit yields empty list (no repo call)")
    void topWorldInvalidStringLimitReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> blank = service.getTopCountriesInWorldByPopulationDesc("   ");
        List<CountryRow> text = service.getTopCountriesInWorldByPopulationDesc("abc");
        List<CountryRow> negative = service.getTopCountriesInWorldByPopulationDesc("-3");

        assertTrue(blank.isEmpty());
        assertTrue(text.isEmpty());
        assertTrue(negative.isEmpty());
        assertNull(stub.lastMethod, "Repo should not be called for invalid string limits");
    }

    @Test
    @DisplayName("R04 – null string limit yields empty list (no repo call)")
    void topWorldNullStringLimitReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getTopCountriesInWorldByPopulationDesc((String) null);

        assertTrue(rows.isEmpty());
        assertNull(stub.lastMethod, "Repo should not be called for null string limit");
    }

    // ---------------------------------------------------------------------
    // R05 – Top-N countries in a continent
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R05 – null/blank continent or non-positive limit returns empty list")
    void topContinentGuardBehaviour() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> r1 = service.getTopCountriesInContinentByPopulationDesc(null, 10);
        List<CountryRow> r2 = service.getTopCountriesInContinentByPopulationDesc("   ", 10);
        List<CountryRow> r3 = service.getTopCountriesInContinentByPopulationDesc("Europe", 0);
        List<CountryRow> r4 = service.getTopCountriesInContinentByPopulationDesc("Europe", -1);

        assertTrue(r1.isEmpty());
        assertTrue(r2.isEmpty());
        assertTrue(r3.isEmpty());
        assertTrue(r4.isEmpty());
        assertNull(stub.lastMethod, "Repo should not be called when guards fail");
    }

    @Test
    @DisplayName("R05 – valid continent and limit are trimmed, clamped, and delegated")
    void topContinentTrimmedAndClamped() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getTopCountriesInContinentByPopulationDesc(" Europe ", 600);

        assertSame(stub.result, rows);
        assertEquals("topContinent", stub.lastMethod);
        assertEquals("Europe", stub.lastContinent);
        assertEquals(500, stub.lastLimit, "Expected limit to be clamped to 500");
        assertNull(stub.lastRegion);
    }

    @Test
    @DisplayName("R05 – string limit overload parses and delegates")
    void topContinentStringLimitParses() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getTopCountriesInContinentByPopulationDesc(" Asia ", "25");

        assertSame(stub.result, rows);
        assertEquals("topContinent", stub.lastMethod);
        assertEquals("Asia", stub.lastContinent);
        assertEquals(25, stub.lastLimit);
    }

    @Test
    @DisplayName("R05 – invalid string limit returns empty list (no repo call)")
    void topContinentInvalidStringLimitReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getTopCountriesInContinentByPopulationDesc("Europe", "abc");

        assertTrue(rows.isEmpty());
        assertNull(stub.lastMethod, "Repo should not be called for invalid string limit");
    }

    // ---------------------------------------------------------------------
    // R06 – Top-N countries in a region
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R06 – null/blank region or non-positive limit returns empty list")
    void topRegionGuardBehaviour() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> r1 = service.getTopCountriesInRegionByPopulationDesc(null, 10);
        List<CountryRow> r2 = service.getTopCountriesInRegionByPopulationDesc("   ", 10);
        List<CountryRow> r3 = service.getTopCountriesInRegionByPopulationDesc("Western Europe", 0);
        List<CountryRow> r4 = service.getTopCountriesInRegionByPopulationDesc("Western Europe", -2);

        assertTrue(r1.isEmpty());
        assertTrue(r2.isEmpty());
        assertTrue(r3.isEmpty());
        assertTrue(r4.isEmpty());
        assertNull(stub.lastMethod, "Repo should not be called when guards fail");
    }

    @Test
    @DisplayName("R06 – valid region and limit are trimmed, clamped, and delegated")
    void topRegionTrimmedAndClamped() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getTopCountriesInRegionByPopulationDesc(" Western Europe ", 900);

        assertSame(stub.result, rows);
        assertEquals("topRegion", stub.lastMethod);
        assertEquals("Western Europe", stub.lastRegion);
        assertEquals(500, stub.lastLimit, "Expected limit to be clamped to 500");
        assertNull(stub.lastContinent);
    }

    @Test
    @DisplayName("R06 – string limit overload parses and delegates")
    void topRegionStringLimitParses() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getTopCountriesInRegionByPopulationDesc(" Caribbean ", "7");

        assertSame(stub.result, rows);
        assertEquals("topRegion", stub.lastMethod);
        assertEquals("Caribbean", stub.lastRegion);
        assertEquals(7, stub.lastLimit);
    }

    @Test
    @DisplayName("R06 – invalid string limit returns empty list (no repo call)")
    void topRegionInvalidStringLimitReturnsEmpty() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows =
            service.getTopCountriesInRegionByPopulationDesc("Western Europe", "xyz");

        assertTrue(rows.isEmpty());
        assertNull(stub.lastMethod, "Repo should not be called for invalid string limit");
    }

    // ---------------------------------------------------------------------
    // HTTP alias methods – extra coverage for CountryService
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Alias worldByPopulation delegates to world report repo call")
    void aliasWorldByPopulationDelegates() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.worldByPopulation();

        assertSame(stub.result, rows);
        assertEquals("worldAll", stub.lastMethod);
    }

    @Test
    @DisplayName("Alias countriesByContinent delegates and normalises input")
    void aliasCountriesByContinentDelegates() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.countriesByContinent(" Europe ");

        assertSame(stub.result, rows);
        assertEquals("continentAll", stub.lastMethod);
        assertEquals("Europe", stub.lastContinent);
    }

    @Test
    @DisplayName("Alias countriesByRegion delegates and normalises input")
    void aliasCountriesByRegionDelegates() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.countriesByRegion(" Western Europe ");

        assertSame(stub.result, rows);
        assertEquals("regionAll", stub.lastMethod);
        assertEquals("Western Europe", stub.lastRegion);
    }

    @Test
    @DisplayName("Alias topCountriesWorld delegates to top-world repo call")
    void aliasTopCountriesWorldDelegates() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.topCountriesWorld(10);

        assertSame(stub.result, rows);
        assertEquals("topWorld", stub.lastMethod);
        assertEquals(10, stub.lastLimit);
    }

    @Test
    @DisplayName("Alias topCountriesByContinent delegates and clamps limit")
    void aliasTopCountriesByContinentDelegates() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.topCountriesByContinent(" Asia ", 600);

        assertSame(stub.result, rows);
        assertEquals("topContinent", stub.lastMethod);
        assertEquals("Asia", stub.lastContinent);
        assertEquals(500, stub.lastLimit);
    }

    @Test
    @DisplayName("Alias topCountriesByRegion delegates to repo")
    void aliasTopCountriesByRegionDelegates() {
        StubWorldRepo stub = new StubWorldRepo();
        CountryService service = new CountryService(stub);

        List<CountryRow> rows = service.topCountriesByRegion(" Caribbean ", 7);

        assertSame(stub.result, rows);
        assertEquals("topRegion", stub.lastMethod);
        assertEquals("Caribbean", stub.lastRegion);
        assertEquals(7, stub.lastLimit);
    }

    // ---------------------------------------------------------------------
    // Stub repository
    // ---------------------------------------------------------------------

    /**
     * Simple stub for {@link WorldRepo} capturing the last call details.
     */
    private static final class StubWorldRepo extends WorldRepo {

        String lastContinent;
        String lastRegion;
        Integer lastLimit;
        String lastMethod;

        final List<CountryRow> result;

        StubWorldRepo() {
            // A single sample row is enough for all tests.
            this.result = List.of(new CountryRow(
                "GBR",
                "United Kingdom",
                "Europe",
                "British Islands",
                59623400L,
                "London"
            ));
        }

        @Override
        public List<CountryRow> findCountriesInWorldByPopulationDesc() {
            lastMethod = "worldAll";
            lastContinent = null;
            lastRegion = null;
            lastLimit = null;
            return result;
        }

        @Override
        public List<CountryRow> findCountriesInContinentByPopulationDesc(String continent) {
            lastMethod = "continentAll";
            lastContinent = continent;
            lastRegion = null;
            lastLimit = null;
            return result;
        }

        @Override
        public List<CountryRow> findCountriesInRegionByPopulationDesc(String region) {
            lastMethod = "regionAll";
            lastRegion = region;
            lastContinent = null;
            lastLimit = null;
            return result;
        }

        @Override
        public List<CountryRow> findTopCountriesInWorldByPopulationDesc(int limit) {
            lastMethod = "topWorld";
            lastLimit = limit;
            lastContinent = null;
            lastRegion = null;
            return result;
        }

        @Override
        public List<CountryRow> findTopCountriesInContinentByPopulationDesc(String continent, int limit) {
            lastMethod = "topContinent";
            lastContinent = continent;
            lastLimit = limit;
            lastRegion = null;
            return result;
        }

        @Override
        public List<CountryRow> findTopCountriesInRegionByPopulationDesc(String region, int limit) {
            lastMethod = "topRegion";
            lastRegion = region;
            lastLimit = limit;
            lastContinent = null;
            return result;
        }
    }
}
