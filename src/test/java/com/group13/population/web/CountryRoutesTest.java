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
     * Build a small fake app using an in-memory repository.
     */
    private static Javalin createApp() {
        // Seed data used by FakeCountryRepo (no real database)
        List<WorldRepo.CountryRow> seed = List.of(
            new WorldRepo.CountryRow("A", "A", "X", "Y", 50, "cA"),
            new WorldRepo.CountryRow("B", "B", "X", "Y", 40, "cB"),
            new WorldRepo.CountryRow("C", "C", "X", "Y", 30, "cC")
        );

        FakeCountryRepo repo = new FakeCountryRepo(seed);
        CountryService service = new CountryService(repo);
        CountryRoutes routes = new CountryRoutes(service);

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
            assertTrue(
                list.get(0).get("population").asLong() >=
                    list.get(1).get("population").asLong()
            );
        });
    }
}
