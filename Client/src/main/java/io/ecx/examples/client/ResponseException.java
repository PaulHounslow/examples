package io.ecx.examples.client;

public class ResponseException extends Exception {

    private static final long serialVersionUID = 5906178887092133443L;

    public ResponseException(Exception e) {
        super(e);
    }

    public ResponseException() {
        super();
    }

    public ResponseException(String msg) {
        super(msg);
    }
}
