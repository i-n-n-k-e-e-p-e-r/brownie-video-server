package org.brownie.server.db;

import org.brownie.server.security.SecurityFunctions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private static DBConnectionProvider provider;

    @BeforeAll
    @Test
    static void setup() {
        assertDoesNotThrow(() -> {
            provider = new DBConnectionProvider(DBConnectionProviderTest.DB_FILE_NAME);
        });
    }

    @Test
    public void testSetRandom() {
        User user = new User();
        user.setRandom("Random");
        assertEquals("Random", user.getRandom());
    }

    @Test
    public void testSetName() {
        User user = new User();
        User actualSetNameResult = user.setName("Name");
        assertSame(user, actualSetNameResult);
        assertEquals("Name", actualSetNameResult.getName());
    }

    @Test
    public void testSetPasswordHash() {
        User user = new User();
        User actualSetPasswordHashResult = user.setPasswordHash("iloveyou");
        assertSame(user, actualSetPasswordHashResult);
        assertEquals("iloveyou", actualSetPasswordHashResult.getPasswordHash());
    }

    @Test
    public void testSetGroup() {
        User user = new User();
        User actualSetGroupResult = user.setGroup(1);
        assertSame(user, actualSetGroupResult);
        assertEquals(1, actualSetGroupResult.getGroup().intValue());
    }

    @Test
    public void testUserEdit() {
        assertEquals(0, User.getAdminsCount(provider));

        String password = "1234567890";
        String salt = "1234567890";
        String saltedHash = SecurityFunctions.getSaltedPasswordHash(password, salt);
        User user = new User("ADMIN",
                saltedHash,
                salt,
                User.GROUP.ADMIN.ordinal());
        assertDoesNotThrow(() -> user.updateUserDBData(provider));

        user.setName("admin");
        assertDoesNotThrow(() -> user.updateUserDBData(provider));
        assertEquals(1, User.getAdminsCount(provider));

        user.deleteUserFromDB(provider);
        assertEquals(0, User.getAdminsCount(provider));
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