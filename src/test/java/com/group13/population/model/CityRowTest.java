package com.group13.population.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CityRow value object.
 *
 * These tests exercise the constructor and all getters so that
 * JaCoCo reports very high coverage for the CityRow model class.
 */
@DisplayName("CityRow – constructor and getter tests")
class CityRowTest {

    @Test
    @DisplayName("Constructor sets all fields and getters return them")
    void constructorAndGettersReturnValues() {
        CityRow row = new CityRow(
            "Kabul",
            "Afghanistan",
            "Kabol",
            1_780_000
        );

        assertAll(
            () -> assertEquals("Kabul", row.getName()),
            () -> assertEquals("Afghanistan", row.getCountry()),
            () -> assertEquals("Kabol", row.getDistrict()),
            () -> assertEquals(1_780_000, row.getPopulation())
        );
    }

    @Test
    @DisplayName("CityRow allows zero population")
    void allowsZeroPopulation() {
        CityRow row = new CityRow("GhostTown", "Nowhere", "Nowhere", 0);
        assertEquals(0, row.getPopulation());
    }
}
