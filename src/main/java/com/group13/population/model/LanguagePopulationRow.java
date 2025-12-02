package com.group13.population.model;

/**
 * Row model for language population report (R32).
 *
 * Used for:
 *  - Number of people who speak Chinese, English, Hindi, Spanish, and Arabic,
 *    ordered from greatest to smallest, including % of world population.
 */
public class LanguagePopulationRow {

    private final String language;
    private final long speakers;
    private final double worldPopulationPercent;

    private LanguagePopulationRow(String language,
                                  long speakers,
                                  double worldPopulationPercent) {
        this.language = language;
        this.speakers = speakers;
        this.worldPopulationPercent = worldPopulationPercent;
    }

    /**
     * Factory method that calculates the % of the world population.
     *
     * @param language         Language name (e.g. "Chinese").
     * @param speakers         Number of people who speak this language.
     * @param worldPopulation  Total world population used as the denominator.
     */
    public static LanguagePopulationRow fromWorldTotal(String language,
                                                       long speakers,
                                                       long worldPopulation) {
        if (language == null || language.isBlank()) {
            language = "unknown";
        }
        if (speakers < 0) {
            speakers = 0;
        }
        if (worldPopulation <= 0) {
            return new LanguagePopulationRow(language, speakers, 0.0);
        }

        double percent = (speakers * 100.0) / worldPopulation;
        return new LanguagePopulationRow(language, speakers, percent);
    }

    /** Language name (Chinese, English, Hindi, Spanish, Arabic). */
    public String getLanguage() {
        return language;
    }

    /** Number of speakers of this language. */
    public long getSpeakers() {
        return speakers;
    }

    /** Percentage of world population that speaks this language. */
    public double getWorldPopulationPercent() {
        return worldPopulationPercent;
    }
}
