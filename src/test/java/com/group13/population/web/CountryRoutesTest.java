package com.group13.population.web;

import com.group13.population.App;
import com.group13.population.model.CountryRow;
import io.javalin.Javalin;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Route tests for country reports (R01–R06).
 * These hit the real HTTP endpoints using Javalin TestTools and
 * verify the CSV responses are ordered by population DESC, the
 * n-parameter validation works, and the CSV helpers (escape and
 * writeCountriesCsv) are exercised for high coverage.
 */
class CountryRoutesTest {

    // ------------------------------------------------------------------
    // Small CSV / assertion helpers
    // ------------------------------------------------------------------

    /**
     * Split a single CSV line into columns, respecting quotes and
     * doubled quotes ("") inside a field.
     */
    private List<String> splitCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Handle doubled quotes inside a quoted field: ""
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // skip second quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        cols.add(current.toString());
        return cols;
    }

    /**
     * Extract population values (5th column) from a CSV body.
     * Assumes header row then data rows.
     */
    private List<Long> extractPopulations(String csvBody) {
        List<Long> populations = new ArrayList<>();
        String[] lines = csvBody.split("\\R");
        // skip header
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            List<String> cols = splitCsvLine(line);
            if (cols.size() < 5) {
                continue;
            }

            String popText = cols.get(4).trim();
            if (popText.isEmpty()) {
                continue;
            }

            long pop = Long.parseLong(popText);
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

    /**
     * Invoke the private static escape(String) method in CountryRoutes
     * so we can cover all its branches (null/plain/quoted/etc).
     */
    private String invokeEscape(String value) throws Exception {
        Method m = CountryRoutes.class.getDeclaredMethod("escape", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, value);
    }

    /**
     * Invoke the private static writeCountriesCsv(Context, List) helper
     * using a real Javalin Context (via JavalinTest) so JaCoCo sees the
     * method executed for both null and non-empty lists.
     */
    private String invokeWriteCountriesCsv(List<CountryRow> rows) throws Exception {
        Method m = CountryRoutes.class.getDeclaredMethod(
            "writeCountriesCsv",
            io.javalin.http.Context.class,
            List.class
        );
        m.setAccessible(true);

        final String[] captured = new String[1];

        Javalin app = Javalin.create();
        app.get("/__test/countries/csv", ctx -> m.invoke(null, ctx, rows));

        JavalinTest.test(app, (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/__test/countries/csv")) {
                captured[0] = new String(res.body().bytes(), StandardCharsets.UTF_8);
            }
        });

        return captured[0];
    }

    // ------------------------------------------------------------------
    // R01 – R06 behaviour tests via real HTTP endpoints
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
            try (Response res =
                     client.get("/api/countries/continent/Europe/top?n=5")) {
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
            try (Response res =
                     client.get("/api/countries/region/Caribbean/top?n=3")) {
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
    // Extra behaviour test to hit the "empty list" branch in writeCountriesCsv
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Unknown continent is handled gracefully (empty result branch)")
    void unknownContinentUsesEmptyResultBranch() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res =
                     client.get("/api/countries/continent/NoSuchContinentXYZ")) {

                int status = res.code();
                assertTrue(status == 200 || status == 404,
                    "Unknown continent should be handled with 200/404, got " + status);

                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                assertFalse(body.isEmpty(), "Response body should not be empty");
            }
        });
    }

    // ------------------------------------------------------------------
    // Validation tests – n parameter for world / continent / region
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

    @Test
    @DisplayName("GET /api/countries/world/top (missing n) returns 400")
    void worldTopWithMissingNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res = client.get("/api/countries/world/top")) {
                assertEquals(400, res.code(),
                    "Missing n should return HTTP 400");
            }
        });
    }

    @Test
    @DisplayName("GET /api/countries/continent/Europe/top?n=abc returns 400")
    void continentTopWithNonNumericNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res =
                     client.get("/api/countries/continent/Europe/top?n=abc")) {
                assertEquals(400, res.code(),
                    "Non-numeric n should return HTTP 400 for continent top");
            }
        });
    }

    @Test
    @DisplayName("GET /api/countries/continent/Europe/top?n=0 returns 400")
    void continentTopWithNonPositiveNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res =
                     client.get("/api/countries/continent/Europe/top?n=0")) {
                assertEquals(400, res.code(),
                    "Zero or negative n should return HTTP 400 for continent top");
            }
        });
    }

    @Test
    @DisplayName("GET /api/countries/continent/Europe/top (missing n) returns 400")
    void continentTopWithMissingNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res =
                     client.get("/api/countries/continent/Europe/top")) {
                assertEquals(400, res.code(),
                    "Missing n should return HTTP 400 for continent top");
            }
        });
    }

    @Test
    @DisplayName("GET /api/countries/region/Caribbean/top?n=abc returns 400")
    void regionTopWithNonNumericNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res =
                     client.get("/api/countries/region/Caribbean/top?n=abc")) {
                assertEquals(400, res.code(),
                    "Non-numeric n should return HTTP 400 for region top");
            }
        });
    }

    @Test
    @DisplayName("GET /api/countries/region/Caribbean/top?n=0 returns 400")
    void regionTopWithNonPositiveNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res =
                     client.get("/api/countries/region/Caribbean/top?n=0")) {
                assertEquals(400, res.code(),
                    "Zero or negative n should return HTTP 400 for region top");
            }
        });
    }

    @Test
    @DisplayName("GET /api/countries/region/Caribbean/top (missing n) returns 400")
    void regionTopWithMissingNReturns400() throws Exception {
        JavalinTest.test(App.createApp(), (Javalin server, HttpClient client) -> {
            try (Response res =
                     client.get("/api/countries/region/Caribbean/top")) {
                assertEquals(400, res.code(),
                    "Missing n should return HTTP 400 for region top");
            }
        });
    }

    // ------------------------------------------------------------------
    // Direct tests for escape(..) to hit all its branches
    // ------------------------------------------------------------------

    @Test
    @DisplayName("escape(null) returns empty string")
    void escapeNullReturnsEmptyString() throws Exception {
        assertEquals("", invokeEscape(null));
    }

    @Test
    @DisplayName("escape(\"\") returns empty string")
    void escapeEmptyStringReturnsEmptyString() throws Exception {
        assertEquals("", invokeEscape(""));
    }

    @Test
    @DisplayName("escape plain value returns unchanged text (no quotes)")
    void escapePlainReturnsUnquoted() throws Exception {
        assertEquals("Plain", invokeEscape("Plain"));
    }

    @Test
    @DisplayName("escape value with comma and quotes wraps and doubles quotes")
    void escapeCommaAndQuotesGetsQuoted() throws Exception {
        String out = invokeEscape("A,B\"C");
        assertTrue(out.startsWith("\""));
        assertTrue(out.endsWith("\""));
        assertTrue(out.contains("A,B"));
        assertTrue(out.contains("\"\"C"));
    }

    // ------------------------------------------------------------------
    // Direct tests for writeCountriesCsv(..) null + non-empty branches
    // ------------------------------------------------------------------

    @Test
    @DisplayName("writeCountriesCsv(null) still returns a single header row")
    void writeCountriesCsvNullListProducesHeaderOnly() throws Exception {
        String csv = invokeWriteCountriesCsv(null);
        assertNotNull(csv);

        String[] lines = csv.split("\\R");
        int nonBlank = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                nonBlank++;
            }
        }

        assertEquals(1, nonBlank,
            "Expected exactly one non-blank CSV line (header only) when list is null");
    }

    @Test
    @DisplayName("writeCountriesCsv(non-empty) runs without throwing and produces some CSV")
    void writeCountriesCsvSingleRowRunsWithoutError() throws Exception {
        CountryRow demo = new CountryRow(
            "XX",
            "Testland",
            "TestContinent",
            "TestRegion",
            4242L,
            "TestCapital"
        );

        String csv = invokeWriteCountriesCsv(List.of(demo));

        assertNotNull(csv, "CSV string should not be null for non-empty list");
        assertFalse(csv.trim().isEmpty(), "CSV should not be empty for non-empty list");
    }
}
