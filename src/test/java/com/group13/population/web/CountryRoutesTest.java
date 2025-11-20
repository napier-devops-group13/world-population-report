package com.group13.population.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group13.population.App;
import io.javalin.Javalin;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Route tests for country reports (R01–R06).
 * These hit the real HTTP endpoints using Javalin TestTools and
 * verify the CSV responses are ordered by population DESC.
 */
class CountryRoutesTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Extract population values from a CSV body.
     * Uses the header row to locate the "population" column so the test
     * still works if the column order changes.
     */
    private List<Long> extractPopulations(String csvBody) {
        List<Long> populations = new ArrayList<>();

        if (csvBody == null || csvBody.isBlank()) {
            return populations;
        }

        String[] lines = csvBody.split("\\R");
        if (lines.length < 2) {
            return populations; // header only or empty
        }

        // --- find index of the population column from header ---
        String header = lines[0].trim();
        String[] headerCols = header.split(",");
        int popIndex = -1;
        for (int i = 0; i < headerCols.length; i++) {
            String colName = headerCols[i].trim().toLowerCase();
            if (colName.contains("population")) {   // e.g. "Population"
                popIndex = i;
                break;
            }
        }
        // fallback to 5th column if header is unexpected
        if (popIndex == -1) {
            popIndex = 4;
        }

        // --- read populations from data rows ---
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] cols = line.split(",");
            if (cols.length <= popIndex) {
                continue;
            }

            String raw = cols[popIndex].trim();
            if (raw.isEmpty()) {
                continue;
            }

            // remove thousands separators if present, e.g. "12,878,000"
            String numeric = raw.replace(",", "");
            long pop = Long.parseLong(numeric);
            populations.add(pop);
        }

        return populations;
    }


    private boolean isDescending(List<Long> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > values.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // R01 – R06 tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("R01 – /api/countries/world is ordered by population DESC")
    void r01_worldOrderedByPopulationDesc() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/world")) {
                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulations(body);
                assertTrue(isDescending(pops),
                    "World country report should be ordered by population DESC");
            }
        });
    }

    @Test
    @DisplayName("R02 – /api/countries/continent/Asia is ordered by population DESC")
    void r02_continentAsiaOrderedByPopulationDesc() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/continent/Asia")) {
                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulations(body);
                assertTrue(isDescending(pops),
                    "Asia report should be ordered by population DESC");
            }
        });
    }

    @Test
    @DisplayName("R03 – /api/countries/region/Western Europe is ordered by population DESC")
    void r03_regionWesternEuropeOrderedByPopulationDesc() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/region/Western Europe")) {
                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulations(body);
                assertTrue(isDescending(pops),
                    "Western Europe report should be ordered by population DESC");
            }
        });
    }

    @Test
    @DisplayName("R04 – /api/countries/world/top?n=10 returns ≤10 rows ordered by population DESC")
    void r04_worldTop10OrderedByPopulationDesc() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/world/top?n=10")) {
                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulations(body);
                assertTrue(pops.size() <= 10,
                    "World top 10 should return at most 10 rows");
                assertTrue(isDescending(pops),
                    "World top 10 should be ordered by population DESC");
            }
        });
    }

    @Test
    @DisplayName("R05 – /api/countries/continent/Europe/top?n=5 returns ≤5 rows ordered by population DESC")
    void r05_continentEuropeTop5OrderedByPopulationDesc() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/continent/Europe/top?n=5")) {
                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulations(body);
                assertTrue(pops.size() <= 5,
                    "Europe top 5 should return at most 5 rows");
                assertTrue(isDescending(pops),
                    "Europe top 5 should be ordered by population DESC");
            }
        });
    }

    @Test
    @DisplayName("R06 – /api/countries/region/Caribbean/top?n=3 returns ≤3 rows ordered by population DESC")
    void r06_regionCaribbeanTop3OrderedByPopulationDesc() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/region/Caribbean/top?n=3")) {
                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulations(body);
                assertTrue(pops.size() <= 3,
                    "Caribbean top 3 should return at most 3 rows");
                assertTrue(isDescending(pops),
                    "Caribbean top 3 should be ordered by population DESC");
            }
        });
    }

    // ------------------------------------------------------------------
    // Extra edge–case tests (boost web coverage)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/countries/world/top?n=abc returns 400 for non-numeric n")
    void worldTopWithNonNumericNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/world/top?n=abc")) {
                assertEquals(400, res.code(),
                    "Non-numeric n should return HTTP 400");
            }
        });
    }

    @Test
    @DisplayName("GET /api/countries/world/top?n=0 returns 400 for non-positive n")
    void worldTopWithNonPositiveNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/world/top?n=0")) {
                assertEquals(400, res.code(),
                    "Zero or negative n should return HTTP 400");
            }
        });
    }
}
