package io.ecx.examples.client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ResponseFactory {

    public static Response getResponse(String inputStr) throws ParseException, ResponseException {
        Response response;
        JSONObject input;
        JSONParser parser = new JSONParser();
        input = (JSONObject) parser.parse(inputStr);

        if(input.containsKey("command")) {
            response = new CommandResponse(input);
        } else if(input.containsKey("directory")) {
            response = new DirectoryResponse(input);
        } else if(input.containsKey("exception")) {
            response = new ExceptionResponse(input);
        } else {
            throw new ParseException(ParseException.ERROR_UNEXPECTED_TOKEN, input);
        }

        return response;
    }

}
