package com.group13.population.service;

import com.group13.population.model.CapitalCity;
import com.group13.population.repo.CapitalRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for {@link CapitalService}.
 * Uses a fake {@link CapitalRepo} so no real DB is touched.
 *
 * Covers the capital reports R17–R22.
 */
class CapitalServiceTest {

    private FakeCapitalRepo fakeRepo;
    private CapitalService service;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeCapitalRepo();
        service = new CapitalService(fakeRepo);
    }

    /* =============== R17–R19: all capitals ================= */

    @Test
    void allCapitalsWorldDelegatesToRepo() {
        List<CapitalCity> result = service.allCapitalsWorld();

        assertTrue(fakeRepo.allCapitalsWorldCalled,
            "Service should call repo.allCapitalsWorld()");
        assertNotNull(result, "Service should never return null");
    }

    @Test
    void allCapitalsContinentTrimsWhitespace() {
        service.allCapitalsContinent("  Asia  ");

        assertEquals("Asia", fakeRepo.continentForAll,
            "Service should trim continent before passing to repo");
    }

    @Test
    void allCapitalsContinentTurnsNullIntoEmptyString() {
        service.allCapitalsContinent(null);

        assertEquals("",
            fakeRepo.continentForAll,
            "Null continent should be normalised to empty string");
    }

    @Test
    void allCapitalsRegionTrimsWhitespace() {
        service.allCapitalsRegion("  Eastern Asia  ");

        assertEquals("Eastern Asia", fakeRepo.regionForAll,
            "Service should trim region before passing to repo");
    }

    @Test
    void allCapitalsRegionTurnsNullIntoEmptyString() {
        service.allCapitalsRegion(null);

        assertEquals("",
            fakeRepo.regionForAll,
            "Null region should be normalised to empty string");
    }

    /* =============== R20–R22: top-N capitals ================= */

    @Test
    void topCapitalsWorldPassesNWhenPositive() {
        service.topCapitalsWorld(5);

        assertEquals(5, fakeRepo.nWorld,
            "Positive n should be passed through unchanged");
    }

    @Test
    void topCapitalsWorldTreatsZeroAndNegativeAsOne() {
        service.topCapitalsWorld(0);
        assertEquals(1, fakeRepo.nWorld,
            "n = 0 should be normalised to 1");

        service.topCapitalsWorld(-10);
        assertEquals(1, fakeRepo.nWorld,
            "Negative n should also be normalised to 1");
    }

    @Test
    void topCapitalsContinentTrimsAndPassesArguments() {
        service.topCapitalsContinent("  Asia  ", 3);

        assertEquals("Asia", fakeRepo.continentForTop,
            "Continent should be trimmed before passing to repo");
        assertEquals(3, fakeRepo.nTopContinent,
            "Positive n should be passed through unchanged");
    }

    @Test
    void topCapitalsContinentTreatsZeroAndNegativeAsOne() {
        service.topCapitalsContinent("Asia", 0);
        assertEquals(1, fakeRepo.nTopContinent,
            "n = 0 should be normalised to 1");

        service.topCapitalsContinent("Asia", -5);
        assertEquals(1, fakeRepo.nTopContinent,
            "Negative n should also be normalised to 1");
    }

    @Test
    void topCapitalsRegionTrimsAndPassesArguments() {
        service.topCapitalsRegion("  Eastern Asia  ", 4);

        assertEquals("Eastern Asia", fakeRepo.regionForTop,
            "Region should be trimmed before passing to repo");
        assertEquals(4, fakeRepo.nTopRegion,
            "Positive n should be passed through unchanged");
    }

    @Test
    void topCapitalsRegionTreatsZeroAndNegativeAsOne() {
        service.topCapitalsRegion("Eastern Asia", 0);
        assertEquals(1, fakeRepo.nTopRegion,
            "n = 0 should be normalised to 1");

        service.topCapitalsRegion("Eastern Asia", -2);
        assertEquals(1, fakeRepo.nTopRegion,
            "Negative n should also be normalised to 1");
    }

    /**
     * Simple fake implementation of {@link CapitalRepo} used only for testing.
     * It records the arguments passed by {@link CapitalService} and always
     * returns empty lists (we don't need real data here).
     */
    private static class FakeCapitalRepo implements CapitalRepo {

        boolean allCapitalsWorldCalled;

        String continentForAll;
        String regionForAll;

        Integer nWorld;

        String continentForTop;
        Integer nTopContinent;

        String regionForTop;
        Integer nTopRegion;

        @Override
        public List<CapitalCity> allCapitalsWorld() {
            allCapitalsWorldCalled = true;
            return List.of();
        }

        @Override
        public List<CapitalCity> allCapitalsContinent(String continent) {
            this.continentForAll = continent;
            return List.of();
        }

        @Override
        public List<CapitalCity> allCapitalsRegion(String region) {
            this.regionForAll = region;
            return List.of();
        }

        @Override
        public List<CapitalCity> topCapitalsWorld(int n) {
            this.nWorld = n;
            return List.of();
        }

        @Override
        public List<CapitalCity> topCapitalsContinent(String continent, int n) {
            this.continentForTop = continent;
            this.nTopContinent = n;
            return List.of();
        }

        @Override
        public List<CapitalCity> topCapitalsRegion(String region, int n) {
            this.regionForTop = region;
            this.nTopRegion = n;
            return List.of();
        }
    }
}
