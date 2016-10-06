package io.ecx.examples.client;

import org.json.simple.JSONObject;

import io.ecx.examples.directory.Directory;

public class ExceptionResponse implements Response {

    private final ResponseException exception;

    public ExceptionResponse(JSONObject input) {
        exception = new ResponseException((String)input.get("exception"));
    }

    @Override
    public RESPONSE_TYPE getType() {
        return Response.RESPONSE_TYPE.EXCEPTION;
    }

    @Override
    public COMMAND_TYPE getCommandType() throws ResponseException {
        throw new ResponseException("Not appropriate for exception!");
    }

    @Override
    public Directory getDirectory() throws ResponseException {
        throw new ResponseException("Not appropriate for exception!");
    }

    @Override
    public ResponseException getException() {
        return exception;
    }
}
