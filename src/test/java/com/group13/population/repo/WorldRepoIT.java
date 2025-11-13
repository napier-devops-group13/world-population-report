package com.group13.population.repo;

import com.group13.population.db.Db;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WorldRepo using a real MySQL "world" database.
 * Covers reports R01–R06.
 */
class WorldRepoIT {

    private static Connection conn;
    private static WorldRepo repo;

    /**
     * Helper that reads a system property but falls back if it is
     * missing or blank. This makes Maven/CI robust if DB_* props
     * are defined but empty.
     */
    private static String prop(String key, String def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    @BeforeAll
    static void setup() throws SQLException {
        // Values are passed in from Maven/CI as system properties.
        // Defaults make it work locally as well.
        String host = prop("DB_HOST", "localhost");

        // Host port mapped to the container's 3306.
        // In CI you can override with -DDB_PORT=3306.
        String portStr = prop("DB_PORT", "43306");
        int port = Integer.parseInt(portStr);

        String dbName = prop("DB_NAME", "world");
        String user = prop("DB_USER", "app");
        String pass = prop("DB_PASS", "app");

        // Use the static helper – no "new Db()" here
        conn = Db.connect(host, port, dbName, user, pass);
        repo = new WorldRepo(conn);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Test
    void r01_allCountriesWorldSortedByPopulation() {
        List<WorldRepo.CountryRow> rows = repo.allCountriesWorld();
        assertFalse(rows.isEmpty());
        assertTrue(
            rows.get(0).population() >= rows.get(1).population(),
            "R01 should be sorted by population DESC"
        );
    }

    @Test
    void r02_allCountriesInContinentAsia() {
        var rows = repo.allCountriesContinent("Asia");
        assertFalse(rows.isEmpty());
        assertEquals("Asia", rows.get(0).continent());
    }

    @Test
    void r03_allCountriesInRegionEasternAsia() {
        var rows = repo.allCountriesRegion("Eastern Asia");
        assertFalse(rows.isEmpty());
        assertEquals("Eastern Asia", rows.get(0).region());
    }

    @Test
    void r04_top5CountriesWorld() {
        var rows = repo.topNCountriesWorld(5);
        assertEquals(5, rows.size());
    }

    @Test
    void r05_top5CountriesInContinentAsia() {
        var rows = repo.topNCountriesContinent("Asia", 5);
        assertEquals(5, rows.size());
    }

    @Test
    void r06_top5CountriesInRegionEasternAsia() {
        var rows = repo.topNCountriesRegion("Eastern Asia", 5);
        assertEquals(5, rows.size());
    }
}
