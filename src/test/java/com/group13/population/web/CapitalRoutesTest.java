package com.group13.population.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group13.population.db.Db;
import com.group13.population.repo.CapitalRepo;
import com.group13.population.repo.CapitalRepoJdbc;
import com.group13.population.service.CapitalService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CapitalRoutes (R17–R22) using Javalin TestTools.
 */
class CapitalRoutesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Read a system property with a sensible default. This mirrors the
     * WorldRepo/CapitalRepo integration tests so CI + local runs behave
     * the same.
     */
    private static String prop(String key, String def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    /**
     * Build a Javalin app wired with CapitalRepo → CapitalService → CapitalRoutes.
     * This app is started/stopped by JavalinTest for each test.
     */
    private static Javalin createApp() {
        try {
            // DB connection (works locally and in CI)
            String host = prop("DB_HOST", "localhost");
            int port = Integer.parseInt(prop("DB_PORT", "43306"));
            String dbName = prop("DB_NAME", "world");
            String user = prop("DB_USER", "app");
            String pass = prop("DB_PASS", "app");

            Connection conn = Db.connect(host, port, dbName, user, pass);

            CapitalRepo repo = new CapitalRepoJdbc(conn);
            CapitalService service = new CapitalService(repo);
            CapitalRoutes routes = new CapitalRoutes(service);

            Javalin app = Javalin.create((JavalinConfig cfg) -> {
                cfg.showJavalinBanner = false;
            });

            // Health endpoint (same behaviour as real app)
            app.get("/health", ctx -> ctx.result("OK"));

            // Register R17–R22 HTTP routes
            routes.register(app);

            return app;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create Javalin app for capital routes tests", ex);
        }
    }

    /**
     * Helper: GET a path, assert 200, and parse the JSON body.
     */
    private static JsonNode getJson(HttpClient client, String path) throws IOException {
        var res = client.get(path);
        assertEquals(200, res.code(), "Expected 200 for " + path);
        return MAPPER.readTree(res.body().string());
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Health endpoint returns 200 OK")
    void healthEndpointReturnsOK() {
        JavalinTest.test(createApp(), (server, client) -> {
            var res = client.get("/health");
            assertEquals(200, res.code());
            assertEquals("OK", res.body().string());
        });
    }

    @Test
    @DisplayName("R17 – all capital cities in the world are sorted by population DESC")
    void r17_worldAllCapitals_sortedByPopulationDesc() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            JsonNode list = getJson(client, "/api/capitals/world");

            assertTrue(list.isArray(), "Expected JSON array for world capitals");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Capitals must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R18 – capitals in a continent are sorted by population DESC")
    void r18_allCapitalsInContinent_sorted() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            JsonNode list = getJson(client, "/api/capitals/continent/Asia");

            assertTrue(list.isArray(), "Expected JSON array for continent capitals");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Capitals must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R19 – capitals in a region are sorted by population DESC")
    void r19_allCapitalsInRegion_sorted() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            JsonNode list = getJson(client, "/api/capitals/region/Caribbean");

            assertTrue(list.isArray(), "Expected JSON array for region capitals");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Capitals must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R20 – top-N capitals in the world return at most N rows, sorted DESC")
    void r20_topNCapitalsWorld_happyPath() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            int n = 5;
            JsonNode list = getJson(client, "/api/capitals/world/top/" + n);

            assertTrue(list.isArray());
            assertTrue(list.size() <= n, "Should return at most N rows");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Capitals must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R21 – top-N capitals in a continent are limited and sorted")
    void r21_topNCapitalsContinent_happyPath() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            int n = 3;
            JsonNode list = getJson(client, "/api/capitals/continent/Asia/top/" + n);

            assertTrue(list.isArray());
            assertTrue(list.size() <= n, "Should return at most N rows");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Capitals must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R22 – top-N capitals in a region are limited and sorted")
    void r22_topNCapitalsRegion_happyPath() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            int n = 3;
            JsonNode list = getJson(client, "/api/capitals/region/Caribbean/top/" + n);

            assertTrue(list.isArray());
            assertTrue(list.size() <= n, "Should return at most N rows");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Capitals must be sorted by population DESC");
                previous = pop;
            }
        });
    }
}
