package org.brownie.server.db;

import org.brownie.server.security.SecurityFunctions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserToFileStateTest {
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
        UserToFileState state = new UserToFileState();
        File testFile = new File("foo.txt");
        state.setFileName(testFile);
        assertEquals(testFile.getAbsolutePath(), state.getFileName());
    }

    @Test
    public void testSetUser() {
        UserToFileState state = new UserToFileState();
        User user = new User(1, "name", "random", "hash", User.GROUP.ADMIN.ordinal());
        state.setUser(user);
        assertSame(user, state.getUser());
    }

    @Test
    public void testSetPausedAt() {
        UserToFileState state = new UserToFileState();
        state.setPausedAt(14321);
        assertEquals(14321, state.getPausedAt());
    }

    @Test
    public void testsStateEntryManipulations() {
        // Tests user creation for foreign key
        assertEquals(0, User.getAdminsCount(provider));

        String password = "1234567890";
        String salt = "1234567890";
        String saltedHash = SecurityFunctions.getSaltedPasswordHash(password, salt);
        User user = new User("ADMIN",
                saltedHash,
                salt,
                User.GROUP.ADMIN.ordinal());
        assertDoesNotThrow(() -> user.updateUserDBData(provider));

        // Tests new state entre creation
        File testFile = new File("test_file.test");
        assertEquals(0, UserToFileState.getEntry(provider, testFile, user).size());
        final UserToFileState state = new UserToFileState(user, testFile);
        assertDoesNotThrow(() -> state.updateEntry(provider));
        List<UserToFileState> states = UserToFileState.getEntry(provider, testFile, user);
        assertEquals(1, states.size());
        assertEquals(state.getUser().getUserId(), states.iterator().next().getUser().getUserId());
        assertEquals(state.getFileName(), states.iterator().next().getFileName());
        assertEquals(state.getPausedAt(), states.iterator().next().getPausedAt());

        // Tests entry edit
        File newTestFile = new File("new_test_file.test");
        state.setFileName(newTestFile);
        assertDoesNotThrow(() -> state.updateEntry(provider));
        states = UserToFileState.getEntry(provider, newTestFile, user);
        assertEquals(1, states.size());
        assertEquals(state.getFileName(), states.iterator().next().getFileName());

        // Tests entry deletion
        state.deleteEntry(provider);
        assertEquals(0, UserToFileState.getEntry(provider, newTestFile, user).size());

        // Tests foreign key deleted
        UserToFileState newState = new UserToFileState(user, newTestFile);
        assertDoesNotThrow(() -> state.updateEntry(provider));
        assertThrows(SQLException.class, () -> newState.updateEntry(provider)); // Same user and file
        assertEquals(1, UserToFileState.getEntry(provider, newTestFile, user).size());
        newState.setFileName(testFile); // Setting different file name
        assertDoesNotThrow(() -> newState.updateEntry(provider));
        assertEquals(1, UserToFileState.getEntry(provider, testFile, user).size());
        assertEquals(1, UserToFileState.getEntry(provider, newTestFile, user).size());
        assertEquals(2, UserToFileState.getEntriesForUser(provider, user).size());
        assertEquals(1, UserToFileState.getEntriesForFile(provider, newTestFile).size());

        assertDoesNotThrow(() -> user.deleteUserFromDB(provider));
        assertEquals(0, User.getAdminsCount(provider));
        assertEquals(0, UserToFileState.getEntriesForUser(provider, user).size());
        assertEquals(0, UserToFileState.getEntriesForFile(provider, newTestFile).size());
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
