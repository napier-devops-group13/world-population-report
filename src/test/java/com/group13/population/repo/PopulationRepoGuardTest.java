package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.LanguagePopulationRow;
import com.group13.population.model.PopulationLookupRow;
import com.group13.population.model.PopulationRow;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guard / coverage tests for PopulationRepo:
 *  - when Db.getConnection() throws
 *  - when Db.getConnection() returns null
 *  - when lookup methods are called with missing names
 *  - when lookup and language methods are called and DB is unavailable
 *  - edge-case result sets with odd/blank data
 *
 * The repo should not throw, and should return
 * empty lists / 0 for world population / sensible "unknown" rows.
 */
class PopulationRepoGuardTest {

    // ---------------------------------------------------------------------
    // Connection error / null connection guards
    // ---------------------------------------------------------------------

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

    // ---------------------------------------------------------------------
    // Input guards (unknown continent / region / etc.)
    // ---------------------------------------------------------------------

    @Test
    void lookupMethodsReturnUnknownRowWhenNameMissing() {
        PopulationRepo repo = new PopulationRepo(new NullDb());

        PopulationLookupRow continent = repo.findContinentPopulation(null);
        assertEquals("unknown continent", continent.getName());
        assertEquals(0L, continent.getPopulation());

        PopulationLookupRow region = repo.findRegionPopulation("   ");
        assertEquals("unknown region", region.getName());
        assertEquals(0L, region.getPopulation());

        PopulationLookupRow country = repo.findCountryPopulation("");
        assertEquals("unknown country", country.getName());
        assertEquals(0L, country.getPopulation());

        PopulationLookupRow district = repo.findDistrictPopulation(null);
        assertEquals("unknown district", district.getName());
        assertEquals(0L, district.getPopulation());

        PopulationLookupRow city = repo.findCityPopulation("  ");
        assertEquals("unknown city", city.getName());
        assertEquals(0L, city.getPopulation());
    }

    // ---------------------------------------------------------------------
    // Lookup + language when DB is unavailable
    // ---------------------------------------------------------------------

    @Test
    void lookupFallsBackToZeroWhenConnectionIsNull() {
        PopulationRepo repo = new PopulationRepo(new NullDb());

        PopulationLookupRow row = repo.findCountryPopulation("Myanmar");
        assertEquals("Myanmar", row.getName());
        assertEquals(0L, row.getPopulation());
    }

    @Test
    void languagePopulationsHandleSQLException() {
        PopulationRepo repo = new PopulationRepo(new ThrowingDb());

        List<LanguagePopulationRow> languages = repo.findLanguagePopulations();
        assertNotNull(languages);
        assertTrue(languages.isEmpty(), "Languages list should be empty when getConnection throws");
    }

    @Test
    void languagePopulationsReturnEmptyListWhenConnectionIsNull() {
        PopulationRepo repo = new PopulationRepo(new NullDb());

        List<LanguagePopulationRow> languages = repo.findLanguagePopulations();
        assertNotNull(languages);
        assertTrue(languages.isEmpty(), "Languages list should be empty when connection is null");
    }

    // ---------------------------------------------------------------------
    // Edge-case result set coverage
    // ---------------------------------------------------------------------

    @Test
    void edgeCaseRowDoesNotCrashAndReturnsSafeValues() {
        ResultSet rs = oneOddRowResultSet();
        PreparedStatement stmt = stmtReturningOneOddRow(rs);
        Connection conn = connectionReturningThatStatement(stmt);
        Db db = new EdgeCaseDb(conn);

        PopulationRepo repo = new PopulationRepo(db);

        List<PopulationRow> regions = repo.findPopulationByRegionInOutCities();
        List<PopulationRow> countries = repo.findPopulationByCountryInOutCities();
        long world = repo.findWorldPopulation();

        assertNotNull(regions);
        assertNotNull(countries);
        // We don’t assert specific values here; we just ensure nothing crazy.
        assertFalse(world < 0, "World population should never be negative");
    }

    // ---------- Dynamic proxy stubs for edge-case row ---------------------

    private static ResultSet oneOddRowResultSet() {
        InvocationHandler handler = new InvocationHandler() {
            boolean first = true;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "next":
                        if (first) {
                            first = false;
                            return true;   // exactly one row
                        }
                        return false;

                    case "getString":
                        // blank label to hit any name/blank guards
                        return "";

                    case "getLong":
                        // zeros for all numeric columns
                        return 0L;

                    case "close":
                        return null;

                    case "wasNull":
                        return false;

                    case "unwrap":
                        return null;

                    case "isWrapperFor":
                        return false;

                    default:
                        throw new UnsupportedOperationException("ResultSet." + name);
                }
            }
        };

        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                handler
        );
    }

    private static PreparedStatement stmtReturningOneOddRow(ResultSet rs) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "executeQuery":
                    return rs;
                case "close":
                    return null;
                case "setString":
                case "setInt":
                case "setLong":
                    return null; // ignore parameter binding
                case "unwrap":
                    return null;
                case "isWrapperFor":
                    return false;
                default:
                    throw new UnsupportedOperationException("PreparedStatement." + name);
            }
        };

        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                handler
        );
    }

    private static Connection connectionReturningThatStatement(PreparedStatement stmt) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "prepareStatement":
                case "createStatement":
                    return stmt;
                case "isClosed":
                    return false;
                case "close":
                    return null;
                case "unwrap":
                    return null;
                case "isWrapperFor":
                    return false;
                default:
                    throw new UnsupportedOperationException("Connection." + name);
            }
        };

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
    }

    private static class EdgeCaseDb extends Db {
        private final Connection conn;

        EdgeCaseDb(Connection conn) {
            this.conn = conn;
        }

        @Override
        public Connection getConnection() {
            return conn;
        }
    }

    // --- Common Db stubs --------------------------------------------------

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
