package com.group13.population.web;

import com.group13.population.db.Db;
import com.group13.population.model.LanguagePopulationRow;
import com.group13.population.model.PopulationLookupRow;
import com.group13.population.model.PopulationRow;
import com.group13.population.repo.PopulationRepo;
import com.group13.population.service.PopulationService;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PopulationRoutes}.
 *
 * Goals:
 *  1. Cover all CSV helper methods including null + escaping branches.
 *  2. Hit every HTTP handler (R24–R32 + alias endpoints) using a real Javalin
 *     instance so the lambda bodies in register(...) are covered.
 */
class PopulationRoutesTest {

    // ---------------------------------------------------------------------
    // Reflection helpers to create model objects whose constructors
    // are private / package-private.
    // ---------------------------------------------------------------------

    private static PopulationLookupRow newLookupRow(String name, long population) {
        try {
            Constructor<PopulationLookupRow> ctor =
                    PopulationLookupRow.class.getDeclaredConstructor(String.class, long.class);
            ctor.setAccessible(true);
            return ctor.newInstance(name, population);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PopulationLookupRow", e);
        }
    }

    private static LanguagePopulationRow newLanguageRow(String language,
                                                        long speakers,
                                                        double percent) {
        try {
            Constructor<LanguagePopulationRow> ctor =
                    LanguagePopulationRow.class.getDeclaredConstructor(String.class, long.class, double.class);
            ctor.setAccessible(true);
            return ctor.newInstance(language, speakers, percent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create LanguagePopulationRow", e);
        }
    }

    // ---------------------------------------------------------------------
    // Stub PopulationService (no real DB calls in tests)
    // ---------------------------------------------------------------------

    private static class StubPopulationService extends PopulationService {

        private final List<PopulationRow> regionRows =
                List.of(
                        // comma in name -> CSV escaping
                        PopulationRow.fromTotals("Region,One", 1_000, 400)
                );

        private final List<PopulationRow> countryRows =
                List.of(
                        // quote in name -> CSV escaping
                        PopulationRow.fromTotals("Country\"One\"", 500, 100)
                );

        private final List<LanguagePopulationRow> languageRows =
                List.of(
                        newLanguageRow("Chinese", 4_000L, 50.0),
                        newLanguageRow("English", 2_000L, 25.0)
                );

        StubPopulationService() {
            // Give PopulationService a NON-NULL repo, but we never hit the DB
            super(new PopulationRepo(new Db()));
        }

        @Override
        public List<PopulationRow> getRegionPopulationInOutCities() {
            return regionRows;
        }

        @Override
        public List<PopulationRow> getCountryPopulationInOutCities() {
            return countryRows;
        }

        @Override
        public long getWorldPopulation() {
            return 7_000_000_000L;
        }

        @Override
        public PopulationLookupRow getContinentPopulation(String continent) {
            return newLookupRow(continent, 1_000_000L);
        }

        @Override
        public PopulationLookupRow getRegionPopulation(String region) {
            return newLookupRow(region, 500_000L);
        }

        @Override
        public PopulationLookupRow getCountryPopulation(String country) {
            return newLookupRow(country, 100_000L);
        }

        @Override
        public PopulationLookupRow getDistrictPopulation(String district) {
            return newLookupRow(district, 50_000L);
        }

        @Override
        public PopulationLookupRow getCityPopulation(String city) {
            return newLookupRow(city, 10_000L);
        }

        @Override
        public List<LanguagePopulationRow> getLanguagePopulations() {
            return languageRows;
        }
    }

    // ---------------------------------------------------------------------
    // Constructor behaviour
    // ---------------------------------------------------------------------

    @Test
    void constructorRejectsNullService() {
        assertThrows(NullPointerException.class,
                () -> new PopulationRoutes(null));
    }

    // ---------------------------------------------------------------------
    // CSV helper tests – cover null branches + escaping
    // ---------------------------------------------------------------------

    @Test
    void buildPopulationCsvHandlesNullListAndNullRowsAndEscaping() {
        PopulationRoutes routes = new PopulationRoutes(new StubPopulationService());

        // rows == null -> header only
        String headerOnly = routes.buildPopulationCsv(null);
        assertEquals(
                "Name,TotalPopulation,CityPopulation,NonCityPopulation," +
                        "CityPopulationPercent,NonCityPopulationPercent\n",
                headerOnly
        );

        // list with null element + row needing escaping
        PopulationRow r1 = PopulationRow.fromTotals("SimpleRegion", 1_000, 400);
        PopulationRow r2 = PopulationRow.fromTotals("Region,With,Comma", 2_000, 800);

        // IMPORTANT: Arrays.asList allows null; List.of does NOT.
        List<PopulationRow> rows = Arrays.asList(null, r1, r2);

        String csv = routes.buildPopulationCsv(rows);
        String[] lines = csv.split("\\R");

        // header + 2 rows (null skipped)
        assertEquals(3, lines.length);
        assertEquals(
                "Name,TotalPopulation,CityPopulation,NonCityPopulation," +
                        "CityPopulationPercent,NonCityPopulationPercent",
                lines[0]
        );
        assertTrue(lines[1].contains("SimpleRegion"));
        assertTrue(lines[2].startsWith("\"Region,With,Comma\""));
    }

    @Test
    void buildWorldCsvProducesSingleRow() {
        PopulationRoutes routes = new PopulationRoutes(new StubPopulationService());

        String csv = routes.buildWorldCsv(1234L);
        assertEquals("Name,WorldPopulation\nWorld,1234\n", csv);
    }

    @Test
    void buildLookupCsvHandlesNullRowAndEscaping() {
        PopulationRoutes routes = new PopulationRoutes(new StubPopulationService());

        // row == null -> header only
        String headerOnly = routes.buildLookupCsv(null);
        assertEquals("Name,Population\n", headerOnly);

        // row needing escaping
        PopulationLookupRow row =
                newLookupRow("Asia, \"Example\"", 123L);

        String csv = routes.buildLookupCsv(row);
        String[] lines = csv.split("\\R");
        assertEquals(2, lines.length);
        assertEquals("Name,Population", lines[0]);
        assertTrue(lines[1].startsWith("\"Asia, \"\"Example\"\"\""));
        assertTrue(lines[1].endsWith(",123"));
    }

    @Test
    void buildLanguageCsvHandlesNullListNullRowsAndEscaping() {
        PopulationRoutes routes = new PopulationRoutes(new StubPopulationService());

        // rows == null -> header only
        String headerOnly = routes.buildLanguageCsv(null);
        assertEquals("Language,Speakers,WorldPopulationPercent\n", headerOnly);

        // list with null + row needing escaping
        LanguagePopulationRow r1 =
                newLanguageRow("PlainLanguage", 1_000L, 10.0);
        LanguagePopulationRow r2 =
                newLanguageRow("Lang,With,Comma", 2_000L, 20.0);

        // Arrays.asList so that null is allowed
        List<LanguagePopulationRow> rows = Arrays.asList(null, r1, r2);

        String csv = routes.buildLanguageCsv(rows);
        String[] lines = csv.split("\\R");

        // header + 2 rows
        assertEquals(3, lines.length);
        assertEquals("Language,Speakers,WorldPopulationPercent", lines[0]);
        assertTrue(lines[1].contains("PlainLanguage"));
        assertTrue(lines[2].startsWith("\"Lang,With,Comma\""));
    }

    @Test
    void escapeHandlesNullViaLookupCsv() {
        PopulationRoutes routes = new PopulationRoutes(new StubPopulationService());

        // name == null -> escape() should produce empty string
        PopulationLookupRow row = newLookupRow(null, 0L);

        String csv = routes.buildLookupCsv(row);
        String[] lines = csv.split("\\R");
        assertEquals(2, lines.length);
        assertEquals(",0", lines[1]); // empty name, population 0
    }

    // ---------------------------------------------------------------------
    // Hit every HTTP route (R24–R32 plus alias routes) using real Javalin
    // ---------------------------------------------------------------------

    @Test
    void allRoutesReturnCsvOverHttp() throws Exception {
        StubPopulationService service = new StubPopulationService();
        PopulationRoutes routes = new PopulationRoutes(service);

        Javalin app = Javalin.create();
        try {
            routes.register(app);
            app.start(0);      // random free port
            int port = app.port();

            HttpClient client = HttpClient.newHttpClient();

            // Helper lambda to send GET and assert 200 + non-empty body
            Function<String, String> get = path -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder(
                                    URI.create("http://localhost:" + port + path))
                            .GET()
                            .build();
                    HttpResponse<String> resp =
                            client.send(request, HttpResponse.BodyHandlers.ofString());
                    assertEquals(200, resp.statusCode(), "status for " + path);
                    String body = resp.body();
                    assertNotNull(body, "body for " + path);
                    assertFalse(body.isBlank(), "blank body for " + path);
                    return body;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            // Core endpoints R24–R32
            assertTrue(get.apply("/reports/population/regions")
                    .startsWith("Name,TotalPopulation"));
            assertTrue(get.apply("/reports/population/countries")
                    .startsWith("Name,TotalPopulation"));
            assertTrue(get.apply("/reports/population/world")
                    .startsWith("Name,WorldPopulation"));
            assertTrue(get.apply("/reports/population/continents/Asia")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/regions/Caribbean")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/countries/Myanmar")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/districts/Rangoon")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/cities/Rangoon%20(Yangon)")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/languages")
                    .startsWith("Language,Speakers,WorldPopulationPercent"));

            // Extra alias endpoints used by PowerShell script
            assertTrue(get.apply("/reports/population/continent?name=Asia")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/region?name=Caribbean")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/country?name=Myanmar")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/district?name=Rangoon")
                    .startsWith("Name,Population"));
            assertTrue(get.apply("/reports/population/city?name=Yangon")
                    .startsWith("Name,Population"));
        } finally {
            app.stop();
        }
    }
}
