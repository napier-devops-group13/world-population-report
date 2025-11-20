package com.group13.population.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CountryReport}.
 *
 * <p>This class is a simple DTO used by the web/API layer, so we mainly
 * verify that the constructor stores values correctly and that getters
 * return them unchanged (including when capital is null).</p>
 */
public class CountryReportTest {

    @Test
    @DisplayName("Constructor stores all fields and getters return them")
    void constructorStoresFields() {
        CountryReport report = new CountryReport(
            "GBR",
            "United Kingdom",
            "Europe",
            "British Islands",
            59623400L,
            "London"
        );

        assertEquals("GBR", report.getCode());
        assertEquals("United Kingdom", report.getName());
        assertEquals("Europe", report.getContinent());
        assertEquals("British Islands", report.getRegion());
        assertEquals(59623400L, report.getPopulation());
        assertEquals("London", report.getCapital());
    }

    @Test
    @DisplayName("Capital may be null in CountryReport")
    void allowsNullCapital() {
        CountryReport report = new CountryReport(
            "ATA",
            "Antarctica",
            "Antarctica",
            "Antarctica",
            0L,
            null
        );

        assertNull(report.getCapital(), "Capital should be allowed to be null");
    }
}
