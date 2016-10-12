package io.ecx.examples.directory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Directory listing example.
 * More changes
 *
 * @author Paul Hounslow
 */
public class Directory  implements JSONAware {
    private static final String SPACES = "                                        ";
    private static final String ROOT_NODE_NAME = "dir";
    private final String dir;
    private final Vector<MyFile> files;

    /**
     * Command line utility to list the files in a directory.
     *
     * <p>
     * Usage: java Directory [<i>-l</i>] [<i>directory</i>]
     * </p>
     * <p>
     * Where:
     * </p>
     * <ul>
     * <li><i>-l</i> prints the long form (default is the short)</li>
     * <li><i>directory</i> prints the contents of the directory (the default is
     * the current directory).</li>
     * </ul>
     *
     * @param args
     *            command line arguments @throws Exception @throws
     */
    public static void main(String[] args) throws Exception {
        boolean doLongPrint = false;
        Directory d;

        try {
            if (args.length > 0) {
                // Got some arguments
                if ("-l".equalsIgnoreCase(args[0])) {
                    doLongPrint = true;

                    if (args.length > 1) {
                        d = new Directory(args[1]);
                    } else {
                        d = new Directory();
                    }
                } else {
                    d = new Directory(args[0]);
                }
            } else {
                d = new Directory();
            }

            if (doLongPrint) {
                d.printLong(System.out);
            } else {
                d.printShort(System.out);
            }
            d.printXML(System.out);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Constructor for default operation (current directory).
     */
    public Directory() {
        this(".");
    }

    /**
     * Constructor for a specific directory.
     *
     * @param name
     *            the name of the directory to list.
     */
    public Directory(String name) {
        Path path = new File(name).toPath().toAbsolutePath().normalize();
        File[] list = path.toFile().listFiles();

        if (list == null) {
            throw new NullPointerException("No such directory: " + path);
        }

        files = new Vector<>(list.length);
        for (File file : list) {
            files.addElement(new MyFile(file));
        }
        dir = path.toString();
    }

    /**
     * Constructor to load an existing directory listing from an XML object.
     *
     * @param doc the XML DOM Object to load.
     * @throws ParserConfigurationException if there is a parser issue.
     * @throws IOException if there is an I/O issue.
     * @throws SAXException if there is a parser failure.
     */
    public Directory(Document doc) throws ParserConfigurationException, IOException, SAXException {
        Path tempDir = null;
        files = new Vector<>();

        // Basic check of document node.
        if (doc.hasChildNodes() && !doc.hasAttributes()) {

            // Check root node.
            Node rootNode = doc.getFirstChild();
            if (ROOT_NODE_NAME.equals(rootNode.getNodeName()) && rootNode.hasChildNodes() && rootNode.hasAttributes()
                    && rootNode.getAttributes().getLength() == 1) {
                tempDir = new File(rootNode.getAttributes().getNamedItem("path").getNodeValue()).toPath()
                        .toAbsolutePath().normalize();

                NodeList fileNodes = rootNode.getChildNodes();

                for (int i = 0; i < fileNodes.getLength(); i++) {
                    try {
                        files.addElement(new MyFile(fileNodes.item(i)));
                    } catch (SAXException e) {
                    }
                }
            }
        }
        dir = tempDir.toString();
    }

    /**
     * Constructor to load an existing directory listing from a JSON object.
     *
     * @param obj the JSON Object to load.
     */
    public Directory(JSONObject obj) {
        dir = (String)obj.get("dir");
        files = new Vector<>();

        JSONArray list = (JSONArray)obj.get("files");
        for(Object o : list) {
            try {
                files.add(new MyFile((JSONObject)o));
            } catch (ParseException e) {
            }
        }
    }

    /**
     * Print the short form of the directory listing.
     *
     * @param out
     *            the destination for the directory listing.
     */
    public void printShort(PrintStream out) {
        out.print(dir + ": ");

        for (int i = 0; i < files.size(); i++) {
            if (i > 0) {
                out.print(", ");
            }
            out.print(files.elementAt(i).getName());
        }
        out.println();
    }

    /**
     * Print the long form of the directory listing.
     *
     * @param out
     *            the destination for the directory listing.
     */
    public void printLong(PrintStream out) {
        String name;
        out.println(dir);

        for (int i = 0; i < files.size(); i++) {
            name = files.elementAt(i).getName();

            out.print(name);
            if (name.length() < SPACES.length()) {
                out.print(SPACES.substring(name.length()));
            }
            out.println(files.elementAt(i).length());
        }
        out.println();
    }

    /**
     * Print the XML document for this directory.
     *
     * @param out
     *            the destination for the XML text.
     * @throws ParserConfigurationException
     *             when there is a problem with the XML.
     * @throws TransformerException
     *             if there is a problem creating the text.
     */
    public void printXML(OutputStream out) throws ParserConfigurationException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(getDoc());
        StreamResult result = new StreamResult(out);

        transformer.transform(source, result);
    }

    /**
     * Create the XML document object for this directory.
     *
     * @return an XML document
     * @throws ParserConfigurationException
     *             when there is a problem with the XML.
     */
    Document getDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();

        // Add the root element.
        Element element = doc.createElement(ROOT_NODE_NAME);
        element.setAttribute("path", dir);
        doc.appendChild(element);

        for (MyFile file : files) {
            // Create an element for each entry.
            element.appendChild(createEntry(doc, file));
        }

        return doc;
    }
    @SuppressWarnings("unchecked")
    public JSONObject getJSON() {
        JSONObject obj = new JSONObject();

        obj.put("dir", dir);

        JSONArray list = new JSONArray();
        list.addAll(files);
        obj.put("files", list);

        return obj;
    }

    @Override
    public String toJSONString() {
        return getJSON().toJSONString();
    }

    public void printJSON(PrintStream output) throws IOException {
        output.print(toJSONString());
    }

    /*
     * Create an XML element for a file.
     */
    private Element createEntry(Document doc, MyFile file) {
        Element element;
        String name = "unknown";

        if (file.isDirectory()) {
            name = "directory";
        } else if (file.isFile()) {
            name = "file";
        }
        element = doc.createElement(name);
        element.setAttribute("name", file.getName());

        // Create elements for the file attributes.
        if (file.isFile()) {
            // Size is only used for files
            element.appendChild(createEntryElement(doc, "size", Long.toString(file.length())));
        }
        element.appendChild(createEntryElement(doc, "hidden", Boolean.toString(file.isHidden())));

        return element;
    }

    /*
     * Create an element for a file attribute.
     */
    private Node createEntryElement(Document doc, String name, String content) {
        Element element = doc.createElement(name);

        element.setTextContent(content);

        return element;
    }

    private class MyFile implements JSONAware {
        private final boolean isDirectory;
        private final boolean isHidden;
        private final long size;
        private final String name;

        MyFile(File file) {
            isDirectory = file.isDirectory();
            isHidden = file.isHidden();
            size = file.length();
            name = file.getName();
        }

        public MyFile(Node node) throws SAXException {
            NamedNodeMap attributes;
            NodeList subNodes;
            Long tempSize = 0L;
            Boolean tempHidden = false;
            String tempName = null;

            if ("directory".equals(node.getNodeName())) {
                isDirectory = true;
            } else if ("file".equals(node.getNodeName())) {
                isDirectory = false;
            } else {
                throw new SAXException(
                        "Unknown element, expected \"directory\" or \"file\", got \"" + node.getNodeName() + "\"");
            }

            if (node.hasChildNodes() && node.hasAttributes()) {
                attributes = node.getAttributes();
                if (1 == attributes.getLength()) {
                    tempName = node.getAttributes().getNamedItem("name").getNodeValue();
                    if (tempName.isEmpty()) {
                        throw new SAXException("Element " + node.getNodeName() + ", does not have a name!");
                    } else {
                        subNodes = node.getChildNodes();

                        tempHidden = false;
                        tempSize = 0L;
                        for (int i = 0; i < subNodes.getLength(); i++) {
                            if ("hidden".equals(subNodes.item(i).getNodeName())) {
                                tempHidden = Boolean.parseBoolean(subNodes.item(i).getTextContent());
                            } else if (!isDirectory && "size".equals(subNodes.item(i).getNodeName())) {
                                tempSize = Long.parseLong(subNodes.item(i).getTextContent());
                            }
                        }
                    }
                }
            }
            isHidden = tempHidden;
            size = tempSize;
            name = tempName;

        }

        public MyFile(JSONObject obj) throws ParseException {
            name = (String)obj.get("name");
            String type = (String)obj.get("type");

            if ("directory".equals(type)) {
                isDirectory = true;
                size = 0L;
            } else if ("file".equals(type)) {
                isDirectory = false;
                size = (long)obj.get("size");
            } else {
                throw new ParseException(ParseException.ERROR_UNEXPECTED_TOKEN);
            }
            isHidden = (boolean)obj.get("hidden");
        }

        public boolean isHidden() {
            return isHidden;
        }

        public boolean isFile() {
            return !isDirectory;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        public long length() {
            return size;
        }

        public String getName() {
            return name;
        }

        @SuppressWarnings("unchecked")
        @Override
        public String toJSONString() {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("hidden", isHidden);
            if (!isDirectory) {
                obj.put("type", "file");
                obj.put("size", size);
            } else {
                obj.put("type", "directory");
            }
            return obj.toJSONString();
        }
    }
}
