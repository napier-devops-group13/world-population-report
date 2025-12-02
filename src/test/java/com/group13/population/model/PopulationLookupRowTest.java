package com.group13.population.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopulationLookupRowTest {

    @Test
    void ofKeepsValidNameAndPopulation() {
        PopulationLookupRow row = PopulationLookupRow.of("Asia", 1000000L);

        assertEquals("Asia", row.getName());
        assertEquals(1000000L, row.getPopulation());
    }

    @Test
    void ofClampsNegativePopulationToZero() {
        PopulationLookupRow row = PopulationLookupRow.of("NegativeLand", -10L);

        assertEquals("NegativeLand", row.getName());
        assertEquals(0L, row.getPopulation());
    }

    @Test
    void ofReplacesNullOrBlankNameWithUnknown() {
        PopulationLookupRow nullName = PopulationLookupRow.of(null, 500L);
        assertEquals("unknown", nullName.getName());
        assertEquals(500L, nullName.getPopulation());

        PopulationLookupRow blankName = PopulationLookupRow.of("   ", 500L);
        assertEquals("unknown", blankName.getName());
        assertEquals(500L, blankName.getPopulation());
    }
}
