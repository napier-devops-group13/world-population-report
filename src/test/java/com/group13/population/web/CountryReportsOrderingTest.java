package com.group13.population.web;

import com.group13.population.db.Db;
import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests which verify that the HTTP endpoints for R01–R06
 * return country reports ordered by population DESC (largest to smallest)
 * using the real MySQL `world` database.
 */
public class CountryReportsOrderingTest {

    private static Db db;
    private static WorldRepo repo;
    private static CountryService service;
    private static CountryRoutes routes;

    @BeforeAll
    static void setUpDatabaseAndServices() {
        db = new Db();

        String host = getenvOrDefault("DB_HOST", "localhost");
        String port = getenvOrDefault("DB_PORT", "43306");
        String location = host + ":" + port;

        System.out.println("DEBUG: Connecting to DB at " + location);
        boolean connected = db.connect(location, 30_000);
        assertTrue(connected, "Failed to connect to database at " + location);

        repo = new WorldRepo(db);
        service = new CountryService(repo);
        routes = new CountryRoutes(service);
    }

    @AfterAll
    static void tearDownDatabase() {
        if (db != null) {
            db.disconnect();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Build a Javalin app wired with the real service/routes. */
    private static Javalin createApp() {
        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);
        routes.register(app);
        app.get("/health", ctx -> ctx.result("OK"));
        return app;
    }

    /**
     * Extract Population column from CSV output.
     *
     * This version:
     *  - Detects delimiter from the header ("," / ";" / tab)
     *  - Locates the "Population" header instead of hard-coding index 4
     *  - Correctly handles quoted fields which may contain commas
     */
    private static List<Long> extractPopulationsFromCsv(String csvBody) {
        List<Long> populations = new ArrayList<>();
        String[] lines = csvBody.split("\\R");
        if (lines.length <= 1) {
            // header only or empty
            return populations;
        }

        String header = lines[0].trim();
        if (header.isEmpty()) {
            return populations;
        }

        // Detect delimiter from header
        String delimiter;
        if (header.contains(",")) {
            delimiter = ",";
        } else if (header.contains(";")) {
            delimiter = ";";
        } else if (header.contains("\t")) {
            delimiter = "\t";
        } else {
            delimiter = ",";
        }

        String[] headerCols = header.split(Pattern.quote(delimiter));
        int populationIndex = -1;
        for (int i = 0; i < headerCols.length; i++) {
            String colName = headerCols[i].trim();
            if ("population".equalsIgnoreCase(colName)) {
                populationIndex = i;
                break;
            }
        }

        // Fallback if header name wasn't found – assume 5th column
        if (populationIndex == -1) {
            populationIndex = Math.min(4, headerCols.length - 1);
        }

        char delimChar = delimiter.charAt(0);

        // Parse each data row with a CSV-aware parser (handles quotes)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            List<String> cols = parseCsvLine(line, delimChar);
            if (cols.size() <= populationIndex) {
                continue;
            }

            String rawPop = cols.get(populationIndex).trim();
            if (rawPop.isEmpty()) {
                continue;
            }

            try {
                long pop = Long.parseLong(rawPop);
                populations.add(pop);
            } catch (NumberFormatException ex) {
                // If a row is malformed, just skip it – we only need enough
                // numeric rows to verify descending order.
                System.out.println(
                    "DEBUG: skipping row with non-numeric population '" + rawPop + "'"
                );
            }
        }

        return populations;
    }

    /**
     * Tiny CSV parser that respects quotes, so values like
     * "Congo, The Democratic Republic of the" don't break columns.
     */
    private static List<String> parseCsvLine(String line, char delimiter) {
        List<String> cols = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        int len = line.length();
        for (int i = 0; i < len; i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < len && line.charAt(i + 1) == '"') {
                    // Escaped quote ("")
                    current.append('"');
                    i++; // skip second quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == delimiter && !inQuotes) {
                cols.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        cols.add(current.toString());
        return cols;
    }

    /** Assert list is in non-increasing order. */
    private static void assertNonIncreasing(List<Long> values, String msgPrefix) {
        for (int i = 1; i < values.size(); i++) {
            long prev = values.get(i - 1);
            long curr = values.get(i);
            assertTrue(prev >= curr,
                msgPrefix + " – expected non-increasing order but found "
                    + prev + " then " + curr + " at index " + i);
        }
    }

    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    // -------------------------------------------------------------------------
    // Optional debug test (proves DB/service are returning rows)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DEBUG – service.getCountriesInWorldByPopulationDesc() has rows")
    void debugWorldServiceHasRows() {
        var rows = service.getCountriesInWorldByPopulationDesc();
        System.out.println("DEBUG: world rows size = " + rows.size());
        assertFalse(rows.isEmpty(), "Service world list should not be empty");
    }

    // -------------------------------------------------------------------------
    // R01 – R06 tests (DB + HTTP)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("R01 – /api/countries/world is ordered by population DESC (DB)")
    void worldReportOrderedByPopulation() throws Exception {
        Javalin app = createApp();

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/countries/world")) {
                assertEquals(200, res.code(), "Expected HTTP 200 for world report");

                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulationsFromCsv(body);

                assertFalse(pops.isEmpty(), "World report should not be empty");
                assertNonIncreasing(pops, "World report");
            }
        });
    }

    @Test
    @DisplayName("R02 – /api/countries/continent/Asia is ordered by population DESC (DB)")
    void continentReportOrderedByPopulation() throws Exception {
        Javalin app = createApp();

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/countries/continent/Asia")) {
                assertEquals(200, res.code(), "Expected HTTP 200 for continent report");

                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulationsFromCsv(body);

                assertFalse(pops.isEmpty(), "Asia report should not be empty");
                assertNonIncreasing(pops, "Continent(Asia) report");
            }
        });
    }

    @Test
    @DisplayName("R03 – /api/countries/region/Western Europe is ordered by population DESC (DB)")
    void regionReportOrderedByPopulation() throws Exception {
        Javalin app = createApp();

        // URL-encode space in path parameter
        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/countries/region/Western%20Europe")) {
                assertEquals(200, res.code(), "Expected HTTP 200 for region report");

                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulationsFromCsv(body);

                assertFalse(pops.isEmpty(), "Western Europe report should not be empty");
                assertNonIncreasing(pops, "Region(Western Europe) report");
            }
        });
    }

    @Test
    @DisplayName("R04 – /api/countries/world/top?n=10 returns ≤10 rows ordered by population DESC (DB)")
    void topWorldReportOrderedAndLimited() throws Exception {
        Javalin app = createApp();

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/countries/world/top?n=10")) {
                assertEquals(200, res.code(), "Expected HTTP 200 for top world report");

                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulationsFromCsv(body);

                assertFalse(pops.isEmpty(), "Top world report should not be empty");
                assertTrue(pops.size() <= 10,
                    "Expected at most 10 rows but got " + pops.size());
                assertNonIncreasing(pops, "Top world report");
            }
        });
    }

    @Test
    @DisplayName("R05 – /api/countries/continent/Europe/top?n=5 returns ≤5 rows ordered by population DESC (DB)")
    void topContinentReportOrderedAndLimited() throws Exception {
        Javalin app = createApp();

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/countries/continent/Europe/top?n=5")) {
                assertEquals(200, res.code(), "Expected HTTP 200 for top continent report");

                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulationsFromCsv(body);

                assertFalse(pops.isEmpty(), "Top Europe report should not be empty");
                assertTrue(pops.size() <= 5,
                    "Expected at most 5 rows but got " + pops.size());
                assertNonIncreasing(pops, "Top continent(Europe) report");
            }
        });
    }

    @Test
    @DisplayName("R06 – /api/countries/region/Caribbean/top?n=3 returns ≤3 rows ordered by population DESC (DB)")
    void topRegionReportOrderedAndLimited() throws Exception {
        Javalin app = createApp();

        JavalinTest.test(app, (server, client) -> {
            try (Response res = client.get("/api/countries/region/Caribbean/top?n=3")) {
                assertEquals(200, res.code(), "Expected HTTP 200 for top region report");

                String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
                List<Long> pops = extractPopulationsFromCsv(body);

                assertFalse(pops.isEmpty(), "Top Caribbean report should not be empty");
                assertTrue(pops.size() <= 3,
                    "Expected at most 3 rows but got " + pops.size());
                assertNonIncreasing(pops, "Top region(Caribbean) report");
            }
        });
    }
}
