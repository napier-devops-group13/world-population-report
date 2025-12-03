package com.group13.population.web;

import com.group13.population.db.Db;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * HTTP API routes for capital city reports (R17–R22).
 *
 * <p>These endpoints return CSV with the required columns:
 * Name, Country, Population.
 *
 * <p>Paths (all under {@code /api/capitals}):
 * <ul>
 *     <li>R17: {@code /api/capitals/world}</li>
 *     <li>R18: {@code /api/capitals/continent/{continent}}</li>
 *     <li>R19: {@code /api/capitals/region/{region}}</li>
 *     <li>R20: {@code /api/capitals/world/top/{limit}} (e.g. /world/top/10)</li>
 *     <li>R21: {@code /api/capitals/continent/{continent}/top/{limit}}</li>
 *     <li>R22: {@code /api/capitals/region/{region}/top/{limit}}</li>
 * </ul>
 */
public final class CapitalApiRoutes {

    private final Db db;

    /**
     * Creates a new instance of the capital API routes.
     *
     * @param db shared database helper
     */
    public CapitalApiRoutes(Db db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    /**
     * Register all capital API routes on the given Javalin app.
     *
     * @param app Javalin instance
     */
    public void register(Javalin app) {
        Objects.requireNonNull(app, "app");

        final String base = "/api/capitals";

        // R17 – all capital cities in the world
        app.get(base + "/world", this::handleWorld);

        // R18 – all capital cities in a continent
        app.get(base + "/continent/{continent}", this::handleContinent);

        // R19 – all capital cities in a region
        app.get(base + "/region/{region}", this::handleRegion);

        // R20 – top N capital cities in the world (path param {limit})
        app.get(base + "/world/top/{limit}", this::handleWorldTop);

        // R21 – top N capital cities in a continent
        app.get(base + "/continent/{continent}/top/{limit}", this::handleContinentTop);

        // R22 – top N capital cities in a region
        app.get(base + "/region/{region}/top/{limit}", this::handleRegionTop);
    }

    // -------------------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------------------

    private void handleWorld(Context ctx) {
        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.ID = country.Capital "
                + "ORDER BY city.Population DESC";

        streamCapitalsAsCsv(ctx, sql);
    }

    private void handleContinent(Context ctx) {
        final String continent = ctx.pathParam("continent");

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.ID = country.Capital "
                + "WHERE country.Continent = ? "
                + "ORDER BY city.Population DESC";

        streamCapitalsAsCsv(ctx, sql, continent);
    }

    private void handleRegion(Context ctx) {
        final String region = ctx.pathParam("region");

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.ID = country.Capital "
                + "WHERE country.Region = ? "
                + "ORDER BY city.Population DESC";

        streamCapitalsAsCsv(ctx, sql, region);
    }

    private void handleWorldTop(Context ctx) {
        final int limit = parseLimit(ctx.pathParam("limit"), 10);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.ID = country.Capital "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCapitalsAsCsv(ctx, sql, limit);
    }

    private void handleContinentTop(Context ctx) {
        final String continent = ctx.pathParam("continent");
        final int limit = parseLimit(ctx.pathParam("limit"), 5);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.ID = country.Capital "
                + "WHERE country.Continent = ? "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCapitalsAsCsv(ctx, sql, continent, limit);
    }

    private void handleRegionTop(Context ctx) {
        final String region = ctx.pathParam("region");
        final int limit = parseLimit(ctx.pathParam("limit"), 3);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.ID = country.Capital "
                + "WHERE country.Region = ? "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCapitalsAsCsv(ctx, sql, region, limit);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Parse a positive integer from a path parameter value, with default.
     *
     * @param raw          raw string (e.g. "10")
     * @param defaultValue default if null/blank/invalid or <= 0
     */
    static int parseLimit(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int n = Integer.parseInt(raw.trim());
            return n > 0 ? n : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Run the given SQL query and stream the results as CSV to the HTTP response.
     *
     * @param ctx    Javalin context
     * @param sql    parameterised SQL query
     * @param params positional parameters for the query
     */
    private void streamCapitalsAsCsv(Context ctx, String sql, Object... params) {
        ctx.contentType("text/csv; charset=UTF-8");
        ctx.header("Content-Disposition", "attachment; filename=\"capitals.csv\"");

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            // Bind parameters (String or Integer)
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                int index = i + 1;
                if (value instanceof Integer) {
                    stmt.setInt(index, (Integer) value);
                } else {
                    stmt.setString(index, String.valueOf(value));
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder csv = new StringBuilder();
                csv.append("Name,Country,Population").append('\n');

                while (rs.next()) {
                    String name = rs.getString("city_name");
                    String country = rs.getString("country_name");
                    long population = rs.getLong("population");

                    csv.append(escapeCsv(name))
                        .append(',')
                        .append(escapeCsv(country))
                        .append(',')
                        .append(population)
                        .append('\n');
                }

                ctx.result(csv.toString());
            }
        } catch (SQLException ex) {
            ctx.status(500).result("Database error: " + ex.getMessage());
        }
    }

    /**
     * Minimal CSV escaping for a single field – package-private static for testing.
     *
     * @param value raw string
     * @return escaped string, quoted if required
     */
    static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes =
            value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r");

        String escaped = value.replace("\"", "\"\"");

        if (needsQuotes) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
