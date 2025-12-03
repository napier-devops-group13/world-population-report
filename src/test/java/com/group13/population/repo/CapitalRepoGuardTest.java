package com.group13.population.repo;

import com.group13.population.db.Db;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CapitalRepo basic structure / guard tests")
class CapitalRepoGuardTest {

    @Test
    @DisplayName("CapitalRepo has a constructor that accepts a Db")
    void capitalRepoHasDbConstructor() {
        Constructor<?>[] constructors = CapitalRepo.class.getConstructors();

        boolean hasDbCtor = Arrays.stream(constructors)
            .anyMatch(ctor -> {
                Class<?>[] params = ctor.getParameterTypes();
                return params.length == 1 && params[0].equals(Db.class);
            });

        assertTrue(
            hasDbCtor,
            "Expected CapitalRepo to have a constructor CapitalRepo(Db db)"
        );
    }

    @Test
    @DisplayName("CapitalRepo constructor rejects null Db")
    void capitalRepoConstructorRejectsNullDb() {
        assertThrows(
            NullPointerException.class,
            () -> new CapitalRepo(null),
            "CapitalRepo(Db) should throw NPE when passed a null Db"
        );
    }
}
