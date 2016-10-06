package io.ecx.examples.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.parser.ParseException;

import io.ecx.examples.directory.Directory;

/**
 * A Simple directory client to demonstrate networking and JSON.
 * <p>
 * The client requests a directory listing from a server.
 * </p>
 * <p>
 * All communication is done using JSON.
 * </p>
 * <p>
 * The messages that may be received from the server are:
 * </p>
 * <ol>
 * <li>command: <i>&lt;cmd&gt;</i></li>
 * <li>dir <i>&lt;directory&gt;</i> the directory listing.</li>
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
 * <li>hello - communication has been established and the server is ready to receive commands.</li>
 * <li>bye - the communication has ended and the socket will be closed. Received in response the a 'bye' command sent by
 * the client.</li>
 * </ol>
 * <p>
 * <i>&lt;directory&gt;</i> is the directory listing.
 * </p>
 * <p>
 * <i>&lt;message&gt;</i> is the error message.
 * </p>
 *
 * <p>
 * The client may send the following to the server:
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
 * <li>hello - sent in response to a connection being opened. Once the client receives the 'hello' command it knows it
 * can send messages to the server.</li>
 * <li>bye - sent in response to a 'bye' command from the client before the connection is closed.</li>
 * </ol>
 * <p>
 * <i>&lt;dir&gt;</i> is the directory to list.
 * </p>
 *
 * @author Paul Hounslow
 *
 */
public class Client {
    public static final int PORT_NUM = 9999;
    private final String path;

    public static void main(String[] args) throws IOException, ResponseException {
        if(args.length == 1) {
            Client client = new Client(args[0]);
            client.getDirectory().printLong(System.out);
        } else {
            System.err.println("Client\nUsage: Client <directory>");
        }
    }

    /**
     * Constructor.
     *
     * @param path the path of the directory to be listed on the server.
     */
    public Client(String path) {
        this.path = path;
    }

    /**
     * Get the directory specified in the constructor from the server.
     * @return the directory
     * @throws ResponseException if there was a problem.
     */
    public Directory getDirectory() throws ResponseException {
        Directory directory = null;
        ResponseException exception = null;

        try (
                Socket socket = new Socket("localhost", PORT_NUM);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ) {
            boolean done = false;
            Response response;

            do {
                String inputStr = in.readLine();
                System.out.println("Client received: " + inputStr);

                response = ResponseFactory.getResponse(inputStr);

                switch (response.getType()) {
                case COMMAND:
                    switch (response.getCommandType()) {
                    case HELLO:
                        out.println("{\"directory\":\"" + path + "\"}");
                        break;
                    case BYE:
                        done = true;
                        break;
                    default:
                        throw new ResponseException();
                    }
                    break;
                case DIRECTORY:
                    directory = response.getDirectory();
                    out.println("{\"command\":\"bye\"}");
                    break;
                case EXCEPTION:
                    exception = response.getException();
                    out.println("{\"command\":\"bye\"}");
                    break;
                default:
                    throw new ResponseException();
                }
            } while (!done);
        } catch (IOException e) {
            throw new ResponseException(e);
        } catch (ParseException e) {
            throw new ResponseException(e);
        }

        if(exception != null) {
            throw exception;
        }
        return directory;
    }
}
