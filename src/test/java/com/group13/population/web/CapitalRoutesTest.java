package com.group13.population.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.group13.population.model.CapitalCity;
import com.group13.population.repo.FakeCapitalRepo;
import com.group13.population.service.CapitalService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Route-level tests for capital city reports (R17–R22).
 *
 * Uses FakeCapitalRepo so these tests never talk to the real database.
 */
class CapitalRoutesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Build a tiny Javalin app just for tests – no DB, no external config. */
    private static Javalin createApp() {
        // Seed the fake repo with a few capitals – the repo will normalise ordering.
        List<CapitalCity> seed = List.of(
            new CapitalCity("Seoul", "South Korea", 9_981_619L),
            new CapitalCity("Jakarta", "Indonesia", 9_604_900L),
            new CapitalCity("Ciudad de México", "Mexico", 8_591_309L),
            new CapitalCity("Moscow", "Russian Federation", 8_389_200L),
            new CapitalCity("Tokyo", "Japan", 7_980_230L),
            new CapitalCity("Beijing", "China", 7_230_000L),
            new CapitalCity("Manila", "Philippines", 7_200_000L)
        );

        FakeCapitalRepo repo = new FakeCapitalRepo(seed);
        CapitalService service = new CapitalService(repo);
        CapitalRoutes routes = new CapitalRoutes(service);

        Javalin app = Javalin.create((JavalinConfig cfg) -> cfg.showJavalinBanner = false);
        routes.register(app);
        app.get("/health", ctx -> ctx.result("OK"));
        return app;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static List<CapitalCity> readCapitalList(Response res) {
        try {
            String json = res.body().string();
            CollectionType listType = MAPPER.getTypeFactory()
                .constructCollectionType(List.class, CapitalCity.class);
            return MAPPER.readValue(json, listType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void assertSortedByPopulationDesc(List<CapitalCity> capitals) {
        long previous = Long.MAX_VALUE;
        for (CapitalCity c : capitals) {
            long pop = c.getPopulation();   // <-- use getter from your model
            assertTrue(pop <= previous, "Populations should be in descending order");
            previous = pop;
        }
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void healthEndpointReturnsOK() {
        JavalinTest.test(
            createApp(),                                      // pass instance, not method reference
            (Javalin server, HttpClient client) -> {
                try (Response res = client.get("/health")) {
                    assertEquals(200, res.code());
                    assertEquals("OK", res.body().string());
                }
            }
        );
    }

    @Test
    void r17_worldAllCapitals_sortedByPopulationDesc() {
        JavalinTest.test(
            createApp(),
            (server, client) -> {
                try (Response res = client.get("/api/capitals/world")) {
                    assertEquals(200, res.code());
                    List<CapitalCity> capitals = readCapitalList(res);
                    assertFalse(capitals.isEmpty(), "World capitals list should not be empty");
                    assertSortedByPopulationDesc(capitals);
                }
            }
        );
    }

    @Test
    void r18_allCapitalsInContinent_sortedByPopulationDesc() {
        JavalinTest.test(
            createApp(),
            (server, client) -> {
                try (Response res = client.get("/api/capitals/continent/Asia")) {
                    assertEquals(200, res.code());
                    List<CapitalCity> capitals = readCapitalList(res);
                    assertFalse(capitals.isEmpty(), "Asia capitals list should not be empty");
                    assertSortedByPopulationDesc(capitals);
                }
            }
        );
    }

    @Test
    void r19_allCapitalsInRegion_sortedByPopulationDesc() {
        JavalinTest.test(
            createApp(),
            (server, client) -> {
                try (Response res = client.get("/api/capitals/region/Eastern%20Asia")) {
                    assertEquals(200, res.code());
                    List<CapitalCity> capitals = readCapitalList(res);
                    assertFalse(capitals.isEmpty(), "Region capitals list should not be empty");
                    assertSortedByPopulationDesc(capitals);
                }
            }
        );
    }

    @Test
    void r20_topNCapitalsWorld_happyPath() {
        JavalinTest.test(
            createApp(),
            (server, client) -> {
                try (Response res = client.get("/api/capitals/world/top/5")) {
                    assertEquals(200, res.code());
                    List<CapitalCity> capitals = readCapitalList(res);
                    assertEquals(5, capitals.size(), "Should return top 5 capitals in the world");
                    assertSortedByPopulationDesc(capitals);
                }
            }
        );
    }

    @Test
    void r21_topNCapitalsContinent_happyPath() {
        JavalinTest.test(
            createApp(),
            (server, client) -> {
                try (Response res = client.get("/api/capitals/continent/Asia/top/5")) {
                    assertEquals(200, res.code());
                    List<CapitalCity> capitals = readCapitalList(res);
                    assertEquals(5, capitals.size(), "Should return top 5 capitals in Asia");
                    assertSortedByPopulationDesc(capitals);
                }
            }
        );
    }

    @Test
    void r22_topNCapitalsRegion_happyPath() {
        JavalinTest.test(
            createApp(),
            (server, client) -> {
                try (Response res =
                         client.get("/api/capitals/region/Eastern%20Asia/top/5")) {
                    assertEquals(200, res.code());
                    List<CapitalCity> capitals = readCapitalList(res);
                    assertEquals(5, capitals.size(), "Should return top 5 capitals in Eastern Asia");
                    assertSortedByPopulationDesc(capitals);
                }
            }
        );
    }
}
