package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CityRow;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CityRepo} against the real MySQL
 * {@code world} database.
 *
 * <p>These tests exercise all city reporting requirements R07–R16:</p>
 * <ul>
 *   <li>R07 – All cities in the world (population DESC).</li>
 *   <li>R08 – All cities in a continent (population DESC).</li>
 *   <li>R09 – All cities in a region (population DESC).</li>
 *   <li>R10 – All cities in a country (population DESC).</li>
 *   <li>R11 – All cities in a district (population DESC).</li>
 *   <li>R12 – Top-N cities in the world (population DESC).</li>
 *   <li>R13 – Top-N cities in a continent (population DESC).</li>
 *   <li>R14 – Top-N cities in a region (population DESC).</li>
 *   <li>R15 – Top-N cities in a country (population DESC).</li>
 *   <li>R16 – Top-N cities in a district (population DESC).</li>
 * </ul>
 */
public class CityRepoIT {

    private static Db db;
    private static CityRepo repo;

    @BeforeAll
    static void connectToDatabase() {
        db = new Db();

        String host = getenvOrDefault("DB_HOST", "localhost");
        String port = getenvOrDefault("DB_PORT", "43306");
        String location = host + ":" + port;

        boolean connected = db.connect(location, 30_000);
        assertTrue(connected, "Failed to connect to database at " + location);

        repo = new CityRepo(db);
    }

    @AfterAll
    static void disconnectDatabase() {
        if (db != null) {
            db.disconnect();
        }
    }

    // ---------------------------------------------------------------------
    // R07 – All cities in the world (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R07 – world cities are returned and sorted by population DESC")
    void worldCitiesSortedByPopulation() {
        List<CityRow> rows = repo.findCitiesInWorldByPopulationDesc();

        assertNotNull(rows, "Result list should not be null");
        assertFalse(rows.isEmpty(), "World cities list should not be empty");

        assertSortedByPopulationDesc(rows, "world cities");
    }

    // ---------------------------------------------------------------------
    // R08 – All cities in a continent (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R08 – continent filter returns cities sorted DESC")
    void continentFilterWorks() {
        List<CityRow> europe =
            repo.findCitiesInContinentByPopulationDesc("Europe");

        assertNotNull(europe);
        assertFalse(europe.isEmpty(), "Europe cities list should not be empty");

        assertSortedByPopulationDesc(europe, "Europe cities");
    }

    // ---------------------------------------------------------------------
    // R09 – All cities in a region (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R09 – region filter returns cities sorted DESC")
    void regionFilterWorks() {
        List<CityRow> caribbean =
            repo.findCitiesInRegionByPopulationDesc("Caribbean");

        assertNotNull(caribbean);
        assertFalse(caribbean.isEmpty(), "Caribbean cities list should not be empty");

        assertSortedByPopulationDesc(caribbean, "Caribbean cities");
    }

    // ---------------------------------------------------------------------
    // R10 – All cities in a country (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R10 – country filter returns cities sorted DESC")
    void countryFilterWorks() {
        List<CityRow> japan =
            repo.findCitiesInCountryByPopulationDesc("Japan");

        assertNotNull(japan);
        assertFalse(japan.isEmpty(), "Japan cities list should not be empty");

        assertSortedByPopulationDesc(japan, "Japan cities");
    }

    // ---------------------------------------------------------------------
    // R11 – All cities in a district (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R11 – district filter returns cities sorted DESC")
    void districtFilterWorks() {
        // IMPORTANT: real district name in the world DB is "Tokyo-to", not "Tokyo"
        List<CityRow> tokyoDistrict =
            repo.findCitiesInDistrictByPopulationDesc("Tokyo-to");

        assertNotNull(tokyoDistrict);
        assertFalse(tokyoDistrict.isEmpty(),
            "Tokyo-to district cities list should not be empty");

        assertSortedByPopulationDesc(tokyoDistrict, "Tokyo-to district cities");
    }

    // ---------------------------------------------------------------------
    // R12 – Top-N cities in the world (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R12 – top-N world cities returns <= N rows sorted DESC")
    void topWorldCitiesRespectLimitAndOrdering() {
        int limit = 10;
        List<CityRow> rows =
            repo.findTopCitiesInWorldByPopulationDesc(limit);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Top-N world cities list should not be empty");
        assertTrue(rows.size() <= limit,
            "Expected at most " + limit + " rows but got " + rows.size());

        assertSortedByPopulationDesc(rows, "top-N world cities");
    }

    // ---------------------------------------------------------------------
    // R13 – Top-N cities in a continent (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R13 – top-N continent cities returns <= N rows sorted DESC")
    void topContinentCitiesRespectLimitAndOrdering() {
        int limit = 5;
        List<CityRow> rows =
            repo.findTopCitiesInContinentByPopulationDesc("Europe", limit);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Top-N Europe cities list should not be empty");
        assertTrue(rows.size() <= limit,
            "Expected at most " + limit + " rows but got " + rows.size());

        assertSortedByPopulationDesc(rows, "top-N Europe cities");
    }

    // ---------------------------------------------------------------------
    // R14 – Top-N cities in a region (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R14 – top-N region cities returns <= N rows sorted DESC")
    void topRegionCitiesRespectLimitAndOrdering() {
        int limit = 5;
        List<CityRow> rows =
            repo.findTopCitiesInRegionByPopulationDesc("Caribbean", limit);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Top-N Caribbean cities list should not be empty");
        assertTrue(rows.size() <= limit,
            "Expected at most " + limit + " rows but got " + rows.size());

        assertSortedByPopulationDesc(rows, "top-N Caribbean cities");
    }

    // ---------------------------------------------------------------------
    // R15 – Top-N cities in a country (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R15 – top-N country cities returns <= N rows sorted DESC")
    void topCountryCitiesRespectLimitAndOrdering() {
        int limit = 5;
        List<CityRow> rows =
            repo.findTopCitiesInCountryByPopulationDesc("Japan", limit);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Top-N Japan cities list should not be empty");
        assertTrue(rows.size() <= limit,
            "Expected at most " + limit + " rows but got " + rows.size());

        assertSortedByPopulationDesc(rows, "top-N Japan cities");
    }

    // ---------------------------------------------------------------------
    // R16 – Top-N cities in a district (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R16 – top-N district cities returns <= N rows sorted DESC")
    void topDistrictCitiesRespectLimitAndOrdering() {
        int limit = 5;
        List<CityRow> rows =
            repo.findTopCitiesInDistrictByPopulationDesc("Tokyo-to", limit);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(),
            "Top-N Tokyo-to cities list should not be empty");
        assertTrue(rows.size() <= limit,
            "Expected at most " + limit + " rows but got " + rows.size());

        assertSortedByPopulationDesc(rows, "top-N Tokyo-to cities");
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static void assertSortedByPopulationDesc(List<CityRow> rows, String label) {
        for (int i = 1; i < rows.size(); i++) {
            long prevPop = rows.get(i - 1).getPopulation();
            long currPop = rows.get(i).getPopulation();
            assertTrue(prevPop >= currPop,
                "Population should be non-increasing for " + label +
                    ", but at index " + i + " found " + prevPop + " then " + currPop);
        }
    }

    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
