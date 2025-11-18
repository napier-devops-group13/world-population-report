package com.group13.population.web;

import com.group13.population.model.City;
import com.group13.population.repo.CityRepo;
import com.group13.population.service.CityService;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP-level tests for {@link CityRoutes}.
 *
 * Uses a recording {@link CityRepo} so we only test wiring:
 *   HTTP -> CityRoutes -> CityService -> CityRepo
 */
class CityRoutesTest {

    private static final City SAMPLE_CITY =
        new City("Testopolis", "Testland", "Testshire", 1234L);

    // ---------------------------------------------------------------------
    // R07 – /api/cities/world
    // ---------------------------------------------------------------------

    @Test
    void worldAllEndpointReturnsJsonAndDelegatesToRepo() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);
        CityRoutes routes = new CityRoutes(service);

        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);
        routes.register(app);

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/cities/world")) {
                assertEquals(200, res.code());
                String body = requireBody(res);
                assertTrue(body.contains("Testopolis"));
                assertTrue(body.contains("\"population\":1234"));
            }
        });

        assertEquals("worldAll", repo.lastMethod);
    }

    // ---------------------------------------------------------------------
    // R08 – /api/cities/continents/{continent}
    // ---------------------------------------------------------------------

    @Test
    void continentAllEndpointUsesPathParam() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);
        CityRoutes routes = new CityRoutes(service);

        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);
        routes.register(app);

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/cities/continents/Europe")) {
                assertEquals(200, res.code());
                requireBody(res); // just ensure the body is readable JSON
            }
        });

        assertEquals("continentAll", repo.lastMethod);
        assertEquals("Europe", repo.lastContinent);
    }

    // ---------------------------------------------------------------------
    // R12 – /api/cities/world/top?n=N
    // ---------------------------------------------------------------------

    @Test
    void worldTopNEndpointReadsQueryParamN() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);
        CityRoutes routes = new CityRoutes(service);

        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);
        routes.register(app);

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/cities/world/top?n=3")) {
                assertEquals(200, res.code());
                requireBody(res);
            }
        });

        assertEquals("worldTopN", repo.lastMethod);
        assertEquals(3, repo.lastN);
    }

    // ---------------------------------------------------------------------
    // R13 – /api/cities/continents/{continent}/top?n=N
    // ---------------------------------------------------------------------

    @Test
    void continentTopNEndpointUsesPathAndQueryParam() {
        RecordingCityRepo repo = new RecordingCityRepo(List.of(SAMPLE_CITY));
        CityService service = new CityService(repo);
        CityRoutes routes = new CityRoutes(service);

        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);
        routes.register(app);

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/cities/continents/Asia/top?n=5")) {
                assertEquals(200, res.code());
                requireBody(res);
            }
        });

        assertEquals("continentTopN", repo.lastMethod);
        assertEquals("Asia", repo.lastContinent);
        assertEquals(5, repo.lastN);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static String requireBody(Response res) throws IOException {
        assertNotNull(res.body(), "HTTP response body should not be null");
        return res.body().string();
    }

    /**
     * Simple recording fake for {@link CityRepo}.
     * It returns a fixed list of cities and remembers what was called.
     */
    private static final class RecordingCityRepo implements CityRepo {

        private final List<City> result;

        String lastMethod;
        String lastContinent;
        String lastRegion;
        String lastCountry;
        String lastDistrict;
        Integer lastN;

        RecordingCityRepo(List<City> result) {
            this.result = result;
        }

        @Override
        public List<City> worldAll() {
            lastMethod = "worldAll";
            return result;
        }

        @Override
        public List<City> continentAll(String continent) {
            lastMethod = "continentAll";
            lastContinent = continent;
            return result;
        }

        @Override
        public List<City> regionAll(String region) {
            lastMethod = "regionAll";
            lastRegion = region;
            return result;
        }

        @Override
        public List<City> countryAll(String country) {
            lastMethod = "countryAll";
            lastCountry = country;
            return result;
        }

        @Override
        public List<City> districtAll(String district) {
            lastMethod = "districtAll";
            lastDistrict = district;
            return result;
        }

        @Override
        public List<City> worldTopN(int n) {
            lastMethod = "worldTopN";
            lastN = n;
            return result;
        }

        @Override
        public List<City> continentTopN(String continent, int n) {
            lastMethod = "continentTopN";
            lastContinent = continent;
            lastN = n;
            return result;
        }

        @Override
        public List<City> regionTopN(String region, int n) {
            lastMethod = "regionTopN";
            lastRegion = region;
            lastN = n;
            return result;
        }

        @Override
        public List<City> countryTopN(String country, int n) {
            lastMethod = "countryTopN";
            lastCountry = country;
            lastN = n;
            return result;
        }

        @Override
        public List<City> districtTopN(String district, int n) {
            lastMethod = "districtTopN";
            lastDistrict = district;
            lastN = n;
            return result;
        }
    }
}
