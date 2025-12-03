package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.CountryRow;
import org.junit.jupiter.api.DisplayName;
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
 * Extra tests for WorldRepo private helpers so that
 * WorldRepo reaches very high JaCoCo / Codecov coverage.
 *
 * These tests:
 *  - call runCountryQuery(..) via reflection
 *  - drive the "connection == null" branch
 *  - drive the normal happy-path (which also covers mapCountryRow(..))
 *  - drive the SQLException/error path from Db.getConnection()
 *
 * No real database is used – small JDBC stubs are built with Java
 * dynamic proxies.
 */
class WorldRepoRunCountryQueryTest {

    // ---------------------------------------------------------------------
    // Helper: invoke the private runCountryQuery(..) method
    // ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<CountryRow> invokeRunCountryQuery(WorldRepo repo,
                                                   String sql,
                                                   Object... params) throws Exception {
        Method m = WorldRepo.class.getDeclaredMethod(
            "runCountryQuery",
            String.class,
            Object[].class
        );
        m.setAccessible(true);

        // For varargs you must wrap the params array in another Object[].
        Object result = m.invoke(repo, sql, new Object[]{params});
        return (List<CountryRow>) result;
    }

    // ---------------------------------------------------------------------
    // Test 1: connection == null -> should return empty list safely
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("runCountryQuery returns empty list when Db.getConnection() is null")
    void runCountryQueryReturnsEmptyWhenConnectionIsNull() throws Exception {
        Db nullDb = new Db() {
            @Override
            public Connection getConnection() throws SQLException {
                return null; // simulate not connected
            }
        };

        WorldRepo repo = new WorldRepo(nullDb);

        List<CountryRow> rows =
            invokeRunCountryQuery(repo, "SELECT * FROM country", "Europe", 10);

        assertNotNull(rows, "Result list should never be null");
        assertTrue(rows.isEmpty(), "Expected empty list when connection is null");
    }

    // ---------------------------------------------------------------------
    // Helpers to build a tiny in-memory JDBC stack with one fake row
    // ---------------------------------------------------------------------

    private Db buildStubDbWithSingleRow() {
        // Fake ResultSet: exactly one row with fixed values.
        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
            ResultSet.class.getClassLoader(),
            new Class[]{ResultSet.class},
            new InvocationHandler() {
                int index = -1; // before first row

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    String name = method.getName();
                    switch (name) {
                        case "next" -> {
                            index++;
                            return index == 0; // true once, then false
                        }
                        case "getString" -> {
                            String col = (String) args[0];
                            if ("Code".equalsIgnoreCase(col)) {
                                return "TST";
                            } else if ("Name".equalsIgnoreCase(col)) {
                                return "Testland";
                            } else if ("Continent".equalsIgnoreCase(col)) {
                                return "TestContinent";
                            } else if ("Region".equalsIgnoreCase(col)) {
                                return "TestRegion";
                            }
                            return "";
                        }
                        case "getLong" -> {
                            return 123_456L; // population
                        }
                        case "close" -> {
                            return null;
                        }
                        default -> {
                            // Safe default values for methods we don't care about
                            Class<?> rt = method.getReturnType();
                            if (rt.equals(boolean.class)) return false;
                            if (rt.equals(int.class)) return 0;
                            if (rt.equals(long.class)) return 0L;
                            return null;
                        }
                    }
                }
            });

        // Fake PreparedStatement: record parameters & return our ResultSet.
        PreparedStatement stmt = (PreparedStatement) Proxy.newProxyInstance(
            PreparedStatement.class.getClassLoader(),
            new Class[]{PreparedStatement.class},
            (proxy, method, args) -> {
                String name = method.getName();
                switch (name) {
                    case "executeQuery" -> {
                        return rs;
                    }
                    case "setString", "setInt", "setLong", "close" -> {
                        // Accept parameter binding/close without doing anything.
                        return null;
                    }
                    default -> {
                        Class<?> rt = method.getReturnType();
                        if (rt.equals(boolean.class)) return false;
                        if (rt.equals(int.class)) return 0;
                        if (rt.equals(long.class)) return 0L;
                        return null;
                    }
                }
            });

        // Fake Connection: only prepareStatement(..) is needed.
        Connection conn = (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class[]{Connection.class},
            (proxy, method, args) -> {
                String name = method.getName();
                switch (name) {
                    case "prepareStatement" -> {
                        return stmt;
                    }
                    case "close" -> {
                        return null;
                    }
                    default -> {
                        Class<?> rt = method.getReturnType();
                        if (rt.equals(boolean.class)) return false;
                        if (rt.equals(int.class)) return 0;
                        if (rt.equals(long.class)) return 0L;
                        return null;
                    }
                }
            });

        // Db stub that always returns our fake Connection.
        return new Db() {
            @Override
            public Connection getConnection() throws SQLException {
                return conn;
            }
        };
    }

    // ---------------------------------------------------------------------
    // Test 2: happy path – parameters bound, row mapped via mapCountryRow(..)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("runCountryQuery maps rows via mapCountryRow when Db returns a connection")
    void runCountryQueryMapsRowsWhenConnectionOk() throws Exception {
        Db stubDb = buildStubDbWithSingleRow();
        WorldRepo repo = new WorldRepo(stubDb);

        List<CountryRow> rows = invokeRunCountryQuery(
            repo,
            "SELECT Code, Name, Continent, Region, Population FROM country " +
                "WHERE Continent = ? LIMIT ?",
            "TestContinent",
            1
        );

        assertNotNull(rows);
        assertEquals(1, rows.size(), "Expected exactly one mapped row");

        CountryRow row = rows.get(0);
        assertEquals("Testland", row.getName());
        assertEquals("TestContinent", row.getContinent());
        assertEquals("TestRegion", row.getRegion());
        assertEquals(123_456L, row.getPopulation());
    }

    // ---------------------------------------------------------------------
    // Test 3: Db.getConnection() throws SQLException -> handled gracefully
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("runCountryQuery handles SQLException from Db.getConnection() gracefully")
    void runCountryQueryHandlesSqlException() throws Exception {
        Db throwingDb = new Db() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("boom");
            }
        };

        WorldRepo repo = new WorldRepo(throwingDb);

        List<CountryRow> rows =
            invokeRunCountryQuery(repo, "SELECT * FROM country", "Asia");

        assertNotNull(rows, "Result list should never be null even on failure");
        assertTrue(rows.isEmpty(), "Expected empty list when SQLException occurs");
    }
}
