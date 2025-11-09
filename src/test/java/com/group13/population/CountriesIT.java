package com.group13.population;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/** End-to-end tests against seeded DB for R01–R06. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CountriesIT {

    static Javalin app;
    static int port;
    static final HttpClient HTTP = HttpClient.newHttpClient();
    static final ObjectMapper M = new ObjectMapper();

    @BeforeAll
    static void start() {
        app = App.create();       // make sure App.create() calls db.awaitReady(30000, 500)
        app.start(0);             // pick a free ephemeral port
        port = app.port();
    }

    @AfterAll
    static void stop() {
        if (app != null) app.stop();
    }

    // ---------- helpers ----------

    /** Encode spaces in path segments (e.g., "Southeast Asia" -> "Southeast%20Asia"). */
    private static String encodePath(String p) {
        return p.replace(" ", "%20");
    }

    /** GET path and fail if status >= expectStatus (we use 400). */
    private JsonNode getJson(String path, int expectStatus) throws Exception {
        var uri = URI.create("http://localhost:" + port + encodePath(path));
        var req = HttpRequest.newBuilder(uri).GET().build();
        var res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(res.statusCode() < expectStatus, "HTTP status ==> expected: <" + expectStatus + "> but was: <" + res.statusCode() + ">");
        return M.readTree(res.body());
    }

    private static boolean isSortedByNameAsc(List<JsonNode> rows) {
        Collator collator = Collator.getInstance(Locale.ENGLISH);
        for (int i = 1; i < rows.size(); i++) {
            String prev = rows.get(i - 1).get("name").asText();
            String curr = rows.get(i).get("name").asText();
            if (collator.compare(prev, curr) > 0) return false;
        }
        return true;
    }

    private static void assertTopNDescPopThenNameAsc(List<JsonNode> rows) {
        Collator collator = Collator.getInstance(Locale.ENGLISH);
        for (int i = 1; i < rows.size(); i++) {
            long p1 = rows.get(i - 1).get("population").asLong();
            long p2 = rows.get(i).get("population").asLong();
            String n1 = rows.get(i - 1).get("name").asText();
            String n2 = rows.get(i).get("name").asText();
            if (p1 < p2) fail("Population must be DESC");
            if (p1 == p2 && collator.compare(n1, n2) > 0) fail("Name must be ASC for ties");
        }
    }

    // ---------- R01–R06 ----------

    @Test @Order(1)
    void r01_world_all_sorted_and_has_fields() throws Exception {
        JsonNode json = getJson("/countries/world", 400);
        assertTrue(json.isArray() && json.size() > 0, "Should return rows");

        List<JsonNode> rows = new ArrayList<>();
        json.forEach(rows::add);

        // fields present
        JsonNode first = rows.get(0);
        assertNotNull(first.get("code"));
        assertNotNull(first.get("name"));
        assertNotNull(first.get("continent"));
        assertNotNull(first.get("region"));
        assertNotNull(first.get("population"));
        assertTrue(first.has("capital")); // may be null, but key must exist

        // sorted by name ASC
        assertTrue(isSortedByNameAsc(rows), "List must be sorted by Name ASC");
    }

    @Test @Order(2)
    void r02_continent_all_validations_and_sort() throws Exception {
        JsonNode json = getJson("/countries/continent/Asia", 400);
        List<JsonNode> rows = new ArrayList<>();
        json.forEach(rows::add);
        assertTrue(rows.size() > 0);
        assertTrue(isSortedByNameAsc(rows));
        // all rows must be Asia
        assertTrue(rows.stream().allMatch(n -> "Asia".equals(n.get("continent").asText())));
    }

    @Test @Order(3)
    void r03_region_all_validations_and_sort() throws Exception {
        JsonNode json = getJson("/countries/region/Southeast Asia", 400);
        List<JsonNode> rows = new ArrayList<>();
        json.forEach(rows::add);
        assertTrue(rows.size() > 0);
        assertTrue(isSortedByNameAsc(rows));
        assertTrue(rows.stream().allMatch(n -> "Southeast Asia".equals(n.get("region").asText())));
    }

    @Test @Order(4)
    void r04_topn_world_exact_size_and_sort() throws Exception {
        int n = 5;
        JsonNode json = getJson("/countries/world/top/" + n, 400);
        List<JsonNode> rows = new ArrayList<>();
        json.forEach(rows::add);
        assertEquals(n, rows.size(), "Should return exactly " + n);
        assertTopNDescPopThenNameAsc(rows);
    }

    @Test @Order(5)
    void r05_topn_continent_validations() throws Exception {
        int n = 7;
        JsonNode json = getJson("/countries/continent/Europe/top/" + n, 400);
        List<JsonNode> rows = new ArrayList<>();
        json.forEach(rows::add);
        assertEquals(n, rows.size());
        assertTrue(rows.stream().allMatch(r -> "Europe".equals(r.get("continent").asText())));
        assertTopNDescPopThenNameAsc(rows);
    }

    @Test @Order(6)
    void r06_topn_region_validations() throws Exception {
        int n = 4;
        JsonNode json = getJson("/countries/region/Eastern Asia/top/" + n, 400);
        List<JsonNode> rows = new ArrayList<>();
        json.forEach(rows::add);
        assertEquals(n, rows.size());
        assertTrue(rows.stream().allMatch(r -> "Eastern Asia".equals(r.get("region").asText())));
        assertTopNDescPopThenNameAsc(rows);
    }
}
