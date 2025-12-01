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
 * Extra edge-case coverage for PopulationRepo:
 *
 * Simulates a query that returns EXACTLY one row with odd values,
 * so any guards / branches inside the mapping code are exercised.
 */
class PopulationRepoEdgeCaseTest {

    // ---------- Dynamic stubs ----------------------------------------------

    private static ResultSet oneOddRowResultSet() {
        InvocationHandler handler = new InvocationHandler() {
            boolean first = true;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "next":
                        // one row only
                        if (first) {
                            first = false;
                            return true;
                        }
                        return false;

                    // label column – sometimes regions, sometimes countries
                    case "getString":
                        // rs.getString("Region") or rs.getString("Name") etc.
                        return "";  // blank name to hit any "blank → skip" guards

                    // numeric columns
                    case "getLong":
                        // e.g., "total_population", "in_cities"
                        return 0L; // 0 values often trigger special branches

                    case "close":
                        return null;

                    case "wasNull":
                        return false;

                    case "unwrap":
                        return null;

                    case "isWrapperFor":
                        return false;

                    default:
                        // Anything unexpected: let us see it quickly
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
                    // ignore parameter setting
                    return null;
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

    // ---------- The test ----------------------------------------------------

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

        // Repository should still behave sensibly on this odd data:
        assertNotNull(regions);
        assertNotNull(countries);
        assertFalse(world < 0, "World population should never be negative");
    }
}
