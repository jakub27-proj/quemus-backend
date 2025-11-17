package com.niebo.quemus.controllers;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.niebo.quemus.services.SpotifyService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/auth")
@RestController
@Tag(name="Auth Controller", description = "Login to spotify Account")
@Slf4j
public class AuthController {
    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(spotifyService.buildUriAuthorize()))
                .build();
    }
    @GetMapping("/session")
    public Map<String, Boolean> getSpotifySession(@CookieValue(value = "spotify_access_token", required = false) String accessToken) {
        boolean loggedIn = accessToken != null;
        log.info("Logged in: " + loggedIn);
        return Map.of("loggedIn", loggedIn);
    }
    
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(
        @CookieValue(value = "spotify_access_token", required = false) String accessToken,
        @CookieValue(value = "spotify_refresh_token", required = false) String refreshToken)
        throws IOException, InterruptedException {

    if (refreshToken == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    boolean valid = spotifyService.isTokenValid(accessToken);
    if (!valid) {
        log.info("Refreshing token...");
        accessToken = spotifyService.refreshAccessToken(refreshToken);
    }

    ResponseCookie accessCookie = ResponseCookie.from("spotify_access_token", accessToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(3600)
            .sameSite("Lax")
            .build();

    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .body(Map.of("access_token", accessToken));
    }

}
