package com.group13.population.web;

import com.group13.population.db.Db;
import com.group13.population.model.PopulationRow;
import com.group13.population.repo.PopulationRepo;
import com.group13.population.service.PopulationService;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PopulationRoutes.
 *
 * We test the CSV-building helpers directly (no HTTP server needed)
 * and also exercise the registered HTTP endpoints via JavalinTest so
 * that the route handlers themselves are covered for JaCoCo.
 */
class PopulationRoutesTest {

    // Paths MUST match PopulationRoutes.register(...)
    private static final String REGIONS_PATH   = "/reports/population/regions";
    private static final String COUNTRIES_PATH = "/reports/population/countries";
    private static final String WORLD_PATH     = "/reports/population/world";

    @Test
    void buildPopulationCsvProducesHeaderAndRows() {
        PopulationRow r1 = PopulationRow.fromTotals("RegionOne", 1000, 400);
        PopulationRow r2 = PopulationRow.fromTotals("RegionTwo", 500, 100);

        PopulationRoutes routes = new PopulationRoutes(
            new StubService(List.of(r1, r2), List.of(), 0L)
        );

        String csv = routes.buildPopulationCsv(List.of(r1, r2));
        String[] lines = csv.split("\\R");

        // header + 2 rows
        assertEquals(3, lines.length);
        assertEquals(
            "Name,TotalPopulation,CityPopulation,NonCityPopulation," +
                "CityPopulationPercent,NonCityPopulationPercent",
            lines[0]
        );
        assertTrue(lines[1].startsWith("RegionOne,1000,400,600,"));
        assertTrue(lines[2].startsWith("RegionTwo,500,100,400,"));
    }

    @Test
    void buildPopulationCsvHandlesNullListAndNullRows() {
        PopulationRoutes routes = new PopulationRoutes(
            new StubService(List.of(), List.of(), 0L)
        );

        // null list → header only
        String csv = routes.buildPopulationCsv(null);
        assertTrue(csv.startsWith(
            "Name,TotalPopulation,CityPopulation,NonCityPopulation," +
                "CityPopulationPercent,NonCityPopulationPercent"));

        // list with a single null row → still just header
        csv = routes.buildPopulationCsv(
            java.util.Collections.singletonList((PopulationRow) null)
        );
        String[] lines = csv.split("\\R");
        assertEquals(1, lines.length);
    }

    @Test
    void buildPopulationCsvHandlesZeroTotalPopulation() {
        // extra branch coverage, especially for formatting
        PopulationRow zero = PopulationRow.fromTotals("Nowhere", 0, 0);

        PopulationRoutes routes = new PopulationRoutes(
            new StubService(List.of(zero), List.of(), 0L)
        );

        String csv = routes.buildPopulationCsv(List.of(zero));
        String[] lines = csv.split("\\R");

        assertEquals(2, lines.length);
        assertEquals(
            "Name,TotalPopulation,CityPopulation,NonCityPopulation," +
                "CityPopulationPercent,NonCityPopulationPercent",
            lines[0]
        );
        // we just check it starts correctly so we don't depend too much
        // on PopulationRow's internal formatting
        assertTrue(lines[1].startsWith("Nowhere,0,0,0,"));
    }

    @Test
    void buildWorldCsvProducesSingleRow() {
        PopulationRoutes routes = new PopulationRoutes(
            new StubService(List.of(), List.of(), 123456789L)
        );

        String csv = routes.buildWorldCsv(123456789L);
        String[] lines = csv.trim().split("\\R");

        assertEquals(2, lines.length);
        assertEquals("Name,WorldPopulation", lines[0]);
        assertEquals("World,123456789", lines[1]);
    }

    @Test
    void registerDoesNotThrowWithFreshJavalin() {
        PopulationRoutes routes = new PopulationRoutes(
            new StubService(List.of(), List.of(), 0L)
        );
        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

        // Just ensure we can register handlers without any exceptions.
        assertDoesNotThrow(() -> routes.register(app));
    }

    // -----------------------------------------------------------------
    // HTTP-level tests to execute the route handlers themselves
    // -----------------------------------------------------------------

    @Test
    void regionEndpointReturnsCsvFromService() {
        PopulationRow r1 = PopulationRow.fromTotals("RegionOne", 1000, 400);
        PopulationRow r2 = PopulationRow.fromTotals("RegionTwo", 500, 100);

        StubService service = new StubService(List.of(r1, r2), List.of(), 0L);
        PopulationRoutes routes = new PopulationRoutes(service);

        JavalinTest.test(Javalin.create(c -> c.showJavalinBanner = false), (app, client) -> {
            routes.register(app);

            try (Response res = client.get(REGIONS_PATH)) {
                assertEquals(200, res.code());
                assertEquals("text/csv", res.header("Content-Type"));

                String body = res.body().string().trim();
                String[] lines = body.split("\\R");

                assertEquals(3, lines.length);
                assertTrue(lines[1].startsWith("RegionOne,1000,400,600,"));
                assertTrue(lines[2].startsWith("RegionTwo,500,100,400,"));
            } catch (IOException e) {
                fail(e);
            }
        });
    }

    @Test
    void countryEndpointReturnsCsvFromService() {
        PopulationRow c1 = PopulationRow.fromTotals("CountryA", 2000, 800);
        PopulationRow c2 = PopulationRow.fromTotals("CountryB", 1500, 300);

        StubService service = new StubService(List.of(), List.of(c1, c2), 0L);
        PopulationRoutes routes = new PopulationRoutes(service);

        JavalinTest.test(Javalin.create(c -> c.showJavalinBanner = false), (app, client) -> {
            routes.register(app);

            try (Response res = client.get(COUNTRIES_PATH)) {
                assertEquals(200, res.code());
                assertEquals("text/csv", res.header("Content-Type"));

                String body = res.body().string().trim();
                String[] lines = body.split("\\R");

                assertEquals(3, lines.length);
                assertTrue(lines[1].startsWith("CountryA,2000,800,1200,"));
                assertTrue(lines[2].startsWith("CountryB,1500,300,1200,"));
            } catch (IOException e) {
                fail(e);
            }
        });
    }

    @Test
    void worldEndpointReturnsCsvFromService() {
        StubService service = new StubService(List.of(), List.of(), 999_999_999L);
        PopulationRoutes routes = new PopulationRoutes(service);

        JavalinTest.test(Javalin.create(c -> c.showJavalinBanner = false), (app, client) -> {
            routes.register(app);

            try (Response res = client.get(WORLD_PATH)) {
                assertEquals(200, res.code());
                assertEquals("text/csv", res.header("Content-Type"));

                String body = res.body().string().trim();
                String[] lines = body.split("\\R");

                assertEquals(2, lines.length);
                assertEquals("Name,WorldPopulation", lines[0]);
                assertEquals("World,999999999", lines[1]);
            } catch (IOException e) {
                fail(e);
            }
        });
    }

    // -----------------------------------------------------------------
    // Minimal stub service – keeps tests independent of DB / real repo.
    // -----------------------------------------------------------------

    private static class StubService extends PopulationService {

        private final List<PopulationRow> regions;
        private final List<PopulationRow> countries;
        private final long world;

        StubService(List<PopulationRow> regions,
                    List<PopulationRow> countries,
                    long world) {
            // We pass a real repo+db but never use it because we override methods.
            super(new PopulationRepo(new Db()));
            this.regions = regions;
            this.countries = countries;
            this.world = world;
        }

        @Override
        public List<PopulationRow> getRegionPopulationInOutCities() {
            return regions;
        }

        @Override
        public List<PopulationRow> getCountryPopulationInOutCities() {
            return countries;
        }

        @Override
        public long getWorldPopulation() {
            return world;
        }
    }
}
