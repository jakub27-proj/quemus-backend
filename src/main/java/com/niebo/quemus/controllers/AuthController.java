package com.niebo.quemus.controllers;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


@RequestMapping("/api/auth")
@RestController
@Tag(name="Auth Controller", description = "Login to spotify Account")
@Slf4j
public class AuthController {
    @Value("${spotify.client-id}")
    private String clientId;
    @Value("${spotify.redirect-uri}")
    private String redirectUri;
    @Value("${spotify.scopes:user-read-private,user-read-email,playlist-read-private,playlist-read-collaborative}")
    private String scopes;

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String authUrl = UriComponentsBuilder
                .fromUriString("https://accounts.spotify.com/authorize")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scopes)
                .build()
                .toUriString();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authUrl))
                .build();
    }
    @GetMapping("/session")
    public ResponseEntity<Map<String, Boolean>> getSpotifySession(@CookieValue(value = "spotify_access_token", required = false) String accessToken) {
        boolean loggedIn = accessToken != null;
        log.info("Logged in: " + loggedIn);
        return ResponseEntity.ok(Map.of("loggedIn", loggedIn));
    }
}
