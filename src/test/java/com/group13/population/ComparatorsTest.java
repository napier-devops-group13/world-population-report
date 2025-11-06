package com.group13.population;

import com.group13.population.repo.CountryReport;
import com.group13.population.util.Comparators;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComparatorsTest {

    @Test
    void sortsByPopulationDescThenNameAsc() {
        List<CountryReport> rows = new ArrayList<>();
        rows.add(new CountryReport()); rows.get(0).setCode("AAA"); rows.get(0).setName("Beta"); rows.get(0).setPopulation(100);
        rows.add(new CountryReport()); rows.get(1).setCode("BBB"); rows.get(1).setName("Alpha"); rows.get(1).setPopulation(100);
        rows.add(new CountryReport()); rows.get(2).setCode("CCC"); rows.get(2).setName("Zeta"); rows.get(2).setPopulation(200);

        rows.sort(Comparators.COUNTRY_BY_POP_DESC_NAME_ASC);

        // Highest population first, then among ties Alpha before Beta
        assertEquals("CCC", rows.get(0).getCode());
        assertEquals("BBB", rows.get(1).getCode());
        assertEquals("AAA", rows.get(2).getCode());
    }
}
