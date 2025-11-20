package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CountryRow;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link WorldRepo} against the real MySQL
 * <em>world</em> database.
 *
 * <p>These tests prove that:</p>
 * <ul>
 *   <li>The repository can connect to the database via {@link Db}.</li>
 *   <li>Queries for R01–R04 return sensible data from the schema.</li>
 *   <li>Results are ordered by population DESC as required.</li>
 *   <li>Filters for continent/region work correctly.</li>
 * </ul>
 *
 * <p>Connection strategy:</p>
 * <ul>
 *   <li>If {@code DB_HOST} and {@code DB_PORT} are set (e.g. inside Docker),
 *       they are used (for your compose this will be {@code db:3306}).</li>
 *   <li>Otherwise the test falls back to {@code localhost:43306}, which matches
 *       the port mapping {@code 43306:3306} in docker-compose.</li>
 * </ul>
 */
public class WorldRepoIT {

    private static Db db;
    private static WorldRepo repo;

    @BeforeAll
    static void connectToDatabase() {
        db = new Db();

        String host = getenvOrDefault("DB_HOST", "localhost");
        String port = getenvOrDefault("DB_PORT", "43306");
        String location = host + ":" + port;

        boolean connected = db.connect(location, 30_000);
        assertTrue(connected, "Failed to connect to database at " + location);

        repo = new WorldRepo(db);
    }

    @AfterAll
    static void disconnectDatabase() {
        if (db != null) {
            db.disconnect();
        }
    }

    // ---------------------------------------------------------------------
    // R01 – All countries in the world (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R01 – world countries are returned and sorted by population DESC")
    void worldCountriesSortedByPopulation() {
        List<CountryRow> rows = repo.findCountriesInWorldByPopulationDesc();

        assertNotNull(rows, "Result list should not be null");
        assertFalse(rows.isEmpty(), "World countries list should not be empty");

        // Standard MySQL world sample: China is the most populated country.
        CountryRow first = rows.get(0);
        assertEquals("CHN", first.getCode());
        assertEquals("China", first.getName());

        // Verify descending order by population.
        for (int i = 1; i < rows.size(); i++) {
            long prevPop = rows.get(i - 1).getPopulation();
            long currPop = rows.get(i).getPopulation();
            assertTrue(prevPop >= currPop,
                "Population should be non-increasing, but at index " + i
                    + " found " + prevPop + " then " + currPop);
        }
    }

    // ---------------------------------------------------------------------
    // R02 – All countries in a continent (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R02 – continent filter returns only that continent, sorted DESC")
    void continentFilterWorks() {
        List<CountryRow> asia = repo.findCountriesInContinentByPopulationDesc("Asia");

        assertNotNull(asia);
        assertFalse(asia.isEmpty(), "Asia list should not be empty");

        // All rows should have Continent == "Asia"
        for (CountryRow row : asia) {
            assertEquals("Asia", row.getContinent());
        }

        // Populations in descending order
        for (int i = 1; i < asia.size(); i++) {
            long prev = asia.get(i - 1).getPopulation();
            long curr = asia.get(i).getPopulation();
            assertTrue(prev >= curr, "Population not sorted DESC within continent");
        }
    }

    // ---------------------------------------------------------------------
    // R03 – All countries in a region (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R03 – region filter returns only that region, sorted DESC")
    void regionFilterWorks() {
        List<CountryRow> westernEurope =
            repo.findCountriesInRegionByPopulationDesc("Western Europe");

        assertNotNull(westernEurope);
        assertFalse(westernEurope.isEmpty(), "Western Europe list should not be empty");

        for (CountryRow row : westernEurope) {
            assertEquals("Western Europe", row.getRegion());
        }

        for (int i = 1; i < westernEurope.size(); i++) {
            long prev = westernEurope.get(i - 1).getPopulation();
            long curr = westernEurope.get(i).getPopulation();
            assertTrue(prev >= curr, "Population not sorted DESC within region");
        }
    }

    // ---------------------------------------------------------------------
    // R04 – Top-N countries in the world (population DESC)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R04 – top-N world countries returns N rows sorted DESC")
    void topWorldCountriesRespectsLimitAndOrdering() {
        int limit = 5;
        List<CountryRow> rows = repo.findTopCountriesInWorldByPopulationDesc(limit);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "Top-N list should not be empty");
        assertTrue(rows.size() <= limit,
            "Expected at most " + limit + " rows but got " + rows.size());

        // Top list should start with China as well.
        CountryRow first = rows.get(0);
        assertEquals("China", first.getName());

        for (int i = 1; i < rows.size(); i++) {
            long prev = rows.get(i - 1).getPopulation();
            long curr = rows.get(i).getPopulation();
            assertTrue(prev >= curr, "Population not sorted DESC in top-N world list");
        }
    }

    // Small helper to avoid repeating env default logic here.
    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
