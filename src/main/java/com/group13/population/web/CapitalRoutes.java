package com.group13.population.web;

import com.group13.population.model.CityRow;
import com.group13.population.service.CapitalService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;

/**
 * HTTP routes for capital city reports (R17–R22).
 *
 * <p>Endpoints are mounted under {@code /reports/capitals} to mirror
 * {@link CityRoutes} and {@link CountryRoutes} and to match the HTTP
 * smoke tests and evidence scripts.</p>
 */
public final class CapitalRoutes {

    private final CapitalService service;

    /**
     * Creates a new CapitalRoutes instance.
     *
     * @param service capital report service.
     */
    public CapitalRoutes(CapitalService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * Convenience entry point used by App.createApp and tests.
     *
     * @param app     running Javalin instance.
     * @param service capital report service.
     */
    public static void register(Javalin app, CapitalService service) {
        new CapitalRoutes(service).register(app);
    }

    /**
     * Register all capital city report endpoints (R17–R22).
     *
     * <p>Path pattern:</p>
     * <ul>
     *   <li>/reports/capitals/world</li>
     *   <li>/reports/capitals/continent/{continent}</li>
     *   <li>/reports/capitals/region/{region}</li>
     *   <li>/reports/capitals/world/top/{limit}</li>
     *   <li>/reports/capitals/continent/{continent}/top/{limit}</li>
     *   <li>/reports/capitals/region/{region}/top/{limit}</li>
     * </ul>
     */
    public void register(Javalin app) {
        Objects.requireNonNull(app, "app");

        final String base = "/reports/capitals";

        // R17 – all capital cities in the world (population DESC)
        app.get(base + "/world", this::handleWorldCapitals);

        // R18 – all capital cities in a continent (population DESC)
        app.get(base + "/continent/{continent}", this::handleContinentCapitals);

        // R19 – all capital cities in a region (population DESC)
        app.get(base + "/region/{region}", this::handleRegionCapitals);

        // R20 – top-N capitals in the world, e.g. /reports/capitals/world/top/10
        app.get(base + "/world/top/{limit}", this::handleTopWorldCapitals);

        // R21 – top-N capitals in a continent,
        // e.g. /reports/capitals/continent/Europe/top/5
        app.get(base + "/continent/{continent}/top/{limit}",
            this::handleTopContinentCapitals);

        // R22 – top-N capitals in a region,
        // e.g. /reports/capitals/region/Caribbean/top/3
        app.get(base + "/region/{region}/top/{limit}",
            this::handleTopRegionCapitals);
    }

    // ---------------------------------------------------------------------
    // Handlers
    // ---------------------------------------------------------------------

    private void handleWorldCapitals(Context ctx) {
        List<CityRow> rows = service.getCapitalCitiesInWorldByPopulationDesc();
        writeCsv(ctx, rows);
    }

    private void handleContinentCapitals(Context ctx) {
        String continent = ctx.pathParam("continent");
        List<CityRow> rows =
            service.getCapitalCitiesInContinentByPopulationDesc(continent);
        writeCsv(ctx, rows);
    }

    private void handleRegionCapitals(Context ctx) {
        String region = ctx.pathParam("region");
        List<CityRow> rows =
            service.getCapitalCitiesInRegionByPopulationDesc(region);
        writeCsv(ctx, rows);
    }

    private void handleTopWorldCapitals(Context ctx) {
        int n = parseLimit(ctx.pathParam("limit"), 10);
        List<CityRow> rows =
            service.getTopCapitalCitiesInWorldByPopulationDesc(n);
        writeCsv(ctx, rows);
    }

    private void handleTopContinentCapitals(Context ctx) {
        String continent = ctx.pathParam("continent");
        int n = parseLimit(ctx.pathParam("limit"), 5);
        List<CityRow> rows =
            service.getTopCapitalCitiesInContinentByPopulationDesc(continent, n);
        writeCsv(ctx, rows);
    }

    private void handleTopRegionCapitals(Context ctx) {
        String region = ctx.pathParam("region");
        int n = parseLimit(ctx.pathParam("limit"), 3);
        List<CityRow> rows =
            service.getTopCapitalCitiesInRegionByPopulationDesc(region, n);
        writeCsv(ctx, rows);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Parses a positive integer limit from a raw path parameter value.
     *
     * @param raw          value from the path, for example {@code "10"}.
     * @param defaultValue fallback value when the input is missing or invalid.
     * @return a positive limit value.
     */
    static int parseLimit(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Writes the given capital city rows as a CSV HTTP response.
     *
     * <p>The CSV header is {@code Name,Country,District,Population}.</p>
     *
     * @param ctx  Javalin request/response context.
     * @param rows capital city rows to serialise.
     */
    private void writeCsv(Context ctx, List<CityRow> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append("Name,Country,District,Population\n");
        for (CityRow row : rows) {
            csv.append(escape(row.getName())).append(',');
            csv.append(escape(row.getCountry())).append(',');
            csv.append(escape(row.getDistrict())).append(',');
            csv.append(row.getPopulation()).append('\n');
        }
        ctx.contentType("text/csv");
        ctx.result(csv.toString());
    }

    /**
     * Escapes a value for safe inclusion in a CSV field.
     *
     * @param value input string value.
     * @return CSV-escaped representation of {@code value}.
     */
    static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
