package org.brownie.server.providers;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrownieUploadsFileFactoryTest {

    @Test
    void testFactory() {
        BrownieUploadsFileFactory factory = new BrownieUploadsFileFactory();
        assertNotNull(factory);
        assertDoesNotThrow(() -> factory.createFile("test_1.txt"));

        try {
            assertNotNull(factory.createFile("test_2.txt"));
            assertNotNull(factory.getTempFiles());
            assertEquals(factory.getTempFiles().size(), 2);
        } catch (IOException e) {
            assertNull(e);
        } finally {
            if (factory.getTempFiles() != null) {
                factory.getTempFiles().forEach(f -> {
                    assertTrue(f.exists());
                    assertTrue(f.delete());
                    assertFalse(f.exists());
                });
                factory.getTempFiles().clear();
            }
        }
    }
}
