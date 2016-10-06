package io.ecx.examples.client;

import io.ecx.examples.directory.Directory;

public interface Response {

    enum RESPONSE_TYPE {COMMAND, DIRECTORY, EXCEPTION};

    enum COMMAND_TYPE {HELLO, BYE};

    RESPONSE_TYPE getType();

    COMMAND_TYPE getCommandType() throws ResponseException;

    Directory getDirectory() throws ResponseException;

    ResponseException getException() throws ResponseException;

}
