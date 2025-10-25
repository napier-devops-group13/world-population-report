package com.group13.population.repo;

import com.group13.population.db.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository for global aggregate queries.
 */
public class WorldRepo {

    /**
     * Returns the total world population (R26).
     * SQL is simple and index-friendly.
     */
    public long worldPopulation() throws SQLException {
        final String sql = "SELECT SUM(Population) FROM country";
        try (final Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}


