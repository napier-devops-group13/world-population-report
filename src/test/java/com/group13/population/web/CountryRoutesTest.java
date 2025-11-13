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

public class CountryRoutesTest {

    private static WorldRepo.CountryRow row(String code, String name, String cont,
                                            String region, long pop, String cap) {
        return new WorldRepo.CountryRow(code, name, cont, region, pop, cap);
    }

    @Test
    void world_endpoint_200_and_desc_order() throws Exception {
        var seed = List.of(
            row("A", "A", "X", "R", 5,  "cA"),
            row("B", "B", "X", "R", 15, "cB"),
            row("C", "C", "X", "R", 10, "cC")
        );

        var service = new CountryService(new FakeCountryRepo(seed));
        var routes  = new CountryRoutes(service);
        var mapper  = new ObjectMapper();

        // build the app and register routes up-front,
        // then pass it to JavalinTest.test(app, (server, client) -> { ... })
        Javalin app = Javalin.create();
        routes.register(app);

        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/api/countries/world");
            assertEquals(200, res.code());

            JsonNode list = mapper.readTree(res.body().string());
            assertEquals(3, list.size());
            long p0 = list.get(0).get("population").asLong();
            long p1 = list.get(1).get("population").asLong();
            long p2 = list.get(2).get("population").asLong();
            assertTrue(p0 >= p1);
            assertTrue(p1 >= p2);
        });
    }

    @Test
    void bad_top_param_returns_400() {
        var service = new CountryService(new FakeCountryRepo(List.of()));
        var routes  = new CountryRoutes(service);

        Javalin app = Javalin.create();
        routes.register(app);

        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/api/countries/world/top/0"); // invalid n
            assertEquals(400, res.code());
        });
    }
}
