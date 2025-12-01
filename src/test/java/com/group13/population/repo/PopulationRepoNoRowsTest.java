package com.group13.population.repo;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extra coverage for PopulationRepo:
 *
 * Scenario: DB connection works and query executes, but the ResultSet has NO rows.
 * This exercises the "no data" branches (rs.next() == false) that are not
 * hit by the normal MySQL integration tests.
 */
class PopulationRepoNoRowsTest {

    /**
     * Creates a Connection whose prepareStatement/executeQuery chain
     * returns an empty ResultSet (next() is always false).
     */
    private static Connection emptyResultConnection() {
        // --- ResultSet stub: next() -> false, everything else unsupported ---
        InvocationHandler rsHandler = (Object proxy, Method method, Object[] args) -> {
            String name = method.getName();
            switch (name) {
                case "next":
                    return false;          // no rows
                case "close":
                    return null;           // ignore
                case "unwrap":
                    return null;
                case "isWrapperFor":
                    return false;
                default:
                    throw new UnsupportedOperationException("ResultSet." + name);
            }
        };
        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
            ResultSet.class.getClassLoader(),
            new Class<?>[]{ResultSet.class},
            rsHandler
        );

        // --- PreparedStatement stub: executeQuery() -> our empty ResultSet ---
        InvocationHandler stmtHandler = (Object proxy, Method method, Object[] args) -> {
            String name = method.getName();
            switch (name) {
                case "executeQuery":
                    return rs;
                case "close":
                    return null;
                case "unwrap":
                    return null;
                case "isWrapperFor":
                    return false;
                default:
                    throw new UnsupportedOperationException("PreparedStatement." + name);
            }
        };
        PreparedStatement stmt = (PreparedStatement) Proxy.newProxyInstance(
            PreparedStatement.class.getClassLoader(),
            new Class<?>[]{PreparedStatement.class},
            stmtHandler
        );

        // --- Connection stub: prepareStatement(...) -> our stub statement ---
        InvocationHandler connHandler = (Object proxy, Method method, Object[] args) -> {
            String name = method.getName();
            switch (name) {
                case "prepareStatement":
                case "createStatement":
                    return stmt;
                case "close":
                    return null;
                case "isClosed":
                    return false;
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
            connHandler
        );
    }

    /** Db stub that always returns our special Connection. */
    private static class EmptyResultDb extends Db {
        private final Connection conn;

        EmptyResultDb(Connection conn) {
            this.conn = conn;
        }

        @Override
        public Connection getConnection() {
            return conn;
        }
    }

    @Test
    void returnsEmptyListsAndZeroWhenQueriesReturnNoRows() {
        Connection conn = emptyResultConnection();
        Db db = new EmptyResultDb(conn);
        PopulationRepo repo = new PopulationRepo(db);

        List<PopulationRow> regions = repo.findPopulationByRegionInOutCities();
        List<PopulationRow> countries = repo.findPopulationByCountryInOutCities();
        long world = repo.findWorldPopulation();

        assertNotNull(regions);
        assertTrue(regions.isEmpty(), "Regions should be empty when no rows are returned");

        assertNotNull(countries);
        assertTrue(countries.isEmpty(), "Countries should be empty when no rows are returned");

        assertEquals(0L, world, "World population should be 0 when no rows are returned");
    }
}
