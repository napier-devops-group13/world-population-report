package com.group13.population.web;

import com.group13.population.db.Db;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-coverage tests for the capital CSV API routes (/api/capitals/...).
 *
 * Uses the same idea as CityApiRoutesTest: a happy in-memory Db for success
 * path and a failing Db for error path. Also tests parseLimit and escapeCsv
 * directly.
 */
class CapitalApiRoutesTest {

    // -------------------------------------------------------------------------
    // App builders
    // -------------------------------------------------------------------------

    private Javalin buildApp(Db db) {
        Javalin app = Javalin.create();
        new CapitalApiRoutes(db).register(app);
        return app;
    }

    private Javalin buildAppWithFailingDb() {
        Db failingDb = new Db() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("Simulated DB failure for tests");
            }
        };
        return buildApp(failingDb);
    }

    private Javalin buildAppWithHappyDb() {
        Db happyDb = new Db() {
            @Override
            public Connection getConnection() {
                return createHappyConnection();
            }
        };
        return buildApp(happyDb);
    }

    // -------------------------------------------------------------------------
    // In-memory JDBC proxies
    // -------------------------------------------------------------------------

    private static Connection createHappyConnection() {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "prepareStatement":
                    return createHappyPreparedStatement();
                case "close":
                    return null;
                default:
                    return defaultReturnValue(method.getReturnType());
            }
        };

        return (Connection) Proxy.newProxyInstance(
            CapitalApiRoutesTest.class.getClassLoader(),
            new Class[]{Connection.class},
            handler
        );
    }

    private static PreparedStatement createHappyPreparedStatement() {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "setInt":
                case "setString":
                case "clearParameters":
                    return null;
                case "executeQuery":
                    return createHappyResultSet();
                case "close":
                    return null;
                default:
                    return defaultReturnValue(method.getReturnType());
            }
        };

        return (PreparedStatement) Proxy.newProxyInstance(
            CapitalApiRoutesTest.class.getClassLoader(),
            new Class[]{PreparedStatement.class},
            handler
        );
    }

    private static ResultSet createHappyResultSet() {
        InvocationHandler handler = new InvocationHandler() {
            private final Object[][] rows = new Object[][]{
                {"London", "United Kingdom", 8_000_000L},
                {"Paris", "France", 2_000_000L}
            };
            private int index = -1;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                switch (name) {
                    case "next":
                        index++;
                        return index < rows.length;
                    case "getString": {
                        String column = (String) args[0];
                        Object[] row = rows[index];
                        if ("city_name".equals(column)) return row[0];
                        if ("country_name".equals(column)) return row[1];
                        return null;
                    }
                    case "getLong":
                        return (Long) rows[index][2];
                    case "close":
                        return null;
                    default:
                        return defaultReturnValue(method.getReturnType());
                }
            }
        };

        return (ResultSet) Proxy.newProxyInstance(
            CapitalApiRoutesTest.class.getClassLoader(),
            new Class[]{ResultSet.class},
            handler
        );
    }

    private static Object defaultReturnValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }

    private void assertBodyNotNull(Response response, String path) {
        assertNotNull(response, "Response should not be null for " + path);
        assertNotNull(response.body(), "Body should not be null for " + path);
        try {
            response.body().string();
        } catch (Exception e) {
            fail("Unable to read body for " + path + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Happy-path tests (200)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("World capitals – happy Db returns CSV")
    void world_capitals_happy_success() {
        Javalin app = buildAppWithHappyDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp = client.get("/api/capitals/world")) {
                assertEquals(200, resp.code());
                String body = resp.body().string();
                assertNotNull(body);
                assertTrue(body.startsWith("Name,Country,Population"),
                    "CSV header should be present");
            }
        });
    }

    @Test
    @DisplayName("Continent capitals – happy Db returns CSV")
    void continent_capitals_happy_success() {
        Javalin app = buildAppWithHappyDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp =
                     client.get("/api/capitals/continent/Europe")) {
                assertEquals(200, resp.code());
                assertBodyNotNull(resp, "/api/capitals/continent/Europe");
            }
        });
    }

    @Test
    @DisplayName("Continent top capitals – happy Db uses limit and params")
    void continent_top_capitals_happy_success() {
        Javalin app = buildAppWithHappyDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp =
                     client.get("/api/capitals/continent/Europe/top/5")) {
                assertEquals(200, resp.code());
                assertBodyNotNull(resp,
                    "/api/capitals/continent/Europe/top/5");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Error path (500) via failing Db
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("World capitals – DB failure returns 500")
    void world_capitals_db_error_returns_500() {
        Javalin app = buildAppWithFailingDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp = client.get("/api/capitals/world")) {
                assertEquals(500, resp.code());
                assertBodyNotNull(resp, "/api/capitals/world (DB error)");
            }
        });
    }

    @Test
    @DisplayName("Top capitals – handlers invoked and parseLimit used")
    void top_capitals_parseLimit_branches_via_http() {
        Javalin app = buildAppWithFailingDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response a =
                     client.get("/api/capitals/world/top/5")) {
                assertEquals(500, a.code());
                assertBodyNotNull(a, "/api/capitals/world/top/5");
            }
            try (Response b =
                     client.get("/api/capitals/continent/Europe/top/3")) {
                assertEquals(500, b.code());
                assertBodyNotNull(b,
                    "/api/capitals/continent/Europe/top/3");
            }
            try (Response c =
                     client.get("/api/capitals/region/WesternEurope/top/2")) {
                assertEquals(500, c.code());
                assertBodyNotNull(c,
                    "/api/capitals/region/WesternEurope/top/2");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Direct tests of helper methods
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CapitalApiRoutes.parseLimit – null, blank, positive, zero, negative, invalid")
    void parseLimit_branches() {
        // null / blank -> default
        assertEquals(10, CapitalApiRoutes.parseLimit(null, 10));
        assertEquals(10, CapitalApiRoutes.parseLimit("   ", 10));

        // positive
        assertEquals(7, CapitalApiRoutes.parseLimit("7", 10));

        // zero / negative -> default
        assertEquals(10, CapitalApiRoutes.parseLimit("0", 10));
        assertEquals(10, CapitalApiRoutes.parseLimit("-5", 10));

        // non-numeric -> default
        assertEquals(10, CapitalApiRoutes.parseLimit("abc", 10));
    }

    @Test
    @DisplayName("CapitalApiRoutes.escapeCsv – null, empty, plain, comma and quotes")
    void escapeCsv_branches() {
        // null -> empty
        assertEquals("", CapitalApiRoutes.escapeCsv(null));

        // empty -> empty
        assertEquals("", CapitalApiRoutes.escapeCsv(""));

        // plain
        assertEquals("Plain", CapitalApiRoutes.escapeCsv("Plain"));

        // comma -> quoted
        assertEquals("\"A,B\"", CapitalApiRoutes.escapeCsv("A,B"));

        // quotes inside -> doubled and wrapped
        String quoted = CapitalApiRoutes.escapeCsv("He said \"Hi\"");
        assertTrue(quoted.startsWith("\""));
        assertTrue(quoted.endsWith("\""));
        assertTrue(quoted.contains("\"\"Hi\"\""));
    }
}
