package com.niebo.quemus.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Spotify Callback Controller", description = "Endpoints for Spotify OAuth2 login and token handling")
public class SpotifyCallbackController {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    @Value("${frontend_url}")
    private String frontendUrl;

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code) throws IOException, InterruptedException {
        var body = "grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=" + redirectUri +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var client = HttpClient.newHttpClient();
        var tokenResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (tokenResponse.statusCode() != 200) {
            throw new RuntimeException("Błąd pobierania tokena Spotify: " + tokenResponse.body());
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(
            tokenResponse.body(),
             new TypeReference<>() {}
        );
        String accessToken = (String) map.get("access_token");
        String refreshToken = (String) map.get("refresh_token");

        ResponseCookie accessCookie = ResponseCookie.from("spotify_access_token", accessToken)
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(3600)
        .sameSite("Lax")
        .build();

        ResponseCookie refreshCookie = ResponseCookie.from("spotify_refresh_token", refreshToken)
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(3600) 
        .sameSite("Lax")
        .build();

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .location(URI.create(frontendUrl))
            .build();
        }
}
