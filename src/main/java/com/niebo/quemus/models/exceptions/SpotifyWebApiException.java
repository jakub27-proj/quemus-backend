package com.niebo.quemus.models.exceptions;

public class SpotifyWebApiException extends RuntimeException {
    public SpotifyWebApiException(String message) {
        super(message);
    }

    public SpotifyWebApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

