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
 * HTTP smoke tests for {@link CapitalRoutes}.
 *
 * <p>Like {@link CityRoutesTest}, this starts the full application via
 * {@link App#createApp()} and exercises the capital-city report endpoints
 * (R17–R22) over HTTP. That drives CapitalRoutes + CapitalService and
 * gives JaCoCo strong coverage for the capital web layer.</p>
 *
 * <p>Expected path pattern in CapitalRoutes.register(...):</p>
 *
 *   /reports/capitals/world
 *   /reports/capitals/continent/{continent}
 *   /reports/capitals/region/{region}
 *   /reports/capitals/world/top/{n}
 *   /reports/capitals/continent/{continent}/top/{n}
 *   /reports/capitals/region/{region}/top/{n}
 *
 * <p>If your actual paths differ, either align CapitalRoutes with this
 * scheme (recommended for clarity + coursework) or update the constants
 * below.</p>
 */
@DisplayName("CapitalRoutes – HTTP smoke tests for R17–R22")
class CapitalRoutesTest {

    // ------------------------------------------------------------------
    // Expected HTTP paths for capital reports
    // ------------------------------------------------------------------

    private static final String BASE = "/reports/capitals";

    private static final String PATH_R17 = BASE + "/world";
    private static final String PATH_R18 = BASE + "/continent/Europe";
    private static final String PATH_R19 = BASE + "/region/Caribbean";
    private static final String PATH_R20 = BASE + "/world/top/10";
    private static final String PATH_R21 = BASE + "/continent/Europe/top/5";
    private static final String PATH_R22 = BASE + "/region/Caribbean/top/5";

    private static Javalin app;
    private static HttpClient client;
    private static String baseUrl;

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @BeforeAll
    static void setUpAll() {
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
    // One HTTP smoke test per requirement (R17–R22)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("R17 – world capitals endpoint responds with 200 and body")
    void r17_worldCapitalsEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R17);
    }

    @Test
    @DisplayName("R18 – continent capitals endpoint responds with 200 and body")
    void r18_continentCapitalsEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R18);
    }

    @Test
    @DisplayName("R19 – region capitals endpoint responds with 200 and body")
    void r19_regionCapitalsEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R19);
    }

    @Test
    @DisplayName("R20 – top-N world capitals endpoint responds with 200 and body")
    void r20_topWorldCapitalsEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R20);
    }

    @Test
    @DisplayName("R21 – top-N continent capitals endpoint responds with 200 and body")
    void r21_topContinentCapitalsEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R21);
    }

    @Test
    @DisplayName("R22 – top-N region capitals endpoint responds with 200 and body")
    void r22_topRegionCapitalsEndpointResponds() throws Exception {
        assertOkWithBody(PATH_R22);
    }

    // ------------------------------------------------------------------
    // Unit tests for helper methods (parseLimit & escape)
    // – these exercise all branches inside CapitalRoutes.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("parseLimit – positive integer keeps the value")
    void parseLimitPositiveKeepsValue() {
        assertEquals(7, CapitalRoutes.parseLimit("7", 10));
    }

    @Test
    @DisplayName("parseLimit – zero, negative and non-numeric fall back to default")
    void parseLimitInvalidFallsBackToDefault() {
        assertEquals(10, CapitalRoutes.parseLimit("0", 10));
        assertEquals(10, CapitalRoutes.parseLimit("-5", 10));
        assertEquals(10, CapitalRoutes.parseLimit("abc", 10));
    }

    @Test
    @DisplayName("parseLimit – null or blank uses default")
    void parseLimitNullOrBlankUsesDefault() {
        assertEquals(5, CapitalRoutes.parseLimit(null, 5));
        assertEquals(5, CapitalRoutes.parseLimit("   ", 5));
    }

    @Test
    @DisplayName("escape – null, plain and comma/quote values handled correctly")
    void escapeCoversAllBranches() {
        // null -> empty
        assertEquals("", CapitalRoutes.escape(null));

        // plain text -> unchanged
        assertEquals("Simple", CapitalRoutes.escape("Simple"));

        // value with comma and quotes -> wrapped + quotes doubled
        assertEquals("\"A,B\"\"C\"",
            CapitalRoutes.escape("A,B\"C"));
    }
}
