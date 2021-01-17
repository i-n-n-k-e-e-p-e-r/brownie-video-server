package org.brownie.server.db;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class DBConnectionProviderTest {
    private static final String DB_FILE_NAME = "testBrownieDB.db";
    private static DBConnectionProvider provider;

    @BeforeAll
    @Test
    static void setup() {
        assertDoesNotThrow(() -> { provider = new DBConnectionProvider(DB_FILE_NAME); });
    }

    @Test
    public void testDB() {
        assertNotNull(provider);
        assertNotNull(provider.getDbFile());
        assertTrue(provider.createDataBase());
        assertTrue(provider.getDbFile().exists());
        assertDoesNotThrow(() -> provider.initDataTables());
        assertNotNull(provider.getOrmDaos());
        assertNotNull(provider.getConnectionSource());
    }

    @AfterAll
    @Test
    static void clear() {
        assertNotNull(provider.getConnectionSource());
        assertDoesNotThrow(() -> provider.getConnectionSource().close());
        assertNotNull(provider.getDbFile());
        assertTrue(provider.getDbFile().exists());
        assertTrue(provider.getDbFile().delete());
        assertFalse(provider.getDbFile().exists());
    }
}

