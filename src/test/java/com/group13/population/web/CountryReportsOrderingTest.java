package com.group13.population.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group13.population.repo.FakeCountryRepo;
import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies the world report is DESC by population via the service layer. */
public class CountryReportsOrderingTest {

    private static WorldRepo.CountryRow row(String code, String name, String cont,
                                            String region, long pop, String cap) {
        return new WorldRepo.CountryRow(code, name, cont, region, pop, cap);
    }

    @Test
    void world_endpoint_is_desc_by_population() {
        var seed = List.of(
            row("A", "A", "X", "Y", 5,  "cA"),
            row("B", "B", "X", "Y", 15, "cB"),
            row("C", "C", "X", "Y", 10, "cC")
        );

        var service = new CountryService(new FakeCountryRepo(seed));
        var all = service.allCountriesWorld();

        assertEquals(3, all.size());
        assertTrue(all.get(0).population >= all.get(1).population);
        assertTrue(all.get(1).population >= all.get(2).population);
    }
}
