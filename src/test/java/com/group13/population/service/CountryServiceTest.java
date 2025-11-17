package com.group13.population.service;

import com.group13.population.repo.WorldRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for CountryService.
 * Uses a fake WorldRepo so no real DB is touched.
 */
class CountryServiceTest {

    private FakeWorldRepo fakeRepo;
    private CountryService service;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeWorldRepo();
        service = new CountryService(fakeRepo);
    }

    /* ================== R01–R03: all countries ================== */

    @Test
    void allCountriesWorldDelegatesToRepo() {
        List<WorldRepo.CountryRow> result = service.allCountriesWorld();

        assertTrue(fakeRepo.allCountriesWorldCalled);
        assertNotNull(result);
    }

    @Test
    void allCountriesContinentTrimsWhitespace() {
        service.allCountriesContinent("  Asia  ");

        assertEquals("Asia", fakeRepo.continentForAll);
    }

    @Test
    void allCountriesContinentTurnsNullIntoEmptyString() {
        service.allCountriesContinent(null);

        assertEquals("", fakeRepo.continentForAll);
    }

    @Test
    void allCountriesRegionTrimsWhitespace() {
        service.allCountriesRegion("  Eastern Asia  ");

        assertEquals("Eastern Asia", fakeRepo.regionForAll);
    }

    @Test
    void allCountriesRegionTurnsNullIntoEmptyString() {
        service.allCountriesRegion(null);

        assertEquals("", fakeRepo.regionForAll);
    }

    /* ================== R04–R06: top-N lists ================== */

    @Test
    void topCountriesWorldPassesNWhenPositive() {
        service.topCountriesWorld(5);

        assertEquals(5, fakeRepo.nWorld);
    }

    @Test
    void topCountriesWorldTreatsZeroAndNegativeAsOne() {
        service.topCountriesWorld(0);
        assertEquals(1, fakeRepo.nWorld);

        service.topCountriesWorld(-10);
        assertEquals(1, fakeRepo.nWorld);
    }

    @Test
    void topCountriesContinentTrimsAndPassesArguments() {
        service.topCountriesContinent("  Asia  ", 3);

        assertEquals("Asia", fakeRepo.continentForTop);
        assertEquals(3, fakeRepo.nTopContinent);
    }

    @Test
    void topCountriesContinentTreatsZeroAndNegativeAsOne() {
        service.topCountriesContinent("Asia", 0);
        assertEquals(1, fakeRepo.nTopContinent);

        service.topCountriesContinent("Asia", -5);
        assertEquals(1, fakeRepo.nTopContinent);
    }

    @Test
    void topCountriesRegionTrimsAndPassesArguments() {
        service.topCountriesRegion("  Eastern Asia  ", 4);

        assertEquals("Eastern Asia", fakeRepo.regionForTop);
        assertEquals(4, fakeRepo.nTopRegion);
    }

    @Test
    void topCountriesRegionTreatsZeroAndNegativeAsOne() {
        service.topCountriesRegion("Eastern Asia", 0);
        assertEquals(1, fakeRepo.nTopRegion);

        service.topCountriesRegion("Eastern Asia", -2);
        assertEquals(1, fakeRepo.nTopRegion);
    }

    /**
     * Simple fake implementation of WorldRepo used only for testing.
     * It records the arguments passed by CountryService and returns
     * empty lists (we don't care about DB data here).
     */
    private static class FakeWorldRepo extends WorldRepo {

        boolean allCountriesWorldCalled;

        String continentForAll;
        String regionForAll;

        Integer nWorld;

        String continentForTop;
        Integer nTopContinent;

        String regionForTop;
        Integer nTopRegion;

        @Override
        public java.util.List<CountryRow> allCountriesWorld() {
            allCountriesWorldCalled = true;
            return List.of();
        }

        @Override
        public java.util.List<CountryRow> allCountriesContinent(String continent) {
            this.continentForAll = continent;
            return List.of();
        }

        @Override
        public java.util.List<CountryRow> allCountriesRegion(String region) {
            this.regionForAll = region;
            return List.of();
        }

        @Override
        public java.util.List<CountryRow> topCountriesWorld(int n) {
            this.nWorld = n;
            return List.of();
        }

        @Override
        public java.util.List<CountryRow> topCountriesContinent(String continent, int n) {
            this.continentForTop = continent;
            this.nTopContinent = n;
            return List.of();
        }

        @Override
        public java.util.List<CountryRow> topCountriesRegion(String region, int n) {
            this.regionForTop = region;
            this.nTopRegion = n;
            return List.of();
        }
    }
}
