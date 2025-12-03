package com.group13.population.web;

import com.group13.population.db.Db;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * HTTP API routes for city reports (R07–R16).
 *
 * These endpoints return CSV with the required columns:
 * Name, Country, District, Population.
 *
 * All paths are under {@code /api/cities}:
 *
 * R07: /api/cities/world
 * R08: /api/cities/continent/{continent}
 * R09: /api/cities/region/{region}
 * R10: /api/cities/country/{country}
 * R11: /api/cities/district/{district}
 *
 * R12: /api/cities/world/top?n=10
 * R13: /api/cities/continent/{continent}/top?n=5
 * R14: /api/cities/region/{region}/top?n=5
 * R15: /api/cities/country/{country}/top?n=5
 * R16: /api/cities/district/{district}/top?n=3
 */
public final class CityApiRoutes {

    private final Db db;

    /**
     * Creates a new instance of the city API routes.
     *
     * @param db shared database helper
     */
    public CityApiRoutes(Db db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    /**
     * Register all city API routes on the given Javalin app.
     *
     * @param app Javalin instance
     */
    public void register(Javalin app) {
        Objects.requireNonNull(app, "app");

        final String base = "/api/cities";

        // R07 – all cities in the world
        app.get(base + "/world", this::handleWorld);

        // R08 – all cities in a continent
        app.get(base + "/continent/{continent}", this::handleContinent);

        // R09 – all cities in a region
        app.get(base + "/region/{region}", this::handleRegion);

        // R10 – all cities in a country
        app.get(base + "/country/{country}", this::handleCountry);

        // R11 – all cities in a district
        app.get(base + "/district/{district}", this::handleDistrict);

        // R12 – top N cities in the world
        app.get(base + "/world/top", this::handleWorldTop);

        // R13 – top N cities in a continent
        app.get(base + "/continent/{continent}/top", this::handleContinentTop);

        // R14 – top N cities in a region
        app.get(base + "/region/{region}/top", this::handleRegionTop);

        // R15 – top N cities in a country
        app.get(base + "/country/{country}/top", this::handleCountryTop);

        // R16 – top N cities in a district
        app.get(base + "/district/{district}/top", this::handleDistrictTop);
    }

    // -------------------------------------------------------------------------
    // Handlers (R07–R16)
    // -------------------------------------------------------------------------

    private void handleWorld(Context ctx) {
        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "ORDER BY city.Population DESC";

        streamCitiesAsCsv(ctx, sql);
    }

    private void handleContinent(Context ctx) {
        final String continent = ctx.pathParam("continent");

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE country.Continent = ? "
                + "ORDER BY city.Population DESC";

        streamCitiesAsCsv(ctx, sql, continent);
    }

    private void handleRegion(Context ctx) {
        final String region = ctx.pathParam("region");

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE country.Region = ? "
                + "ORDER BY city.Population DESC";

        streamCitiesAsCsv(ctx, sql, region);
    }

    private void handleCountry(Context ctx) {
        final String country = ctx.pathParam("country");

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE country.Name = ? "
                + "ORDER BY city.Population DESC";

        streamCitiesAsCsv(ctx, sql, country);
    }

    private void handleDistrict(Context ctx) {
        final String district = ctx.pathParam("district");

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE city.District = ? "
                + "ORDER BY city.Population DESC";

        streamCitiesAsCsv(ctx, sql, district);
    }

    private void handleWorldTop(Context ctx) {
        final int limit = parseLimit(ctx, 10);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCitiesAsCsv(ctx, sql, limit);
    }

    private void handleContinentTop(Context ctx) {
        final String continent = ctx.pathParam("continent");
        final int limit = parseLimit(ctx, 5);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE country.Continent = ? "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCitiesAsCsv(ctx, sql, continent, limit);
    }

    private void handleRegionTop(Context ctx) {
        final String region = ctx.pathParam("region");
        final int limit = parseLimit(ctx, 5);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE country.Region = ? "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCitiesAsCsv(ctx, sql, region, limit);
    }

    private void handleCountryTop(Context ctx) {
        final String country = ctx.pathParam("country");
        final int limit = parseLimit(ctx, 5);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE country.Name = ? "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCitiesAsCsv(ctx, sql, country, limit);
    }

    private void handleDistrictTop(Context ctx) {
        final String district = ctx.pathParam("district");
        final int limit = parseLimit(ctx, 3);

        final String sql =
            "SELECT city.Name AS city_name, "
                + "country.Name AS country_name, "
                + "city.District AS district, "
                + "city.Population AS population "
                + "FROM city "
                + "JOIN country ON city.CountryCode = country.Code "
                + "WHERE city.District = ? "
                + "ORDER BY city.Population DESC "
                + "LIMIT ?";

        streamCitiesAsCsv(ctx, sql, district, limit);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Parse ?n= query parameter with a sensible default. */
    private int parseLimit(Context ctx, int defaultValue) {
        String raw = ctx.queryParam("n");
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
     * Run the given SQL query and stream the results as CSV.
     */
    private void streamCitiesAsCsv(Context ctx, String sql, Object... params) {
        ctx.contentType("text/csv; charset=UTF-8");
        ctx.header("Content-Disposition", "attachment; filename=\"cities.csv\"");

        // All SQL work (including db.getConnection) is inside this try,
        // so any SQLException is correctly caught and turned into 500.
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
                csv.append("Name,Country,District,Population").append('\n');

                while (rs.next()) {
                    String name = rs.getString("city_name");
                    String country = rs.getString("country_name");
                    String district = rs.getString("district");
                    long population = rs.getLong("population");

                    csv.append(escapeCsv(name))
                        .append(',')
                        .append(escapeCsv(country))
                        .append(',')
                        .append(escapeCsv(district))
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

    /** Minimal CSV escaping for a single field – made package-private static for testing. */
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
