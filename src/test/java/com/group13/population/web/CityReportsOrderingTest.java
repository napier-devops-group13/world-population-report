package com.group13.population.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group13.population.model.City;
import com.group13.population.repo.CityWorldRepo;
import com.group13.population.service.CityService;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end ordering tests for the City reports (R07–R16).
 *
 * HTTP -> CityRoutes -> CityService -> CityWorldRepo -> DB
 */
class CityReportsOrderingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Build (but do not start) a Javalin app with only city routes. */
    private static Javalin createCityApp() {
        CityWorldRepo repo = new CityWorldRepo();
        CityService service = new CityService(repo);
        CityRoutes routes = new CityRoutes(service);

        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);
        routes.register(app);
        app.get("/health", ctx -> ctx.result("OK"));
        return app;
    }

    private static City[] readCities(Response res) throws IOException {
        assertNotNull(res.body(), "HTTP response body should not be null");
        String json = res.body().string();
        return MAPPER.readValue(json, City[].class);
    }

    // ======================================================================
    // R07 – world report ordering
    // ======================================================================

    @Test
    @DisplayName("R07 – /api/cities/world returns all cities ordered by population DESC")
    void worldReport_isOrderedByPopulationDescending() {
        Javalin app = createCityApp();

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/cities/world")) {
                assertEquals(200, res.code());

                City[] cities = readCities(res);
                assertTrue(cities.length > 0, "Expected at least one city");

                City first = cities[0];
                assertEquals("Mumbai (Bombay)", first.getName());
                assertEquals(10_500_000L, first.getPopulation());

                for (int i = 1; i < cities.length; i++) {
                    long prev = cities[i - 1].getPopulation();
                    long curr = cities[i].getPopulation();
                    assertTrue(prev >= curr,
                        "Cities must be ordered by population DESC (index " + i + ")");
                }
            }
        });
    }

    // ======================================================================
    // R12 – world top-N report
    // ======================================================================

    @Test
    @DisplayName("R12 – /api/cities/world/top?n=N respects N and ordering")
    void worldTopN_respectsLimitAndOrdering() {
        Javalin app = createCityApp();

        JavalinTest.test(app, (server, client) -> {
            int n = 5;
            try (Response res = client.get("/api/cities/world/top?n=" + n)) {
                assertEquals(200, res.code());

                City[] cities = readCities(res);
                assertEquals(n, cities.length, "Expected exactly N cities");

                assertEquals("Mumbai (Bombay)", cities[0].getName());
                assertEquals(10_500_000L, cities[0].getPopulation());

                for (int i = 1; i < cities.length; i++) {
                    long prev = cities[i - 1].getPopulation();
                    long curr = cities[i].getPopulation();
                    assertTrue(prev >= curr,
                        "Top-N cities must be ordered by population DESC (index " + i + ")");
                }
            }
        });
    }

    // ======================================================================
    // R11 / R16 – district ordering (New York)
    // ======================================================================

    @Test
    @DisplayName("R11/R16 – /api/cities/districts/New York is ordered by population DESC")
    void districtNewYork_isOrderedByPopulationDescending() {
        Javalin app = createCityApp();

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/cities/districts/New%20York")) {
                assertEquals(200, res.code());

                City[] cities = readCities(res);
                assertEquals(6, cities.length,
                    "Classic world DB has six cities in district 'New York'");

                String[] expectedNames = {
                    "New York",
                    "Buffalo",
                    "Rochester",
                    "Yonkers",
                    "Syracuse",
                    "Albany"
                };

                String[] actualNames = Arrays.stream(cities)
                    .map(City::getName)
                    .toArray(String[]::new);

                assertArrayEquals(expectedNames, actualNames,
                    "District 'New York' cities should be ordered by population DESC");

                for (int i = 1; i < cities.length; i++) {
                    long prev = cities[i - 1].getPopulation();
                    long curr = cities[i].getPopulation();
                    assertTrue(prev >= curr,
                        "District rows must be ordered by population DESC (index " + i + ")");
                }
            }
        });
    }
}
