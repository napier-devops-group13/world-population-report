package com.group13.population.web;

import com.group13.population.model.PopulationRow;
import com.group13.population.service.PopulationService;
import io.javalin.Javalin;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * HTTP endpoints for population reports R24–R26.
 *
 * R24 – /reports/population/regions
 * R25 – /reports/population/countries
 * R26 – /reports/population/world
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
        // R24 – regions
        app.get("/reports/population/regions", ctx -> {
            List<PopulationRow> rows = populationService.getRegionPopulationInOutCities();
            ctx.contentType("text/csv");
            ctx.result(buildPopulationCsv(rows));
        });

        // R25 – countries
        app.get("/reports/population/countries", ctx -> {
            List<PopulationRow> rows = populationService.getCountryPopulationInOutCities();
            ctx.contentType("text/csv");
            ctx.result(buildPopulationCsv(rows));
        });

        // R26 – world population
        app.get("/reports/population/world", ctx -> {
            long worldPopulation = populationService.getWorldPopulation();
            ctx.contentType("text/csv");
            ctx.result(buildWorldCsv(worldPopulation));
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
