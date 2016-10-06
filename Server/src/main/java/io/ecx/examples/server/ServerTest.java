package io.ecx.examples.server;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerTest {

	private static final String TEMP_DIR = "temp";
	private static final String FILE_PREFIX = "file_";
	private static final String DIR_PREFIX = "dir_";
	private static final int NUM_FILES = 10;
	private static final int NUM_DIRS = 10;
	private static final File dir = new File(TEMP_DIR);
	private static final String RESULT_JSON = "server_dir.json";

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testServer() throws Exception {
		Server server = new Server();

		server.start();

		Socket socket = new Socket("localhost", Server.PORT_NUM);
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		String fromServer;

		assertNotNull(fromServer = in.readLine());
		assertEquals("{\"command\":\"hello\"}", fromServer);

		out.println("{\"directory\":\"test\"}\"");
		assertNotNull(fromServer = in.readLine());
		assertEquals("{\"exception\":java.lang.NullPointerException: No such directory: /development/examples/Server/test}", fromServer);

		out.println("{\"directory\":\"temp\"}\"");
		assertNotNull(fromServer = in.readLine());
		String wanted = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource(RESULT_JSON).getFile()), StandardCharsets.UTF_8);
		assertEquals(wanted, fromServer);

		out.println("{\"command\":\"bye\"}");
		assertNotNull(fromServer = in.readLine());
		assertEquals("{\"command\":\"bye\"}", fromServer);
		
		socket.close();
		
		assertFalse(server.isAlive());
	}
}
