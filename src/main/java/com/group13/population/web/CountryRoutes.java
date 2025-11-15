package com.group13.population.web;

import com.group13.population.service.CountryService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * HTTP routes for the country reports R01–R06.
 *
 * <ul>
 *   <li>R01: all countries in the world</li>
 *   <li>R02: all countries in a continent</li>
 *   <li>R03: all countries in a region</li>
 *   <li>R04: top-N countries in the world</li>
 *   <li>R05: top-N countries in a continent</li>
 *   <li>R06: top-N countries in a region</li>
 * </ul>
 */
public final class CountryRoutes {

    private final CountryService service;

    public CountryRoutes(CountryService service) {
        this.service = service;
    }

    /** Register all endpoints on the given Javalin instance. */
    public void register(Javalin app) {

        // R01 – all countries in the world
        app.get("/api/countries/world", ctx ->
            ctx.json(service.allCountriesWorld())
        );

        // R02 – all countries in a continent
        app.get("/api/countries/continent/{continent}", ctx -> {
            String continent = ctx.pathParam("continent");
            ctx.json(service.allCountriesContinent(continent));
        });

        // R03 – all countries in a region
        app.get("/api/countries/region/{region}", ctx -> {
            String region = ctx.pathParam("region");
            ctx.json(service.allCountriesRegion(region));
        });

        // R04 – top-N countries in the world
        app.get("/api/countries/world/top/{n}", ctx -> {
            Integer n = parsePositiveOr400(ctx, ctx.pathParam("n"));
            if (n == null) {
                // 400 already set in parsePositiveOr400
                return;
            }
            ctx.json(service.topCountriesWorld(n));
        });

        // R05 – top-N countries in a continent
        app.get("/api/countries/continent/{continent}/top/{n}", ctx -> {
            String continent = ctx.pathParam("continent");
            Integer n = parsePositiveOr400(ctx, ctx.pathParam("n"));
            if (n == null) {
                return;
            }
            ctx.json(service.topCountriesContinent(continent, n));
        });

        // R06 – top-N countries in a region
        app.get("/api/countries/region/{region}/top/{n}", ctx -> {
            String region = ctx.pathParam("region");
            Integer n = parsePositiveOr400(ctx, ctx.pathParam("n"));
            if (n == null) {
                return;
            }
            ctx.json(service.topCountriesRegion(region, n));
        });
    }

    /**
     * Convert a path parameter {@code n} into a positive integer.
     * <p>
     * If parsing fails or the value is not positive, this method writes
     * a 400 Bad Request response and returns {@code null}, which the
     * caller checks for.
     * </p>
     */
    private Integer parsePositiveOr400(Context ctx, String raw) {
        try {
            int n = Integer.parseInt(raw);
            if (n <= 0) {
                ctx.status(400).result("n must be a positive integer");
                return null;
            }
            return n;
        } catch (NumberFormatException ex) {
            ctx.status(400).result("n must be an integer");
            return null;
        }
    }
}
