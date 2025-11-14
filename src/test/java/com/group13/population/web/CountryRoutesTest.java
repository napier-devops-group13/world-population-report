package com.group13.population.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CountryRoutes (R01–R06) using Javalin TestTools.
 */
class CountryRoutesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Build a Javalin app wired with WorldRepo → CountryService → CountryRoutes.
     * This app is started/stopped by JavalinTest for each test.
     */
    private static Javalin createApp() {
        WorldRepo repo = new WorldRepo();
        CountryService service = new CountryService(repo);
        CountryRoutes routes = new CountryRoutes(service);

        Javalin app = Javalin.create((JavalinConfig cfg) -> {
            cfg.showJavalinBanner = false;
        });

        // Health endpoint (same behaviour as real app)
        app.get("/health", ctx -> ctx.result("OK"));

        // Register R01–R06 HTTP routes
        routes.register(app);

        return app;
    }

    /**
     * Helper: GET a path, assert 200, and parse the JSON body.
     */
    private static JsonNode getJson(HttpClient client, String path) throws IOException {
        var res = client.get(path);
        assertEquals(200, res.code(), "Expected 200 for " + path);
        return MAPPER.readTree(res.body().string());
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

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
    @DisplayName("R01 – all countries in the world are sorted by population DESC")
    void r01_worldAllCountries_sortedByPopulationDesc() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            JsonNode list = getJson(client, "/api/countries/world");

            assertTrue(list.isArray(), "Expected JSON array for world countries");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Countries must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R02 – all countries in a continent are filtered to that continent and sorted")
    void r02_allCountriesInContinent_filterAndSort() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            JsonNode list = getJson(client, "/api/countries/continent/Asia");

            assertTrue(list.isArray(), "Expected JSON array for continent countries");

            // Every row (if any) should be in Asia and ordered by population DESC
            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                assertEquals("Asia", node.get("continent").asText(),
                    "Country should be in Asia");
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Countries must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R03 – all countries in a region are filtered to that region and sorted")
    void r03_allCountriesInRegion_filterAndSort() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            // Region name without spaces; "Caribbean" is a valid region in world DB
            JsonNode list = getJson(client, "/api/countries/region/Caribbean");

            assertTrue(list.isArray(), "Expected JSON array for region countries");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                assertEquals("Caribbean", node.get("region").asText(),
                    "Country should be in Caribbean region");
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Countries must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R04 – top-N countries in the world return at most N rows sorted by population DESC")
    void r04_topNWorld_happyPath() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            int n = 5;
            JsonNode list = getJson(client, "/api/countries/world/top/" + n);

            assertTrue(list.isArray());
            assertTrue(list.size() <= n, "Should return at most N rows");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Countries must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R05 – top-N countries in a continent are filtered and limited correctly")
    void r05_topNContinent_happyPath() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            int n = 3;
            JsonNode list = getJson(client, "/api/countries/continent/Asia/top/" + n);

            assertTrue(list.isArray());
            assertTrue(list.size() <= n, "Should return at most N rows");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                assertEquals("Asia", node.get("continent").asText(),
                    "Country should be in Asia");
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Countries must be sorted by population DESC");
                previous = pop;
            }
        });
    }

    @Test
    @DisplayName("R06 – top-N countries in a region are filtered and limited correctly")
    void r06_topNRegion_happyPath() throws Exception {
        JavalinTest.test(createApp(), (server, client) -> {
            int n = 3;
            JsonNode list = getJson(client, "/api/countries/region/Caribbean/top/" + n);

            assertTrue(list.isArray());
            assertTrue(list.size() <= n, "Should return at most N rows");

            long previous = Long.MAX_VALUE;
            for (JsonNode node : list) {
                assertEquals("Caribbean", node.get("region").asText(),
                    "Country should be in Caribbean region");
                long pop = node.get("population").asLong();
                assertTrue(pop <= previous, "Countries must be sorted by population DESC");
                previous = pop;
            }
        });
    }
}
