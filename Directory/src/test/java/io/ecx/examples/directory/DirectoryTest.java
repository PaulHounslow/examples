package io.ecx.examples.directory;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.ecx.examples.directory.Directory;

public class DirectoryTest {
	private static final String TEMP_DIR = "temp";
	private static final String FILE_PREFIX = "file_";
	private static final String DIR_PREFIX = "dir_";
	private static final int NUM_FILES = 10;
	private static final int NUM_DIRS = 10;
	private static final String RESULT_SHORT = "/development/examples/Directory/temp: dir_0, dir_1, dir_2, dir_3, dir_4, dir_5, dir_6, dir_7, dir_8, dir_9, file_0, file_1, file_2, file_3, file_4, file_5, file_6, file_7, file_8, file_9\n";
	private static final String RESULT_LONG_FILE_NAME = "result_long.txt";
	private static final String RESULT_XML = "directory.xml";
	private static final String RESULT_JSON = "directory.json";

	private static final File dir = new File(TEMP_DIR);

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
	public void testPrintShort() throws IOException {
		Directory directory = new Directory(dir.getAbsolutePath());
		StringWriter strW = new StringWriter();
		PrintStream output = new PrintStream(new WriterOutputStream(strW, "ASCII"));

		directory.printShort(output);

		output.close();
		assertEquals(RESULT_SHORT, strW.toString());
	}

	@Test
	public void testPrintLong() throws Exception {
		Directory directory = new Directory(dir.getAbsolutePath());
		StringWriter strW = new StringWriter();
		PrintStream output = new PrintStream(new WriterOutputStream(strW, "ASCII"));

		String wanted = FileUtils.readFileToString(
				new File(getClass().getClassLoader().getResource(RESULT_LONG_FILE_NAME).getFile()),
				StandardCharsets.UTF_8);

		directory.printLong(output);

		output.close();
		assertEquals(wanted, strW.toString());
	}

	@Test
	public void testGetDoc() throws Exception {
		Directory directory = new Directory(dir.getAbsolutePath());
		Node node;

		Document doc = directory.getDoc();

		// Basic check of document node.
		assertTrue(doc.hasChildNodes());
		assertFalse(doc.hasAttributes());

		// Check root node.
		node = doc.getFirstChild();
		assertEquals("dir", node.getNodeName());
		assertTrue(node.hasChildNodes());
		assertTrue(node.hasAttributes());
		assertEquals(1, node.getAttributes().getLength());
		assertEquals("/development/examples/Directory/temp", node.getAttributes().getNamedItem("path").getNodeValue());

		// Check entries that are directories.
		NodeList dirs = doc.getElementsByTagName("directory");
		assertEquals(NUM_DIRS, dirs.getLength());

		// Check entries that are files.
		NodeList files = doc.getElementsByTagName("file");
		assertEquals(NUM_FILES, files.getLength());

		NamedNodeMap attributes;
		NodeList nodes;

		for (int i = 0; i < dirs.getLength(); i++) {
			node = dirs.item(i);
			// Check the name attribute.
			assertTrue(node.hasAttributes());
			attributes = node.getAttributes();
			assertEquals(1, attributes.getLength());
			assertEquals(DIR_PREFIX + i, node.getAttributes().getNamedItem("name").getNodeValue());
			// Check the hidden element.
			assertTrue(node.hasChildNodes());
			nodes = node.getChildNodes();
			assertEquals(1, nodes.getLength());
			assertEquals("hidden", nodes.item(0).getNodeName());
			assertEquals("false", nodes.item(0).getTextContent());
		}

		String name;
		for (int i = 0; i < files.getLength(); i++) {
			node = files.item(i);
			// Check the name attribute.
			assertTrue(node.hasAttributes());
			attributes = node.getAttributes();
			assertEquals(1, attributes.getLength());
			assertEquals(FILE_PREFIX + i, node.getAttributes().getNamedItem("name").getNodeValue());
			// Check the hidden element.
			assertTrue(node.hasChildNodes());
			nodes = node.getChildNodes();
			assertEquals(2, nodes.getLength());
			for (int j = 0; j < nodes.getLength(); j++) {
				name = nodes.item(j).getNodeName();
				if (name.equals("size")) {
					assertEquals("0", nodes.item(j).getTextContent());
				} else if (name.equals("hidden")) {
					assertEquals("false", nodes.item(j).getTextContent());
				} else {
					fail("Unknown element in file: " + name);
				}
			}
		}
	}

	@Test
	public void testPrintXML() throws Exception {
		Directory directory = new Directory(dir.getAbsolutePath());
		StringWriter strW = new StringWriter();
		PrintStream output = new PrintStream(new WriterOutputStream(strW, "ASCII"));

		String wanted = FileUtils.readFileToString(
				new File(getClass().getClassLoader().getResource(RESULT_XML).getFile()), StandardCharsets.UTF_8);

		directory.printXML(output);

		output.close();
		assertEquals(wanted, strW.toString());
	}

	@Test
	public void testCreateFromXML() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Directory directory = new Directory(db.parse(new File(getClass().getClassLoader().getResource(RESULT_XML).getFile())));
		Node node;

		Document doc = directory.getDoc();

		// Basic check of document node.
		assertTrue(doc.hasChildNodes());
		assertFalse(doc.hasAttributes());

		// Check root node.
		node = doc.getFirstChild();
		assertEquals("dir", node.getNodeName());
		assertTrue(node.hasChildNodes());
		assertTrue(node.hasAttributes());
		assertEquals(1, node.getAttributes().getLength());
		assertEquals("/development/examples/Directory/temp", node.getAttributes().getNamedItem("path").getNodeValue());

		// Check entries that are directories.
		NodeList dirs = doc.getElementsByTagName("directory");
		assertEquals(NUM_DIRS, dirs.getLength());

		// Check entries that are files.
		NodeList files = doc.getElementsByTagName("file");
		assertEquals(NUM_FILES, files.getLength());

		NamedNodeMap attributes;
		NodeList nodes;

		for (int i = 0; i < dirs.getLength(); i++) {
			node = dirs.item(i);
			// Check the name attribute.
			assertTrue(node.hasAttributes());
			attributes = node.getAttributes();
			assertEquals(1, attributes.getLength());
			assertEquals(DIR_PREFIX + i, node.getAttributes().getNamedItem("name").getNodeValue());
			// Check the hidden element.
			assertTrue(node.hasChildNodes());
			nodes = node.getChildNodes();
			assertEquals(1, nodes.getLength());
			assertEquals("hidden", nodes.item(0).getNodeName());
			assertEquals("false", nodes.item(0).getTextContent());
		}

		String name;
		for (int i = 0; i < files.getLength(); i++) {
			node = files.item(i);
			// Check the name attribute.
			assertTrue(node.hasAttributes());
			attributes = node.getAttributes();
			assertEquals(1, attributes.getLength());
			assertEquals(FILE_PREFIX + i, node.getAttributes().getNamedItem("name").getNodeValue());
			// Check the hidden element.
			assertTrue(node.hasChildNodes());
			nodes = node.getChildNodes();
			assertEquals(2, nodes.getLength());
			for (int j = 0; j < nodes.getLength(); j++) {
				name = nodes.item(j).getNodeName();
				if (name.equals("size")) {
					assertEquals("0", nodes.item(j).getTextContent());
				} else if (name.equals("hidden")) {
					assertEquals("false", nodes.item(j).getTextContent());
				} else {
					fail("Unknown element in file: " + name);
				}
			}
		}
	}

	@Test
	public void testPrintJSON() throws Exception {
		Directory directory = new Directory(dir.getAbsolutePath());
		StringWriter strW = new StringWriter();
		PrintStream output = new PrintStream(new WriterOutputStream(strW, "ASCII"));

		String wanted = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource(RESULT_JSON).getFile()), StandardCharsets.UTF_8);

		directory.printJSON(output);

		output.close();
		assertEquals(wanted, strW.toString());
	}

	@Test
	public void testCreateFromJSON() throws Exception {
		JSONParser parser = new JSONParser();
		Directory directory = new Directory((JSONObject)parser.parse(new FileReader(getClass().getClassLoader().getResource(RESULT_JSON).getFile())));
		Node node;

		Document doc = directory.getDoc();

		// Basic check of document node.
		assertTrue(doc.hasChildNodes());
		assertFalse(doc.hasAttributes());

		// Check root node.
		node = doc.getFirstChild();
		assertEquals("dir", node.getNodeName());
		assertTrue(node.hasChildNodes());
		assertTrue(node.hasAttributes());
		assertEquals(1, node.getAttributes().getLength());
		assertEquals("/development/examples/Directory/temp", node.getAttributes().getNamedItem("path").getNodeValue());

		// Check entries that are directories.
		NodeList dirs = doc.getElementsByTagName("directory");
		assertEquals(NUM_DIRS, dirs.getLength());

		// Check entries that are files.
		NodeList files = doc.getElementsByTagName("file");
		assertEquals(NUM_FILES, files.getLength());

		NamedNodeMap attributes;
		NodeList nodes;

		for (int i = 0; i < dirs.getLength(); i++) {
			node = dirs.item(i);
			// Check the name attribute.
			assertTrue(node.hasAttributes());
			attributes = node.getAttributes();
			assertEquals(1, attributes.getLength());
			assertEquals(DIR_PREFIX + i, node.getAttributes().getNamedItem("name").getNodeValue());
			// Check the hidden element.
			assertTrue(node.hasChildNodes());
			nodes = node.getChildNodes();
			assertEquals(1, nodes.getLength());
			assertEquals("hidden", nodes.item(0).getNodeName());
			assertEquals("false", nodes.item(0).getTextContent());
		}

		String name;
		for (int i = 0; i < files.getLength(); i++) {
			node = files.item(i);
			// Check the name attribute.
			assertTrue(node.hasAttributes());
			attributes = node.getAttributes();
			assertEquals(1, attributes.getLength());
			assertEquals(FILE_PREFIX + i, node.getAttributes().getNamedItem("name").getNodeValue());
			// Check the hidden element.
			assertTrue(node.hasChildNodes());
			nodes = node.getChildNodes();
			assertEquals(2, nodes.getLength());
			for (int j = 0; j < nodes.getLength(); j++) {
				name = nodes.item(j).getNodeName();
				if (name.equals("size")) {
					assertEquals("0", nodes.item(j).getTextContent());
				} else if (name.equals("hidden")) {
					assertEquals("false", nodes.item(j).getTextContent());
				} else {
					fail("Unknown element in file: " + name);
				}
			}
		}
	}
}