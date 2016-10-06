package io.ecx.examples.client;

import org.json.simple.JSONObject;

import io.ecx.examples.directory.Directory;

public class DirectoryResponse implements Response {

    private final Directory directory;

    public DirectoryResponse(JSONObject input) {
        directory = new Directory((JSONObject)input.get("directory"));
    }

    @Override
    public RESPONSE_TYPE getType() {
        return Response.RESPONSE_TYPE.DIRECTORY;
    }

    @Override
    public COMMAND_TYPE getCommandType() throws ResponseException {
        throw new ResponseException("Not appropriate for directory!");
    }

    @Override
    public Directory getDirectory() throws ResponseException {
        return directory;
    }

    @Override
    public ResponseException getException() throws ResponseException {
        throw new ResponseException("Not appropriate for directory!");
    }

}
