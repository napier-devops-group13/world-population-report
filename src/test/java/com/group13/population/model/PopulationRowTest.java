package com.group13.population.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopulationRowTest {

    @Test
    void fromTotalsComputesNonCityAndPercentages() {
        PopulationRow row = PopulationRow.fromTotals("TestRegion", 1000, 250);

        assertEquals("TestRegion", row.getName());
        assertEquals(1000L, row.getTotalPopulation());
        assertEquals(250L, row.getCityPopulation());
        assertEquals(750L, row.getNonCityPopulation());

        assertEquals(25.0, row.getCityPopulationPercent(), 0.0001);
        assertEquals(75.0, row.getNonCityPopulationPercent(), 0.0001);
    }

    @Test
    void fromTotalsHandlesZeroTotalPopulation() {
        PopulationRow row = PopulationRow.fromTotals("Empty", 0, 0);

        assertEquals(0L, row.getTotalPopulation());
        assertEquals(0L, row.getCityPopulation());
        assertEquals(0L, row.getNonCityPopulation());
        assertEquals(0.0, row.getCityPopulationPercent(), 0.0001);
        assertEquals(0.0, row.getNonCityPopulationPercent(), 0.0001);
    }

    @Test
    void fromTotalsClampsNegativeValuesToZero() {
        PopulationRow row = PopulationRow.fromTotals("Negative", -10, -5);

        assertEquals(0L, row.getTotalPopulation());
        assertEquals(0L, row.getCityPopulation());
        assertEquals(0L, row.getNonCityPopulation());
        assertEquals(0.0, row.getCityPopulationPercent(), 0.0001);
        assertEquals(0.0, row.getNonCityPopulationPercent(), 0.0001);
    }
}
