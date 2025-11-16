package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CapitalCity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CapitalRepoJdbc} using the real MySQL
 * {@code world} database.
 *
 * Covers reports R17–R22:
 *  - R17: all capital cities in the world (population DESC)
 *  - R18: all capital cities in a continent
 *  - R19: all capital cities in a region
 *  - R20: top-N capitals in the world
 *  - R21: top-N capitals in a continent
 *  - R22: top-N capitals in a region
 */
class CapitalRepoJdbcIT {

    private static Connection conn;
    private static CapitalRepoJdbc repo;

    /**
     * Helper that reads a system property, but falls back to {@code def}
     * when the property is missing or blank.
     */
    private static String prop(String key, String def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    @BeforeAll
    static void setup() throws SQLException {
        // These values are normally passed from Maven/CI as -DDB_*
        String host   = prop("DB_HOST", "localhost");
        int    port   = Integer.parseInt(prop("DB_PORT", "43306")); // host port → container 3306
        String dbName = prop("DB_NAME", "world");
        String user   = prop("DB_USER", "app");
        String pass   = prop("DB_PASS", "app");

        conn = Db.connect(host, port, dbName, user, pass);
        repo = new CapitalRepoJdbc(conn);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // ---------- helpers ----------

    /** Assert that a list is sorted by population DESC. */
    private static void assertSortedByPopulationDesc(List<CapitalCity> rows, String label) {
        for (int i = 1; i < rows.size(); i++) {
            long prev = rows.get(i - 1).getPopulation();
            long curr = rows.get(i).getPopulation();
            assertTrue(
                prev >= curr,
                label + " should be sorted by population DESC at index " + i
            );
        }
    }

    // ---------- R17–R22 tests ----------

    @Test
    void r17_allCapitalsWorldSortedByPopulation() {
        List<CapitalCity> rows = repo.allCapitalsWorld();

        assertFalse(rows.isEmpty(), "R17 should return at least one capital city");
        assertSortedByPopulationDesc(rows, "R17");
    }

    @Test
    void r18_allCapitalsInContinentAsia() {
        List<CapitalCity> rows = repo.allCapitalsContinent("Asia");

        assertFalse(rows.isEmpty(), "R18 should return capitals for Asia");
        assertSortedByPopulationDesc(rows, "R18");

        // Simple sanity check using only the model fields we have:
        // there should be no obviously non-Asian capitals such as London.
        assertTrue(
            rows.stream().noneMatch(c -> "London".equals(c.getName())),
            "R18 should not include London (Europe)"
        );
    }

    @Test
    void r19_allCapitalsInRegionEasternAsia() {
        List<CapitalCity> rows = repo.allCapitalsRegion("Eastern Asia");

        assertFalse(rows.isEmpty(), "R19 should return capitals for Eastern Asia");
        assertSortedByPopulationDesc(rows, "R19");

        // Again, sanity check that obviously wrong capitals are not present.
        assertTrue(
            rows.stream().noneMatch(c -> "London".equals(c.getName())),
            "R19 should not include London (not in Eastern Asia)"
        );
    }

    @Test
    void r20_top5CapitalsWorld() {
        int n = 5;
        List<CapitalCity> rows = repo.topCapitalsWorld(n);

        assertEquals(n, rows.size(), "R20 should return exactly " + n + " capitals");
        assertSortedByPopulationDesc(rows, "R20");
    }

    @Test
    void r21_top5CapitalsInContinentAsia() {
        int n = 5;
        List<CapitalCity> rows = repo.topCapitalsContinent("Asia", n);

        assertEquals(n, rows.size(), "R21 should return exactly " + n + " capitals for Asia");
        assertSortedByPopulationDesc(rows, "R21");
        assertTrue(
            rows.stream().noneMatch(c -> "London".equals(c.getName())),
            "R21 should not include London (Europe)"
        );
    }

    @Test
    void r22_top5CapitalsInRegionEasternAsia() {
        int n = 5;
        List<CapitalCity> rows = repo.topCapitalsRegion("Eastern Asia", n);

        assertEquals(n, rows.size(), "R22 should return exactly " + n + " capitals for Eastern Asia");
        assertSortedByPopulationDesc(rows, "R22");
        assertTrue(
            rows.stream().noneMatch(c -> "London".equals(c.getName())),
            "R22 should not include London (not in Eastern Asia)"
        );
    }
}
