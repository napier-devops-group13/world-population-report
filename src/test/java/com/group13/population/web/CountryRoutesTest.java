package com.group13.population.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group13.population.repo.FakeCountryRepo;
import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Route-level tests for country endpoints (R01–R06).
 * Uses Javalin TestTools with a FakeCountryRepo (no real DB).
 */
class CountryRoutesTest {

    /**
     * Build a fresh Javalin app wired with a FakeCountryRepo.
     * JavalinTest will start/stop this app for each test.
     */
    private static Javalin createApp() {
        // Small in-memory data set for the fake repo
        List<WorldRepo.CountryRow> seedRows = List.of(
            new WorldRepo.CountryRow("CHN", "China", "Asia", "Eastern Asia",
                1_439_323_776L, "Beijing"),
            new WorldRepo.CountryRow("IND", "India", "Asia", "Southern Asia",
                1_380_004_385L, "New Delhi"),
            new WorldRepo.CountryRow("USA", "United States", "North America",
                "North America", 331_002_651L, "Washington"),
            new WorldRepo.CountryRow("IDN", "Indonesia", "Asia", "South-Eastern Asia",
                273_523_615L, "Jakarta"),
            new WorldRepo.CountryRow("PAK", "Pakistan", "Asia", "Southern Asia",
                220_892_340L, "Islamabad")
        );

        FakeCountryRepo repo = new FakeCountryRepo(seedRows);
        CountryService service = new CountryService(repo);
        CountryRoutes routes = new CountryRoutes(service);

        // NOTE: do NOT call start() here; JavalinTest handles that.
        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

        routes.register(app);
        app.get("/health", ctx -> ctx.result("OK"));

        return app;
    }

    @Test
    void healthEndpointReturnsOK() throws Exception {
        Javalin app = createApp();

        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/health");
            assertEquals(200, res.code());
            assertEquals("OK", res.body().string());
        });
    }

    @Test
    void r04_worldTopNCountriesHappyPath() throws Exception {
        Javalin app = createApp();

        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/api/countries/world/top/3");
            assertEquals(200, res.code());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode list = mapper.readTree(res.body().string());

            assertTrue(list.isArray());
            assertEquals(3, list.size());
            // Check descending population order for top-3
            assertTrue(
                list.get(0).get("population").asLong()
                    >= list.get(1).get("population").asLong()
            );
            assertTrue(
                list.get(1).get("population").asLong()
                    >= list.get(2).get("population").asLong()
            );
        });
    }
}
