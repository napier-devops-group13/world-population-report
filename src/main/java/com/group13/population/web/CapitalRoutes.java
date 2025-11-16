package com.group13.population.web;

import com.group13.population.service.CapitalService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

/**
 * HTTP routes for capital city reports (R17–R22).
 *
 * Base path: /api/capitals/...
 */
public final class CapitalRoutes {

    private final CapitalService service;

    public CapitalRoutes(CapitalService service) {
        this.service = service;
    }

    /**
     * Register all capital-city endpoints on the given app.
     */
    public void register(Javalin app) {
        // R17 – all capitals in the world
        app.get("/api/capitals/world", ctx ->
            ctx.json(service.allCapitalsWorld()));

        // R18 – all capitals in a continent
        app.get("/api/capitals/continent/{continent}", ctx -> {
            String continent = ctx.pathParam("continent");
            ctx.json(service.allCapitalsContinent(continent));
        });

        // R19 – all capitals in a region
        app.get("/api/capitals/region/{region}", ctx -> {
            String region = ctx.pathParam("region");
            ctx.json(service.allCapitalsRegion(region));
        });

        // R20 – top-N capitals in the world
        app.get("/api/capitals/world/top/{n}", this::handleTopWorld);

        // R21 – top-N capitals in a continent
        app.get(
            "/api/capitals/continent/{continent}/top/{n}",
            this::handleTopContinent
        );

        // R22 – top-N capitals in a region
        app.get(
            "/api/capitals/region/{region}/top/{n}",
            this::handleTopRegion
        );
    }

    private void handleTopWorld(Context ctx) {
        Integer n = parsePositiveNOrSendError(ctx);
        if (n == null) {
            return;
        }
        ctx.json(service.topCapitalsWorld(n));
    }

    private void handleTopContinent(Context ctx) {
        Integer n = parsePositiveNOrSendError(ctx);
        if (n == null) {
            return;
        }
        String continent = ctx.pathParam("continent");
        ctx.json(service.topCapitalsContinent(continent, n));
    }

    private void handleTopRegion(Context ctx) {
        Integer n = parsePositiveNOrSendError(ctx);
        if (n == null) {
            return;
        }
        String region = ctx.pathParam("region");
        ctx.json(service.topCapitalsRegion(region, n));
    }

    /**
     * Helper that parses {@code n} and sends a 400 JSON error if invalid.
     *
     * @return the positive integer N, or {@code null} if an error response
     *         has already been sent.
     */
    private Integer parsePositiveNOrSendError(Context ctx) {
        String raw = ctx.pathParam("n");
        int n;

        try {
            n = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            ctx.status(400);
            ctx.json(Map.of("error", "n must be an integer"));
            return null;
        }

        if (n <= 0) {
            ctx.status(400);
            ctx.json(Map.of("error", "n must be > 0"));
            return null;
        }

        return n;
    }
}
