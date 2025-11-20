package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CountryRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guard-rail tests for {@link WorldRepo}.
 *
 * <p>These tests verify that invalid inputs (null/blank filters, non-positive
 * limits) are handled safely by returning an empty list and, importantly,
 * <strong>do not touch the database</strong>.</p>
 *
 * <p>They use a {@link FailingDb} stub which throws if {@link Db#getConnection()}
 * is ever called. If a guard fails to short-circuit, the test will fail with
 * an {@link AssertionError}.</p>
 */
public class WorldRepoGuardTest {

    // ---------------------------------------------------------------------
    // Continent guard tests (R02 + R05)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R02 – null continent returns empty list and does not hit DB")
    void nullContinentReturnsEmptyForAllInContinent() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        List<CountryRow> result = repo.findCountriesInContinentByPopulationDesc(null);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list for null continent");
    }

    @Test
    @DisplayName("R02 – blank continent returns empty list and does not hit DB")
    void blankContinentReturnsEmptyForAllInContinent() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        List<CountryRow> result = repo.findCountriesInContinentByPopulationDesc("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list for blank continent");
    }

    @Test
    @DisplayName("R05 – null continent or non-positive N returns empty list and does not hit DB")
    void nullContinentOrBadLimitReturnsEmptyForTopInContinent() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        // null continent
        List<CountryRow> r1 = repo.findTopCountriesInContinentByPopulationDesc(null, 10);
        assertNotNull(r1);
        assertTrue(r1.isEmpty());

        // blank continent
        List<CountryRow> r2 = repo.findTopCountriesInContinentByPopulationDesc("   ", 10);
        assertNotNull(r2);
        assertTrue(r2.isEmpty());

        // non-positive N
        List<CountryRow> r3 = repo.findTopCountriesInContinentByPopulationDesc("Europe", 0);
        List<CountryRow> r4 = repo.findTopCountriesInContinentByPopulationDesc("Europe", -5);

        assertTrue(r3.isEmpty());
        assertTrue(r4.isEmpty());
    }

    // ---------------------------------------------------------------------
    // Region guard tests (R03 + R06)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R03 – null region returns empty list and does not hit DB")
    void nullRegionReturnsEmptyForAllInRegion() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        List<CountryRow> result = repo.findCountriesInRegionByPopulationDesc(null);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list for null region");
    }

    @Test
    @DisplayName("R03 – blank region returns empty list and does not hit DB")
    void blankRegionReturnsEmptyForAllInRegion() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        List<CountryRow> result = repo.findCountriesInRegionByPopulationDesc("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list for blank region");
    }

    @Test
    @DisplayName("R06 – null region or non-positive N returns empty list and does not hit DB")
    void nullRegionOrBadLimitReturnsEmptyForTopInRegion() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        // null region
        List<CountryRow> r1 = repo.findTopCountriesInRegionByPopulationDesc(null, 10);
        assertNotNull(r1);
        assertTrue(r1.isEmpty());

        // blank region
        List<CountryRow> r2 = repo.findTopCountriesInRegionByPopulationDesc("   ", 10);
        assertNotNull(r2);
        assertTrue(r2.isEmpty());

        // non-positive N
        List<CountryRow> r3 = repo.findTopCountriesInRegionByPopulationDesc("Western Europe", 0);
        List<CountryRow> r4 = repo.findTopCountriesInRegionByPopulationDesc("Western Europe", -3);

        assertTrue(r3.isEmpty());
        assertTrue(r4.isEmpty());
    }

    // ---------------------------------------------------------------------
    // World Top-N guard tests (R04)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R04 – non-positive N for world top countries returns empty list and does not hit DB")
    void nonPositiveLimitReturnsEmptyForTopWorld() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        List<CountryRow> r1 = repo.findTopCountriesInWorldByPopulationDesc(0);
        List<CountryRow> r2 = repo.findTopCountriesInWorldByPopulationDesc(-1);

        assertTrue(r1.isEmpty());
        assertTrue(r2.isEmpty());
    }

    // ---------------------------------------------------------------------
    // Positive path sanity check
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("R01 – world query does delegate to Db for normal inputs")
    void worldQueryDelegatesToDb() {
        WorldRepo repo = new WorldRepo(new FailingDb());

        // For a non-guarded call, we expect the repository to ask the Db
        // for a connection, which in this test throws AssertionError.
        assertThrows(AssertionError.class,
                repo::findCountriesInWorldByPopulationDesc);
    }

    // ---------------------------------------------------------------------
    // Test double
    // ---------------------------------------------------------------------

    /**
     * A Db stub that fails the test if any method tries to obtain a real
     * JDBC connection. This allows us to prove that guard clauses return
     * early without touching the database.
     */
    private static final class FailingDb extends Db {

        @Override
        public Connection getConnection() throws SQLException {
            throw new AssertionError("Db.getConnection() should not be called in guard tests");
        }
    }
}
