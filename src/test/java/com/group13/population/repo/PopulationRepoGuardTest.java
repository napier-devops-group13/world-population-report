package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guard tests for PopulationRepo:
 *  - when Db.getConnection() throws
 *  - when Db.getConnection() returns null
 *
 * In both cases the repo should not throw, and should return
 * empty lists / 0 for world population.
 */
class PopulationRepoGuardTest {

    @Test
    void returnsEmptyResultsWhenGetConnectionThrows() {
        PopulationRepo repo = new PopulationRepo(new ThrowingDb());

        List<PopulationRow> regions = repo.findPopulationByRegionInOutCities();
        List<PopulationRow> countries = repo.findPopulationByCountryInOutCities();
        long world = repo.findWorldPopulation();

        assertNotNull(regions);
        assertNotNull(countries);
        assertTrue(regions.isEmpty());
        assertTrue(countries.isEmpty());
        assertEquals(0L, world);
    }

    @Test
    void returnsEmptyResultsWhenConnectionIsNull() {
        PopulationRepo repo = new PopulationRepo(new NullDb());

        List<PopulationRow> regions = repo.findPopulationByRegionInOutCities();
        List<PopulationRow> countries = repo.findPopulationByCountryInOutCities();
        long world = repo.findWorldPopulation();

        assertNotNull(regions);
        assertNotNull(countries);
        assertTrue(regions.isEmpty());
        assertTrue(countries.isEmpty());
        assertEquals(0L, world);
    }



    // --- Test stubs -------------------------------------------------------

    /** Db stub whose getConnection() always throws SQLException. */
    private static class ThrowingDb extends Db {
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("boom");
        }
    }

    /** Db stub whose getConnection() always returns null. */
    private static class NullDb extends Db {
        @Override
        public Connection getConnection() {
            return null;
        }
    }
}
