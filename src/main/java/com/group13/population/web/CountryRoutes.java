package com.group13.population.web;

import com.group13.population.model.CountryRow;
import com.group13.population.service.CountryService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;

/**
 * HTTP routes for country reports R01–R06.
 *
 * All endpoints return CSV with header:
 *   Code,Name,Continent,Region,Population,Capital
 */
public class CountryRoutes {

    private final CountryService service;

    public CountryRoutes(CountryService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * Register all country routes on the given Javalin app.
     */
    public void register(Javalin app) {
        Objects.requireNonNull(app, "app");

        // R01 – All countries in the world
        app.get("/api/countries/world", this::handleWorld);

        // R02 – All countries in a continent
        app.get("/api/countries/continent/{continent}", this::handleContinent);

        // R03 – All countries in a region
        app.get("/api/countries/region/{region}", this::handleRegion);

        // R04 – Top-N countries in the world
        app.get("/api/countries/world/top", this::handleWorldTop);

        // R05 – Top-N countries in a continent
        app.get("/api/countries/continent/{continent}/top", this::handleContinentTop);

        // R06 – Top-N countries in a region
        app.get("/api/countries/region/{region}/top", this::handleRegionTop);
    }

    // ---------------------------------------------------------------------
    // Handlers for “all” reports (R01–R03)
    // ---------------------------------------------------------------------

    private void handleWorld(Context ctx) {
        List<CountryRow> rows = service.getCountriesInWorldByPopulationDesc();
        writeCountriesCsv(ctx, rows);
    }

    private void handleContinent(Context ctx) {
        String continent = ctx.pathParam("continent");
        List<CountryRow> rows = service.getCountriesInContinentByPopulationDesc(continent);
        writeCountriesCsv(ctx, rows);
    }

    private void handleRegion(Context ctx) {
        String region = ctx.pathParam("region");
        List<CountryRow> rows = service.getCountriesInRegionByPopulationDesc(region);
        writeCountriesCsv(ctx, rows);
    }

    // ---------------------------------------------------------------------
    // Handlers for “top N” reports (R04–R06)
    // ---------------------------------------------------------------------
    // NOTE: These use Javalin’s Validator so that:
    //   * missing n  -> HTTP 400
    //   * non-numeric n -> HTTP 400
    //   * n <= 0 -> HTTP 400
    // which is exactly what CountryRoutesTest expects.

    private void handleWorldTop(Context ctx) {
        int n = ctx.queryParamAsClass("n", Integer.class)
            .check(value -> value > 0, "n must be a positive integer")
            .get(); // throws BadRequestResponse -> HTTP 400 on failure

        List<CountryRow> rows = service.getTopCountriesInWorldByPopulationDesc(n);
        writeCountriesCsv(ctx, rows);
    }

    private void handleContinentTop(Context ctx) {
        String continent = ctx.pathParam("continent");

        int n = ctx.queryParamAsClass("n", Integer.class)
            .check(value -> value > 0, "n must be a positive integer")
            .get();

        List<CountryRow> rows =
            service.getTopCountriesInContinentByPopulationDesc(continent, n);
        writeCountriesCsv(ctx, rows);
    }

    private void handleRegionTop(Context ctx) {
        String region = ctx.pathParam("region");

        int n = ctx.queryParamAsClass("n", Integer.class)
            .check(value -> value > 0, "n must be a positive integer")
            .get();

        List<CountryRow> rows =
            service.getTopCountriesInRegionByPopulationDesc(region, n);
        writeCountriesCsv(ctx, rows);
    }

    // ---------------------------------------------------------------------
    // CSV helpers
    // ---------------------------------------------------------------------

    /**
     * Write a list of countries as CSV to the HTTP response.
     */
    private void writeCountriesCsv(Context ctx, List<CountryRow> rows) {
        ctx.contentType("text/csv; charset=utf-8");

        StringBuilder sb = new StringBuilder();
        sb.append("Code,Name,Continent,Region,Population,Capital\n");

        for (CountryRow row : rows) {
            sb.append(escape(row.getCode())).append(',')
                .append(escape(row.getName())).append(',')
                .append(escape(row.getContinent())).append(',')
                .append(escape(row.getRegion())).append(',')
                .append(row.getPopulation()).append(',')
                .append(escape(row.getCapital()))
                .append('\n');
        }

        ctx.result(sb.toString());
    }


    /**
     * Minimal CSV escaping – wraps in quotes if needed and doubles any quotes.
     */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        boolean mustQuote = v.contains(",") || v.contains("\"")
            || v.contains("\n") || v.contains("\r");

        if (mustQuote) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }
}
