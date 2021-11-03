package dev.ssdd.rtdb.exceptions;
public class InvalidServerHandshakeException extends RuntimeException {
	public InvalidServerHandshakeException(String message) {
		super(message);
	}
}
