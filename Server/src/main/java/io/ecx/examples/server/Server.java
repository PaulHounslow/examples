package io.ecx.examples.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.ecx.examples.directory.Directory;

/**
 * A Simple directory server to demonstrate networking and JSON.
 * <p>
 * The server sends a directory listing when requested by a client.
 * </p>
 * <p>
 * All communication is done using JSON.
 * </p>
 * <p>
 * The messages that may be received from the client are:
 * </p>
 * <ol>
 * <li>command: <i>&lt;cmd&gt;</i></li>
 * <li>directory: <i>&lt;dir&gt;</i></li>
 * <ol>
 *
 * <p>
 * Where:
 * </p>
 * <p>
 * <i>&lt;cmd&gt;</i> is one of:
 * </p>
 * <ol>
 * <li>bye - end the communication.</li>
 * </ol>
 * <p>
 * <i>&lt;dir&gt;</i> is the directory to list.
 * </p>
 *
 * <p>
 * The server may send the following to the client:
 * </p>
 * <ol>
 * <li>command: <i>&lt;cmd&gt;</i></li>
 * <li>dir: <i>&lt;directory&gt;</i></li>
 * <li>exception: <i>&lt;message&gt;</i></li>
 * <ol>
 *
 * <p>
 * Where:
 * </p>
 * <p>
 * <i>&lt;cmd&gt;</i> is one of:
 * </p>
 * <ol>
 * <li>hello - sent in response to a connection being opened. Once the client receives the 'hello' command it knows it
 * can send messages to the server.</li>
 * <li>bye - sent in response to a 'bye' command from the client before the connection is closed.</li>
 * </ol>
 * <p>
 * <i>&lt;directory&gt;</i> is the directory listing.
 * </p>
 * <p>
 * <i>&lt;message&gt;</i> is the error message.
 * </p>
 *
 * @author Paul Hounslow
 *
 */
public class Server extends Thread {
    public static final int PORT_NUM = 9999;
    private final ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        new Server();
    }

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT_NUM);
    }

    public void run() {
        Socket clientSocket;
        try {
            boolean quit = false;
            clientSocket = serverSocket.accept();
            PrintStream out = new PrintStream(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            JSONObject input;
            JSONParser parser = new JSONParser();

            out.println(command2JSON("hello"));

            do {
                String inputStr = in.readLine();
                System.out.println("Server received: " + inputStr);

                input = (JSONObject) parser.parse(inputStr);

                if (input.containsKey("command")) {
                    String command = (String) input.get("command");

                    if ("bye".equalsIgnoreCase(command)) {
                        quit = true;
                        out.print(command2JSON("bye"));
                    }
                } else if (input.containsKey("directory")) {
                    try {
                        Directory dir = new Directory((String) input.get("directory"));
                        JSONObject obj = new JSONObject();

                        obj.put("directory", dir);
                        out.print(obj.toJSONString());
                    } catch (NullPointerException e) {
                        out.print(exception2JSON(e));
                    }
                }
                out.println();
                out.flush();
            } while (!quit);

            out.close();
            serverSocket.close();

        } catch (IOException e) {
        } catch (ParseException e1) {
        } finally {
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject exception2JSON(Exception e) {
        JSONObject obj = new JSONObject();

        obj.put("exception", e.getMessage());

        return obj;
    }

    @SuppressWarnings("unchecked")
    private JSONObject command2JSON(String cmd) {
        JSONObject obj = new JSONObject();

        obj.put("command", cmd);

        return obj;
    }
}
