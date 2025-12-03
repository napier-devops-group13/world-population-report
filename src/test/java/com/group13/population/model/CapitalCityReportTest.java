package com.group13.population.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CapitalCityReport}.
 *
 * <p>This class is a simple DTO used by the web/API layer for capital
 * city reports (R17–R22). We verify that the constructor stores values
 * correctly and that getters return them unchanged.</p>
 *
 * <p>Fields match the coursework capital city report specification:
 * Name, Country, Population.</p>
 */
public class CapitalCityReportTest {

    @Test
    @DisplayName("Constructor stores all fields and getters return them")
    void constructorStoresFields() {
        CapitalCityReport report = new CapitalCityReport(
            "London",
            "United Kingdom",
            7285000L
        );

        assertEquals("London", report.getName());
        assertEquals("United Kingdom", report.getCountry());
        assertEquals(7285000L, report.getPopulation());
    }
}
