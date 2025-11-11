package com.group13.population;

import com.group13.population.repo.CountryReport;
import com.group13.population.repo.CountryRepository;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Route-level tests for App.
 *
 * Uses a small in-memory fake CountryRepository so:
 *  - no real database or Docker is required
 *  - we can assert which repo methods were called
 *  - JaCoCo marks App + route lambdas as covered, boosting Codecov.
 */
class AppRoutesTest {

    private Javalin app;
    private int port;
    private HttpClient client;
    private FakeCountryRepo repo;

    @BeforeEach
    void startServer() throws Exception {
        // In-memory fake repo – no DB calls
        repo = new FakeCountryRepo();

        // Minimal Javalin config (mirrors App.create())
        app = Javalin.create(cfg -> {
            cfg.showJavalinBanner = false;
            cfg.http.defaultContentType = "application/json";
        });

        // Same error mapping as App.create()
        app.exception(IllegalArgumentException.class, (e, ctx) ->
            ctx.status(400)
                .json(Map.of("error", e.getMessage()))
        );

        app.exception(Exception.class, (e, ctx) ->
            ctx.status(500)
                .json(Map.of("error", "internal server error"))
        );

        // Register real routes from App using our fake repo
        App.registerRoutes(app, repo, "");

        // Start on a random free port
        app.start(0);
        port = app.port();

        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServer() {
        if (app != null) {
            app.stop();
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .GET()
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void readPortDefaultsTo7070WhenEnvMissing() throws Exception {
        // Call private static readPort() via reflection
        Method m = App.class.getDeclaredMethod("readPort");
        m.setAccessible(true);
        int value = (int) m.invoke(null);
        assertEquals(7070, value);
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        HttpResponse<String> res = get("/health");
        assertEquals(200, res.statusCode());
        assertEquals("ok", res.body());
    }

    @Test
    void worldCountriesUsesDefaultSortByName() throws Exception {
        HttpResponse<String> res = get("/countries/world");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().startsWith("[")); // JSON array
        assertTrue(repo.worldByNameCalled);
        assertFalse(repo.worldByPopCalled);
    }

    @Test
    void worldCountriesCanSortByPopulation() throws Exception {
        HttpResponse<String> res = get("/countries/world?sort=pop");
        assertEquals(200, res.statusCode());
        assertTrue(repo.worldByPopCalled);
        assertFalse(repo.worldByNameCalled);
    }

    @Test
    void continentRoutesWorkWithAndWithoutSort() throws Exception {
        HttpResponse<String> res1 = get("/countries/continent/Europe");
        assertEquals(200, res1.statusCode());
        assertEquals("Europe", repo.continentByName);
        assertNull(repo.continentByPop);

        HttpResponse<String> res2 = get("/countries/continent/Europe?sort=pop");
        assertEquals(200, res2.statusCode());
        assertEquals("Europe", repo.continentByPop);
    }

    @Test
    void regionRoutesWorkWithAndWithoutSort() throws Exception {
        HttpResponse<String> res1 = get("/countries/region/Asia");
        assertEquals(200, res1.statusCode());
        assertEquals("Asia", repo.regionByName);
        assertNull(repo.regionByPop);

        HttpResponse<String> res2 = get("/countries/region/Asia?sort=pop");
        assertEquals(200, res2.statusCode());
        assertEquals("Asia", repo.regionByPop);
    }

    @Test
    void topWorldCountriesRouteUsesRepo() throws Exception {
        HttpResponse<String> res = get("/countries/world/top/5");
        assertEquals(200, res.statusCode());
        assertEquals(Integer.valueOf(5), repo.worldTopN);
    }

    @Test
    void topContinentCountriesRouteUsesRepo() throws Exception {
        HttpResponse<String> res =
            get("/countries/continent/Africa/top/3");
        assertEquals(200, res.statusCode());
        assertEquals("Africa", repo.continentTopName);
        assertEquals(Integer.valueOf(3), repo.continentTopN);
    }

    @Test
    void topRegionCountriesRouteUsesRepo() throws Exception {
        HttpResponse<String> res =
            get("/countries/region/Caribbean/top/2");
        assertEquals(200, res.statusCode());
        assertEquals("Caribbean", repo.regionTopName);
        assertEquals(Integer.valueOf(2), repo.regionTopN);
    }

    @Test
    void topRegionRejectsZeroOrNegativeN() throws Exception {
        HttpResponse<String> res =
            get("/countries/region/Europe/top/0");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("n must be > 0"));
    }

    @Test
    void topRegionRejectsNonNumericN() throws Exception {
        HttpResponse<String> res =
            get("/countries/region/Europe/top/bad");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("n must be a positive integer"));
    }

    @Test
    void unexpectedErrorIsMappedToInternalServerError() throws Exception {
        repo.topWorldShouldThrow = true;

        HttpResponse<String> res =
            get("/countries/world/top/99");

        assertEquals(500, res.statusCode());
        assertTrue(res.body().contains("internal server error"));
    }

    // ---------------------------------------------------------------------
    // Fake repository used ONLY for these tests
    // ---------------------------------------------------------------------

    /**
     * Simple in-memory fake that records which methods were called.
     * Implements CountryRepository so it fits App.registerRoutes, but
     * never touches a real database.
     */
    private static final class FakeCountryRepo implements CountryRepository {

        boolean worldByNameCalled;
        boolean worldByPopCalled;

        String continentByName;
        String continentByPop;

        String regionByName;
        String regionByPop;

        Integer worldTopN;
        String continentTopName;
        Integer continentTopN;
        String regionTopName;
        Integer regionTopN;

        boolean topWorldShouldThrow;

        @Override
        public List<CountryReport> countriesWorld() throws SQLException {
            worldByNameCalled = true;
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> countriesWorldByPopulation()
            throws SQLException {
            worldByPopCalled = true;
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> countriesByContinent(String continent)
            throws SQLException {
            continentByName = continent;
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> countriesByContinentByPopulation(
            String continent) throws SQLException {
            continentByPop = continent;
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> countriesByRegion(String region)
            throws SQLException {
            regionByName = region;
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> countriesByRegionByPopulation(
            String region) throws SQLException {
            regionByPop = region;
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> topCountriesWorld(int n)
            throws SQLException {
            worldTopN = n;
            if (topWorldShouldThrow && n == 99) {
                // simulate unexpected failure -> mapped to HTTP 500
                throw new RuntimeException("boom");
            }
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> topCountriesByContinent(
            String continent, int n) throws SQLException {
            continentTopName = continent;
            continentTopN = n;
            return Collections.emptyList();
        }

        @Override
        public List<CountryReport> topCountriesByRegion(
            String region, int n) throws SQLException {
            regionTopName = region;
            regionTopN = n;
            return Collections.emptyList();
        }
    }
}
