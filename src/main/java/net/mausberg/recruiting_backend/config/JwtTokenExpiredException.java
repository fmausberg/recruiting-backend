package net.mausberg.recruiting_backend.config;

public class JwtTokenExpiredException extends RuntimeException {

	public JwtTokenExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public JwtTokenExpiredException(String message) {
		super(message);
	}
}
