package com.group13.population.web;

import com.group13.population.repo.FakeCountryRepo;
import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service-level test verifying that the world report
 * is ordered by population DESC.
 */
class CountryReportsOrderingTest {

    private static WorldRepo.CountryRow row(
        String code,
        String name,
        String continent,
        String region,
        long population,
        String capital
    ) {
        return new WorldRepo.CountryRow(
            code, name, continent, region, population, capital
        );
    }

    @Test
    void worldReportIsDescByPopulation() {
        // Seed data deliberately out of order
        List<WorldRepo.CountryRow> seed = List.of(
            row("A", "A", "X", "Y", 5L,  "cA"),
            row("B", "B", "X", "Y", 15L, "cB"),
            row("C", "C", "X", "Y", 10L, "cC")
        );

        CountryService service = new CountryService(new FakeCountryRepo(seed));
        List<WorldRepo.CountryRow> all = service.allCountriesWorld();

        assertEquals(3, all.size());
        assertTrue(all.get(0).population() >= all.get(1).population());
        assertTrue(all.get(1).population() >= all.get(2).population());
    }
}
