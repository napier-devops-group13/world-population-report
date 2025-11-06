package com.group13.population.util;

import com.group13.population.repo.CountryReport;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/** Common comparators used in tests and repo tie-breaks. */
public final class Comparators {

    private Comparators() {
        // utility
    }

    /** Population DESC, then Name ASC (English collation). */
    public static final Comparator<CountryReport> COUNTRY_BY_POP_DESC_NAME_ASC =
        Comparator
            .comparingLong(CountryReport::getPopulation)
            .reversed()
            .thenComparing(CountryReport::getName, Collator.getInstance(Locale.ENGLISH));
}
