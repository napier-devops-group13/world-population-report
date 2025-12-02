package com.group13.population.web;

import com.group13.population.model.LanguagePopulationRow;
import com.group13.population.model.PopulationLookupRow;
import com.group13.population.model.PopulationRow;
import com.group13.population.service.PopulationService;
import io.javalin.Javalin;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * HTTP endpoints for population reports R24–R32.
 *
 * R24 – /reports/population/regions
 * R25 – /reports/population/countries
 * R26 – /reports/population/world
 *
 * R27 – /reports/population/continents/{continent}
 *    or /reports/population/continent?name=Asia
 *
 * R28 – /reports/population/regions/{region}
 *    or /reports/population/region?name=Caribbean
 *
 * R29 – /reports/population/countries/{country}
 *    or /reports/population/country?name=Myanmar
 *
 * R30 – /reports/population/districts/{district}
 *    or /reports/population/district?name=Rangoon
 *
 * R31 – /reports/population/cities/{city}
 *    or /reports/population/city?name=Yangon
 *
 * R32 – /reports/population/languages
 */
public class PopulationRoutes {

    private final PopulationService populationService;

    public PopulationRoutes(PopulationService populationService) {
        this.populationService = Objects.requireNonNull(populationService, "populationService");
    }

    /**
     * Register population routes with the given Javalin application.
     */
    public void register(Javalin app) {
        // -----------------------------------------------------------------
        // R24 – regions (population in / not in cities)
        // -----------------------------------------------------------------
        app.get("/reports/population/regions", ctx -> {
            List<PopulationRow> rows = populationService.getRegionPopulationInOutCities();
            ctx.contentType("text/csv");
            ctx.result(buildPopulationCsv(rows));
        });

        // -----------------------------------------------------------------
        // R25 – countries (population in / not in cities)
        // -----------------------------------------------------------------
        app.get("/reports/population/countries", ctx -> {
            List<PopulationRow> rows = populationService.getCountryPopulationInOutCities();
            ctx.contentType("text/csv");
            ctx.result(buildPopulationCsv(rows));
        });

        // -----------------------------------------------------------------
        // R26 – world population
        // -----------------------------------------------------------------
        app.get("/reports/population/world", ctx -> {
            long worldPopulation = populationService.getWorldPopulation();
            ctx.contentType("text/csv");
            ctx.result(buildWorldCsv(worldPopulation));
        });

        // -----------------------------------------------------------------
        // R27 – population of a continent
        // Example: /reports/population/continents/Asia
        // -----------------------------------------------------------------
        app.get("/reports/population/continents/{continent}", ctx -> {
            String continent = ctx.pathParam("continent");
            PopulationLookupRow row = populationService.getContinentPopulation(continent);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // Alias used by PowerShell script:
        //   /reports/population/continent?name=Asia
        app.get("/reports/population/continent", ctx -> {
            String name = ctx.queryParam("name");
            if (name == null || name.isBlank()) {
                ctx.status(400).result("Missing required query parameter 'name'");
                return;
            }
            PopulationLookupRow row = populationService.getContinentPopulation(name);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // -----------------------------------------------------------------
        // R28 – population of a region
        // Example: /reports/population/regions/Eastern%20Asia
        // -----------------------------------------------------------------
        app.get("/reports/population/regions/{region}", ctx -> {
            String region = ctx.pathParam("region");
            PopulationLookupRow row = populationService.getRegionPopulation(region);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // Alias for script:
        //   /reports/population/region?name=Caribbean
        app.get("/reports/population/region", ctx -> {
            String name = ctx.queryParam("name");
            if (name == null || name.isBlank()) {
                ctx.status(400).result("Missing required query parameter 'name'");
                return;
            }
            PopulationLookupRow row = populationService.getRegionPopulation(name);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // -----------------------------------------------------------------
        // R29 – population of a country
        // Example: /reports/population/countries/Myanmar
        // -----------------------------------------------------------------
        app.get("/reports/population/countries/{country}", ctx -> {
            String country = ctx.pathParam("country");
            PopulationLookupRow row = populationService.getCountryPopulation(country);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // Alias for script:
        //   /reports/population/country?name=Myanmar
        app.get("/reports/population/country", ctx -> {
            String name = ctx.queryParam("name");
            if (name == null || name.isBlank()) {
                ctx.status(400).result("Missing required query parameter 'name'");
                return;
            }
            PopulationLookupRow row = populationService.getCountryPopulation(name);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // -----------------------------------------------------------------
        // R30 – population of a district
        // Example: /reports/population/districts/Yangon
        // -----------------------------------------------------------------
        app.get("/reports/population/districts/{district}", ctx -> {
            String district = ctx.pathParam("district");
            PopulationLookupRow row = populationService.getDistrictPopulation(district);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // Alias for script:
        //   /reports/population/district?name=Rangoon
        app.get("/reports/population/district", ctx -> {
            String name = ctx.queryParam("name");
            if (name == null || name.isBlank()) {
                ctx.status(400).result("Missing required query parameter 'name'");
                return;
            }
            PopulationLookupRow row = populationService.getDistrictPopulation(name);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // -----------------------------------------------------------------
        // R31 – population of a city
        // Example: /reports/population/cities/Yangon
        // -----------------------------------------------------------------
        app.get("/reports/population/cities/{city}", ctx -> {
            String city = ctx.pathParam("city");
            PopulationLookupRow row = populationService.getCityPopulation(city);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // Alias for script:
        //   /reports/population/city?name=Yangon
        app.get("/reports/population/city", ctx -> {
            String name = ctx.queryParam("name");
            if (name == null || name.isBlank()) {
                ctx.status(400).result("Missing required query parameter 'name'");
                return;
            }
            PopulationLookupRow row = populationService.getCityPopulation(name);
            ctx.contentType("text/csv");
            ctx.result(buildLookupCsv(row));
        });

        // -----------------------------------------------------------------
        // R32 – language populations (Chinese, English, Hindi, Spanish, Arabic)
        // -----------------------------------------------------------------
        app.get("/reports/population/languages", ctx -> {
            List<LanguagePopulationRow> rows = populationService.getLanguagePopulations();
            ctx.contentType("text/csv");
            ctx.result(buildLanguageCsv(rows));
        });
    }

    /**
     * Build CSV for R24/R25 style population reports.
     * Package-private so tests in the same package can call it.
     */
    String buildPopulationCsv(List<PopulationRow> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append("Name,TotalPopulation,CityPopulation,NonCityPopulation,")
                .append("CityPopulationPercent,NonCityPopulationPercent\n");

        if (rows == null) {
            // header only
            return csv.toString();
        }

        for (PopulationRow r : rows) {
            if (r == null) {
                continue;
            }
            csv.append(escape(r.getName())).append(',')
                    .append(r.getTotalPopulation()).append(',')
                    .append(r.getCityPopulation()).append(',')
                    .append(r.getNonCityPopulation()).append(',')
                    .append(String.format(Locale.US, "%.2f", r.getCityPopulationPercent())).append(',')
                    .append(String.format(Locale.US, "%.2f", r.getNonCityPopulationPercent()))
                    .append('\n');
        }

        return csv.toString();
    }

    /**
     * Build CSV for R26 (world population).
     */
    String buildWorldCsv(long worldPopulation) {
        return "Name,WorldPopulation\nWorld," + worldPopulation + "\n";
    }

    // ---------------------------------------------------------------------
    // Helpers for R27–R31 (simple lookup) and R32 (languages)
    // ---------------------------------------------------------------------

    /**
     * Build CSV for lookup-style population reports (R27–R31).
     * Header: Name,Population
     */
    String buildLookupCsv(PopulationLookupRow row) {
        StringBuilder csv = new StringBuilder();
        csv.append("Name,Population\n");

        if (row == null) {
            return csv.toString();
        }

        csv.append(escape(row.getName())).append(',')
                .append(row.getPopulation()).append('\n');

        return csv.toString();
    }

    /**
     * Build CSV for language population report (R32).
     * Header: Language,Speakers,WorldPopulationPercent
     */
    String buildLanguageCsv(List<LanguagePopulationRow> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append("Language,Speakers,WorldPopulationPercent\n");

        if (rows == null) {
            return csv.toString();
        }

        for (LanguagePopulationRow r : rows) {
            if (r == null) {
                continue;
            }
            csv.append(escape(r.getLanguage())).append(',')
                    .append(r.getSpeakers()).append(',')
                    .append(String.format(Locale.US, "%.2f", r.getWorldPopulationPercent()))
                    .append('\n');
        }

        return csv.toString();
    }

    /**
     * Tiny CSV escaping helper for names containing commas or quotes.
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
