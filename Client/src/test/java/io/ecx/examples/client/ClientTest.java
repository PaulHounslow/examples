package io.ecx.examples.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ecx.examples.server.Server;

public class ClientTest {
    private static final String TEMP_DIR = "temp";
    private static final String FILE_PREFIX = "file_";
    private static final String DIR_PREFIX = "dir_";
    private static final int NUM_FILES = 10;
    private static final int NUM_DIRS = 10;
    private static final File dir = new File(TEMP_DIR);

    private Server server;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Create a temporary directory for the tests.
        dir.mkdir();
        dir.deleteOnExit();

        File temp;
        // Create the temporary files for the tests.
        for (int i = 0; i < NUM_FILES; i++) {
            temp = new File(dir, FILE_PREFIX + i);
            temp.createNewFile();
            temp.deleteOnExit();
        }

        // Create the temporary directories for the tests.
        for (int i = 0; i < NUM_DIRS; i++) {
            temp = new File(dir, DIR_PREFIX + i);
            temp.mkdir();
            temp.deleteOnExit();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        server = new Server();

        server.start();
    }

    @After
    public void tearDown() throws Exception {
        if(server.isAlive()) {
            server.join();
        }
    }

    @Test (expected = ResponseException.class)
    public void testUnknownDirectory() throws Exception {
        Client client = new Client("test");

        assertNull(client.getDirectory());
    }

    @Test //(expected = ResponseException.class)
    public void testTempDirectory() throws Exception {
        Client client = new Client("temp");

        assertNotNull(client.getDirectory());
    }

}
