package com.group13.population.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CityReport}.
 *
 * <p>This class is a simple DTO used by the web/API layer for city reports
 * (R07–R16). We verify that the constructor stores values correctly and that
 * getters return them unchanged.</p>
 *
 * <p>Fields match the coursework city report specification:
 * Name, Country, District, Population.</p>
 */
public class CityReportTest {

    @Test
    @DisplayName("Constructor stores all fields and getters return them")
    void constructorStoresFields() {
        CityReport report = new CityReport(
            "Yangon",
            "Myanmar",
            "Yangon",
            4477638L
        );

        assertEquals("Yangon", report.getName());
        assertEquals("Myanmar", report.getCountry());
        assertEquals("Yangon", report.getDistrict());
        assertEquals(4477638L, report.getPopulation());
    }
}
