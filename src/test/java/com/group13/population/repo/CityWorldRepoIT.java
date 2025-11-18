package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.City;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CityWorldRepo} against the real classic
 * {@code world} database.
 *
 * Verifies that the JDBC queries for the city reports (R07–R16) are wired
 * correctly and that ordering/filters match the coursework specification:
 *
 *  - R07: all cities in the world (population DESC)
 *  - R08: all cities in a continent (population DESC)
 *  - R09: all cities in a region (population DESC)
 *  - R10: all cities in a country (population DESC)
 *  - R11: all cities in a district (population DESC)
 *  - R12: top-N cities in the world (population DESC)
 *  - R13: top-N cities in a continent (population DESC)
 *  - R14: top-N cities in a region (population DESC)
 *  - R15: top-N cities in a country (population DESC)
 *  - R16: top-N cities in a district (population DESC)
 *
 * These tests assume the standard MySQL {@code world.sql} dataset.
 */
class CityWorldRepoIT {

    private static Connection connection;
    private static CityWorldRepo repo;

    // ---------------------------------------------------------------------
    // Test lifecycle
    // ---------------------------------------------------------------------

    @BeforeAll
    static void setUpDatabaseConnection() throws SQLException {
        // Match the same env-based defaults used in the main code:
        //  - DB_HOST  (default localhost)
        //  - DB_PORT  (default 43306 for IDE; 3306 in CI docker-compose)
        //  - DB_NAME  (default world)
        //  - DB_USER  (default app)
        //  - DB_PASS  (default app)
        String host = envOr("DB_HOST", "localhost");
        int port = Integer.parseInt(envOr("DB_PORT", "43306"));
        String dbName = envOr("DB_NAME", "world");
        String user = envOr("DB_USER", "app");
        String pass = envOr("DB_PASS", "app");

        connection = Db.connect(host, port, dbName, user, pass);
        repo = new CityWorldRepo(connection);
    }

    @AfterAll
    static void tearDownDatabaseConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private static String envOr(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    // ---------------------------------------------------------------------
    // Tests – world level (R07, R12)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R07 – all cities in the world are ordered by population DESC")
    void worldAll_isOrderedByPopulationDesc() {
        List<City> cities = repo.worldAll();

        // basic sanity
        assertNotNull(cities, "worldAll() should never return null");
        assertFalse(cities.isEmpty(), "worldAll() should return at least one row");

        // known top city in classic world DB
        City first = cities.get(0);
        assertEquals("Mumbai (Bombay)", first.getName());
        assertEquals(10_500_000L, first.getPopulation());

        // monotonic non-increasing population
        for (int i = 1; i < cities.size(); i++) {
            long prev = cities.get(i - 1).getPopulation();
            long curr = cities.get(i).getPopulation();
            assertTrue(prev >= curr,
                "Cities must be ordered by Population DESC (index " + i + ")");
        }
    }

    @Test
    @DisplayName("R12 – top-N cities in the world respects N and ordering")
    void worldTopN_respectsLimitAndOrdering() {
        int n = 3;

        List<City> top = repo.worldTopN(n);

        assertNotNull(top);
        assertEquals(n, top.size(), "top-N should return exactly N rows");

        // From the classic world DB:
        // 1. Mumbai (Bombay) – 10500000
        // 2. Seoul           –  9981619
        // 3. São Paulo       –  9968485
        assertEquals("Mumbai (Bombay)", top.get(0).getName());
        assertEquals(10_500_000L, top.get(0).getPopulation());

        assertEquals("Seoul", top.get(1).getName());
        assertEquals(9_981_619L, top.get(1).getPopulation());

        assertEquals("São Paulo", top.get(2).getName());
        assertEquals(9_968_485L, top.get(2).getPopulation());
    }

    // ---------------------------------------------------------------------
    // Tests – continent / region filters (R08, R09, R13, R14)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R08 – all cities in a continent only include that continent (spot-check Asia)")
    void continentAll_filtersByContinent() {
        List<City> asiaCities = repo.continentAll("Asia");

        assertNotNull(asiaCities);
        assertFalse(asiaCities.isEmpty());

        // top Asian city should still be Mumbai (Bombay)
        City first = asiaCities.get(0);
        assertEquals("Mumbai (Bombay)", first.getName());
        assertEquals(10_500_000L, first.getPopulation());

        // And size should be smaller than the full world list
        List<City> world = repo.worldAll();
        assertTrue(asiaCities.size() < world.size(),
            "continentAll(\"Asia\") must return a strict subset of worldAll()");
    }

    @Test
    @DisplayName("R13 – top-N cities in a continent respects filter and N (Asia)")
    void continentTopN_filtersAndLimits() {
        int n = 5;
        List<City> asiaTop = repo.continentTopN("Asia", n);

        assertNotNull(asiaTop);
        assertEquals(n, asiaTop.size());

        // Still expect Mumbai at the top for Asia
        City first = asiaTop.get(0);
        assertEquals("Mumbai (Bombay)", first.getName());
        assertEquals(10_500_000L, first.getPopulation());
    }

    // ---------------------------------------------------------------------
    // Tests – district filters (R11, R16) using 'New York' district
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R11 – all cities in a district (New York) use population DESC ordering")
    void districtAll_newYork_hasExpectedCitiesAndOrder() {
        List<City> ny = repo.districtAll("New York");

        // The classic world DB has six cities in district 'New York'
        assertNotNull(ny);
        assertEquals(6, ny.size(), "District 'New York' should contain six cities");

        // Expected descending order by population:
        // New York, Buffalo, Rochester, Yonkers, Syracuse, Albany
        assertEquals("New York", ny.get(0).getName());
        assertEquals(8_008_278L, ny.get(0).getPopulation());

        assertEquals("Buffalo", ny.get(1).getName());
        assertEquals("Rochester", ny.get(2).getName());
        assertEquals("Yonkers", ny.get(3).getName());
        assertEquals("Syracuse", ny.get(4).getName());
        assertEquals("Albany", ny.get(5).getName());

        // Also check monotone population DESC
        for (int i = 1; i < ny.size(); i++) {
            long prev = ny.get(i - 1).getPopulation();
            long curr = ny.get(i).getPopulation();
            assertTrue(prev >= curr,
                "District rows must be ordered by Population DESC (index " + i + ")");
        }
    }

    @Test
    @DisplayName("R16 – top-N cities in a district respects N and ordering (New York)")
    void districtTopN_newYork_respectsLimit() {
        int n = 3;
        List<City> nyTop = repo.districtTopN("New York", n);

        assertNotNull(nyTop);
        assertEquals(n, nyTop.size());

        // Should just be the first three from the full district result
        assertEquals("New York", nyTop.get(0).getName());
        assertEquals("Buffalo", nyTop.get(1).getName());
        assertEquals("Rochester", nyTop.get(2).getName());
    }
}
