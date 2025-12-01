package com.group13.population.web;

import com.group13.population.App;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP smoke tests for {@link CityRoutes}.
 *
 * <p>These tests start the full application via {@link App#createApp()},
 * then hit every city-report endpoint (R07–R16) once. This executes
 * the route handlers end-to-end (Javalin → CityRoutes → CityService),
 * which gives JaCoCo solid coverage on the web layer for cities.</p>
 *
 * <p>IMPORTANT: The paths below assume your CityRoutes.register(...) uses
 * the following pattern:
 *
 *   /reports/cities/world
 *   /reports/cities/continent/{continent}
 *   /reports/cities/region/{region}
 *   /reports/cities/country/{country}
 *   /reports/cities/district/{district}
 *   /reports/cities/world/top/{n}
 *   /reports/cities/continent/{continent}/top/{n}
 *   /reports/cities/region/{region}/top/{n}
 *   /reports/cities/country/{country}/top/{n}
 *   /reports/cities/district/{district}/top/{n}
 *
 * If your routes use different paths, either update CityRoutes to match
 * this scheme (recommended for the coursework) or adjust the constants
 * below accordingly.
 * </p>
 */
@DisplayName("CityRoutes – HTTP smoke tests for R07–R16")
class CityRoutesTest {

    // ------------------------------------------------------------------
    // Expected HTTP paths for city reports
    // ------------------------------------------------------------------

    private static final String BASE = "/reports/cities";

    private static final String PATH_R07 = BASE + "/world";
    private static final String PATH_R08 = BASE + "/continent/Europe";
    private static final String PATH_R09 = BASE + "/region/Caribbean";
    private static final String PATH_R10 = BASE + "/country/Japan";
    private static final String PATH_R11 = BASE + "/district/Tokyo";
    private static final String PATH_R12 = BASE + "/world/top/10";
    private static final String PATH_R13 = BASE + "/continent/Europe/top/5";
    private static final String PATH_R14 = BASE + "/region/Caribbean/top/5";
    private static final String PATH_R15 = BASE + "/country/Japan/top/5";
    private static final String PATH_R16 = BASE + "/district/Tokyo/top/5";

    private static Javalin app;
    private static HttpClient client;
    private static String baseUrl;

    // ------------------------------------------------------------------
    // Lifecycle – start and stop the real app once for this test class
    // ------------------------------------------------------------------

    @BeforeAll
    static void setUpAll() {
        // Build the full app (Db, repos, services, routes) but don’t
        // care which port – let Javalin choose a free one.
        app = App.createApp();
        app.start(0);

        client = HttpClient.newHttpClient();
        baseUrl = "http://localhost:" + app.port();
    }

    @AfterAll
    static void tearDownAll() {
        if (app != null) {
            app.stop();
        }
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private void assertOkWithBody(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
            .GET()
            .build();

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
            "Expected HTTP 200 from " + path);

        String body = response.body();
        assertNotNull(body, "Body should not be null for " + path);
        assertFalse(body.isBlank(), "Body should not be blank for " + path);
    }

    // ------------------------------------------------------------------
    // One HTTP smoke test per requirement (R07–R16)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("R07 – world cities endpoint responds with 200 and body")
    void r07_worldCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R07);
    }

    @Test
    @DisplayName("R08 – continent cities endpoint responds with 200 and body")
    void r08_continentCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R08);
    }

    @Test
    @DisplayName("R09 – region cities endpoint responds with 200 and body")
    void r09_regionCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R09);
    }

    @Test
    @DisplayName("R10 – country cities endpoint responds with 200 and body")
    void r10_countryCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R10);
    }

    @Test
    @DisplayName("R11 – district cities endpoint responds with 200 and body")
    void r11_districtCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R11);
    }

    @Test
    @DisplayName("R12 – top-N world cities endpoint responds with 200 and body")
    void r12_topWorldCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R12);
    }

    @Test
    @DisplayName("R13 – top-N continent cities endpoint responds with 200 and body")
    void r13_topContinentCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R13);
    }

    @Test
    @DisplayName("R14 – top-N region cities endpoint responds with 200 and body")
    void r14_topRegionCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R14);
    }

    @Test
    @DisplayName("R15 – top-N country cities endpoint responds with 200 and body")
    void r15_topCountryCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R15);
    }

    @Test
    @DisplayName("R16 – top-N district cities endpoint responds with 200 and body")
    void r16_topDistrictCitiesEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R16);
    }
}
