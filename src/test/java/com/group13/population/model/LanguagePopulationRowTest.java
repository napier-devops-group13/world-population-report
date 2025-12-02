package com.group13.population.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LanguagePopulationRowTest {

    @Test
    void fromWorldTotalComputesPercentageCorrectly() {
        // 2,000 out of 8,000 = 25%
        LanguagePopulationRow row =
            LanguagePopulationRow.fromWorldTotal("English", 2000L, 8000L);

        assertEquals("English", row.getLanguage());
        assertEquals(2000L, row.getSpeakers());
        assertEquals(25.0, row.getWorldPopulationPercent(), 0.0001);
    }

    @Test
    void fromWorldTotalHandlesZeroWorldPopulation() {
        LanguagePopulationRow row =
            LanguagePopulationRow.fromWorldTotal("Spanish", 1000L, 0L);

        assertEquals("Spanish", row.getLanguage());
        assertEquals(1000L, row.getSpeakers());
        // No division possible → 0%
        assertEquals(0.0, row.getWorldPopulationPercent(), 0.0001);
    }

    @Test
    void fromWorldTotalClampsNegativeSpeakersToZero() {
        LanguagePopulationRow row =
            LanguagePopulationRow.fromWorldTotal("Hindi", -50L, 1000L);

        assertEquals("Hindi", row.getLanguage());
        assertEquals(0L, row.getSpeakers());
        assertEquals(0.0, row.getWorldPopulationPercent(), 0.0001);
    }

    @Test
    void fromWorldTotalReplacesNullOrBlankLanguageWithUnknown() {
        LanguagePopulationRow nullLanguage =
            LanguagePopulationRow.fromWorldTotal(null, 500L, 1000L);
        assertEquals("unknown", nullLanguage.getLanguage());

        LanguagePopulationRow blankLanguage =
            LanguagePopulationRow.fromWorldTotal("   ", 500L, 1000L);
        assertEquals("unknown", blankLanguage.getLanguage());
    }
}
