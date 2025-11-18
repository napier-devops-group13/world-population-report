package com.group13.population.web;

import com.group13.population.service.CityService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * HTTP routes for city reports (R07–R16).
 *
 * City report columns:
 *   - name
 *   - country
 *   - district
 *   - population
 */
public final class CityRoutes {

    private static final String BASE = "/api/cities";

    private final CityService service;

    public CityRoutes(final CityService service) {
        this.service = service;
    }

    /**
     * Register all city endpoints on the given Javalin app.
     */
    public void register(final Javalin app) {

        // ------------------------------------------------
        // R07–R11: "all" city reports
        // ------------------------------------------------

        // All cities in the world
        app.get(BASE + "/world", this::worldAll);

        // All cities in a continent (singular + plural aliases)
        app.get(BASE + "/continent/{continent}", this::continentAll);
        app.get(BASE + "/continents/{continent}", this::continentAll);

        // All cities in a region
        app.get(BASE + "/region/{region}", this::regionAll);

        // All cities in a country
        app.get(BASE + "/country/{country}", this::countryAll);

        // All cities in a district (singular + plural aliases)
        app.get(BASE + "/district/{district}", this::districtAll);
        app.get(BASE + "/districts/{district}", this::districtAll);

        // ------------------------------------------------
        // R12–R16: top-N city reports (N from query param ?n=)
        // ------------------------------------------------

        // Top-N cities in the world
        app.get(BASE + "/world/top", this::worldTopN);

        // Top-N cities in a continent (singular + plural aliases)
        app.get(BASE + "/continent/{continent}/top", this::continentTopN);
        app.get(BASE + "/continents/{continent}/top", this::continentTopN);

        // Top-N cities in a region
        app.get(BASE + "/region/{region}/top", this::regionTopN);

        // Top-N cities in a country
        app.get(BASE + "/country/{country}/top", this::countryTopN);

        // Top-N cities in a district (singular + plural aliases)
        app.get(BASE + "/district/{district}/top", this::districtTopN);
        app.get(BASE + "/districts/{district}/top", this::districtTopN);
    }

    // ------------------------------------------------
    // Handlers: "all" reports
    // ------------------------------------------------

    private void worldAll(final Context ctx) {
        ctx.json(service.worldAll());
    }

    private void continentAll(final Context ctx) {
        String continent = ctx.pathParam("continent");
        ctx.json(service.continentAll(continent));
    }

    private void regionAll(final Context ctx) {
        String region = ctx.pathParam("region");
        ctx.json(service.regionAll(region));
    }

    private void countryAll(final Context ctx) {
        String country = ctx.pathParam("country");
        ctx.json(service.countryAll(country));
    }

    private void districtAll(final Context ctx) {
        String district = ctx.pathParam("district");
        ctx.json(service.districtAll(district));
    }

    // ------------------------------------------------
    // Handlers: top-N reports
    // ------------------------------------------------

    private void worldTopN(final Context ctx) {
        int n = parseLimit(ctx);
        ctx.json(service.worldTopN(n));
    }

    private void continentTopN(final Context ctx) {
        String continent = ctx.pathParam("continent");
        int n = parseLimit(ctx);
        ctx.json(service.continentTopN(continent, n));
    }

    private void regionTopN(final Context ctx) {
        String region = ctx.pathParam("region");
        int n = parseLimit(ctx);
        ctx.json(service.regionTopN(region, n));
    }

    private void countryTopN(final Context ctx) {
        String country = ctx.pathParam("country");
        int n = parseLimit(ctx);
        ctx.json(service.countryTopN(country, n));
    }

    private void districtTopN(final Context ctx) {
        String district = ctx.pathParam("district");
        int n = parseLimit(ctx);
        ctx.json(service.districtTopN(district, n));
    }

    // ------------------------------------------------
    // Helper
    // ------------------------------------------------

    /**
     * Read query param "n", default 10, and clamp to [1, 100].
     */
    private int parseLimit(final Context ctx) {
        String raw = ctx.queryParam("n");
        int n = 10;

        if (raw != null) {
            try {
                n = Integer.parseInt(raw);
            } catch (NumberFormatException ignored) {
                // keep default of 10
            }
        }

        if (n < 1) {
            n = 1;
        } else if (n > 100) {
            n = 100;
        }

        return n;
    }
}
