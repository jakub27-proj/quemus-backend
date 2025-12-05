package com.niebo.quemus.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SpotifyService {
    @Value("${spotify.client-id}")
    private String clientId;
    @Value("${spotify.redirect-uri}")
    private String redirectUri;
    @Value("${spotify.scopes:user-read-private,user-read-email,playlist-read-private,playlist-read-collaborative,user-modify-playback-state,user-read-playback-state,streaming}")
    private String scopes;
    @Value("${spotify.client-secret}")
    private String clientSecret;

    public boolean isTokenValid(String accessToken) throws IOException, InterruptedException {
        if (accessToken == null) return false;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.spotify.com/v1/me"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }
    public String refreshAccessToken(String refreshToken) throws IOException, InterruptedException {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("User is not logged in");
        }

        String body = "grant_type=refresh_token" +
                "&refresh_token=" + refreshToken +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Spotify refresh errror: " + response.body());

            if (response.body().contains("invalid_grant")) {
                throw new RuntimeException("Token Expired");
            }
            throw new RuntimeException("Unable to refresh token.");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(response.body(), new TypeReference<>() {});

        return (String) map.get("access_token");
    }

    public String buildUriAuthorize(){
        return UriComponentsBuilder
            .fromUriString("https://accounts.spotify.com/authorize")
            .queryParam("client_id", clientId)
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", scopes)
            .build()
            .toUriString();
    }
}
