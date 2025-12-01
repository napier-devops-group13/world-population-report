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
 * High-coverage tests for the city CSV API routes (/api/cities/...).
 *
 * We use two kinds of Db:
 *  - a "happy" Db that returns a fake Connection/ResultSet with in-memory rows
 *    so the full CSV-building logic is executed;
 *  - a "failing" Db that always throws SQLException so we hit the error path.
 *
 * This gives very high JaCoCo coverage of CityApiRoutes without needing a real DB.
 */
class CityApiRoutesTest {

    // -------------------------------------------------------------------------
    // App builders
    // -------------------------------------------------------------------------

    private Javalin buildApp(Db db) {
        Javalin app = Javalin.create();
        new CityApiRoutes(db).register(app);
        return app;
    }

    /** Db that always throws SQLException from getConnection() – exercises error path. */
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
    // Tiny in-memory JDBC implementation backed by dynamic proxies
    // -------------------------------------------------------------------------

    private static Connection createHappyConnection() {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "prepareStatement":
                    // Always return the same happy PreparedStatement
                    return createHappyPreparedStatement();
                case "close":
                    return null; // no-op
                default:
                    return defaultReturnValue(method.getReturnType());
            }
        };

        return (Connection) Proxy.newProxyInstance(
            CityApiRoutesTest.class.getClassLoader(),
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
                    // We ignore parameters – they don't affect coverage.
                    return null;
                case "executeQuery":
                    return createHappyResultSet();
                case "close":
                    return null; // no-op
                default:
                    return defaultReturnValue(method.getReturnType());
            }
        };

        return (PreparedStatement) Proxy.newProxyInstance(
            CityApiRoutesTest.class.getClassLoader(),
            new Class[]{PreparedStatement.class},
            handler
        );
    }

    private static ResultSet createHappyResultSet() {
        InvocationHandler handler = new InvocationHandler() {
            // Two fake rows to drive the while(rs.next()) loop
            private final Object[][] rows = new Object[][]{
                {"Edinburgh", "United Kingdom", "Scotland", 100_000L},
                {"Glasgow", "United Kingdom", "Scotland", 200_000L}
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
                        if ("district".equals(column)) return row[2];
                        return null;
                    }
                    case "getLong":
                        return (Long) rows[index][3];
                    case "close":
                        return null; // no-op
                    default:
                        return defaultReturnValue(method.getReturnType());
                }
            }
        };

        return (ResultSet) Proxy.newProxyInstance(
            CityApiRoutesTest.class.getClassLoader(),
            new Class[]{ResultSet.class},
            handler
        );
    }

    /** Default value for unused proxy methods so we don't accidentally throw. */
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

    // -------------------------------------------------------------------------
    // Small helper – consume body so JaCoCo sees handler fully executed
    // -------------------------------------------------------------------------

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
    // Happy-path tests (200) – cover full CSV-building logic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("World cities – happy Db returns CSV (success path)")
    void world_cities_happy_success() {
        Javalin app = buildAppWithHappyDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp = client.get("/api/cities/world")) {
                assertEquals(200, resp.code());
                String body = resp.body().string();
                assertNotNull(body);
                assertTrue(body.startsWith("Name,Country,District,Population"),
                    "CSV header should be present");
            }
        });
    }

    @Test
    @DisplayName("Continent cities – happy Db returns CSV")
    void continent_cities_happy_success() {
        Javalin app = buildAppWithHappyDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp = client.get("/api/cities/continent/Europe")) {
                assertEquals(200, resp.code());
                assertBodyNotNull(resp, "/api/cities/continent/Europe");
            }
        });
    }

    @Test
    @DisplayName("Continent top cities – happy Db uses limit and params")
    void continent_top_cities_happy_success() {
        Javalin app = buildAppWithHappyDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp =
                     client.get("/api/cities/continent/Europe/top?n=5")) {
                assertEquals(200, resp.code());
                assertBodyNotNull(resp,
                    "/api/cities/continent/Europe/top?n=5");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Error-path tests (500) – Db throws and streamCitiesAsCsv catches it
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("World cities – DB failure returns 500")
    void world_cities_db_error_returns_500() {
        Javalin app = buildAppWithFailingDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response resp = client.get("/api/cities/world")) {
                assertEquals(500, resp.code());
                assertBodyNotNull(resp, "/api/cities/world (DB error)");
            }
        });
    }

    @Test
    @DisplayName("Other 'all' handlers – DB failure still returns 500")
    void other_all_handlers_db_error() {
        Javalin app = buildAppWithFailingDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response a =
                     client.get("/api/cities/region/WesternEurope")) {
                assertEquals(500, a.code());
                assertBodyNotNull(a, "/api/cities/region/WesternEurope");
            }
            try (Response b =
                     client.get("/api/cities/country/United%20Kingdom")) {
                assertEquals(500, b.code());
                assertBodyNotNull(b,
                    "/api/cities/country/United Kingdom");
            }
            try (Response c =
                     client.get("/api/cities/district/Scotland")) {
                assertEquals(500, c.code());
                assertBodyNotNull(c,
                    "/api/cities/district/Scotland");
            }
        });
    }

    // -------------------------------------------------------------------------
    // parseLimit branches via world/top endpoint
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("World top cities – parseLimit branches (missing, blank, ok, 0, negative, invalid)")
    void world_top_parseLimit_branches() {
        Javalin app = buildAppWithFailingDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response r1 = client.get("/api/cities/world/top")) {
                assertEquals(500, r1.code());
                assertBodyNotNull(r1, "/api/cities/world/top");
            }
            try (Response r2 = client.get("/api/cities/world/top?n=")) {
                assertEquals(500, r2.code());
                assertBodyNotNull(r2, "/api/cities/world/top?n=");
            }
            try (Response r3 = client.get("/api/cities/world/top?n=5")) {
                assertEquals(500, r3.code());
                assertBodyNotNull(r3, "/api/cities/world/top?n=5");
            }
            try (Response r4 = client.get("/api/cities/world/top?n=0")) {
                assertEquals(500, r4.code());
                assertBodyNotNull(r4, "/api/cities/world/top?n=0");
            }
            try (Response r5 = client.get("/api/cities/world/top?n=-5")) {
                assertEquals(500, r5.code());
                assertBodyNotNull(r5, "/api/cities/world/top?n=-5");
            }
            try (Response r6 = client.get("/api/cities/world/top?n=abc")) {
                assertEquals(500, r6.code());
                assertBodyNotNull(r6, "/api/cities/world/top?n=abc");
            }
        });
    }

    @Test
    @DisplayName("Region/country/district top cities – handlers invoked and limits parsed")
    void other_top_handlers_db_error() {
        Javalin app = buildAppWithFailingDb();

        JavalinTest.test(app, (server, client) -> {
            try (Response a =
                     client.get("/api/cities/continent/Europe/top?n=3")) {
                assertEquals(500, a.code());
                assertBodyNotNull(a,
                    "/api/cities/continent/Europe/top?n=3");
            }
            try (Response b =
                     client.get("/api/cities/region/WesternEurope/top?n=2")) {
                assertEquals(500, b.code());
                assertBodyNotNull(b,
                    "/api/cities/region/WesternEurope/top?n=2");
            }
            try (Response c =
                     client.get("/api/cities/country/United%20Kingdom/top?n=1")) {
                assertEquals(500, c.code());
                assertBodyNotNull(c,
                    "/api/cities/country/United Kingdom/top?n=1");
            }
            try (Response d =
                     client.get("/api/cities/district/Scotland/top?n=1")) {
                assertEquals(500, d.code());
                assertBodyNotNull(d,
                    "/api/cities/district/Scotland/top?n=1");
            }
        });
    }

    // -------------------------------------------------------------------------
    // escapeCsv helper – covers null, empty, plain and quoted values
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CityApiRoutes.escapeCsv – null, empty, plain and quoted values")
    void escapeCsv_branches() {
        // null -> empty
        assertEquals("", CityApiRoutes.escapeCsv(null));

        // empty string -> empty (value.isEmpty() branch)
        assertEquals("", CityApiRoutes.escapeCsv(""));

        // simple value -> unchanged, no quotes
        assertEquals("Plain", CityApiRoutes.escapeCsv("Plain"));

        // value with comma and quotes -> wrapped and quotes doubled
        String complex = CityApiRoutes.escapeCsv("A,B\"C");
        assertTrue(complex.startsWith("\""));
        assertTrue(complex.endsWith("\""));
        assertTrue(complex.contains("A,B"));
        assertTrue(complex.contains("\"\"C"));
    }
}
