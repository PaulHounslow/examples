package io.ecx.examples.client;

import org.json.simple.JSONObject;

import io.ecx.examples.directory.Directory;

public class CommandResponse implements Response {

    private final COMMAND_TYPE type;

    public CommandResponse(JSONObject input) throws ResponseException {
        String cmd = (String)input.get("command");


        if("bye".equalsIgnoreCase(cmd)) {
            type = COMMAND_TYPE.BYE;
        } else if("hello".equalsIgnoreCase(cmd)) {
            type = COMMAND_TYPE.HELLO;
        } else {
            throw new ResponseException("Unknown command: " + cmd);
        }
    }

    @Override
    public RESPONSE_TYPE getType() {
        return Response.RESPONSE_TYPE.COMMAND;
    }

    @Override
    public COMMAND_TYPE getCommandType() {
        return type;
    }

    @Override
    public Directory getDirectory() throws ResponseException {
        throw new ResponseException("Not appropriate for command!");
    }

    @Override
    public ResponseException getException() throws ResponseException {
        throw new ResponseException("Not appropriate for command!");
    }

}
