package com.niebo.quemus.integration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niebo.quemus.controllers.AuthController;
import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.services.GameService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
class GameIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Mock
    private AuthController authController;
    @Autowired
    private GameService gameService;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void gameCreationTest() throws Exception {
        when(authController.getSpotifySession("FAKE_SPOTIFY_TOKEN"))
                .thenReturn(Map.of("loggedIn", true));

        mvc.perform(post("/api/game/create")
                .cookie(new Cookie("spotify_access_token", "FAKE_SPOTIFY_TOKEN"))
                .param("isOnline", "false")
                .param("turns", "3")
                .param("deviceId", "device123"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                "online": false,
                "turnsLeft": 3,
                "host_device_ID":"device123"
                }
            """, JsonCompareMode.LENIENT));
    }
    
    @Test
    void gameCreationTestWithoutCookie() throws Exception{
        mvc.perform(post("/api/game/create")
                .param("isOnline", "false")
                .param("turns", "3")
                .param("deviceId", "device123"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }
    @Test
    void getGameTest() throws Exception {
        when(authController.getSpotifySession("FAKE_TOKEN"))
            .thenReturn(Map.of("loggedIn", true));

        Game game = gameService.createNewGame(
                true,
                5,
                "FAKE_TOKEN",
                "device123"
        );
        long id = game.getGame_ID();
        mvc.perform(get("/api/game")
            .param("game_ID", String.valueOf(id)))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                    "online": true,
                    "turnsLeft": 5,
                    "host_device_ID":"device123"
                    }
                """, JsonCompareMode.LENIENT));
    }
    @Test 
    void getWrongGameTest() throws Exception {
        when(authController.getSpotifySession("FAKE_TOKEN"))
            .thenReturn(Map.of("loggedIn", true));
        gameService.createNewGame(
                true,
                5,
                "FAKE_TOKEN",
                "device123"
        );
        mvc.perform(get("/api/game")
            .param("game_ID", String.valueOf(1)))
            .andExpect(status().is(404));
    }

    @Test
    void setOnlineTest() throws Exception {
        Game game = gameService.createNewGame(false, 5, "FAKE_TOKEN", "device123");
        long id = game.getGame_ID();

        mvc.perform(put("/api/game/online")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(id)))
                .andExpect(status().isOk());

        Game updated = gameService.getGameById(id);
        assertTrue(updated.isOnline());
    }

    @Test
    void setOfflineTest() throws Exception {
        Game game = gameService.createNewGame(true, 5, "FAKE_TOKEN", "device123");
        long id = game.getGame_ID();

        mvc.perform(put("/api/game/online")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(id)))
                .andExpect(status().isOk());

        Game updated = gameService.getGameById(id);
        assertTrue(!updated.isOnline());
    }

    @Test
    void joinGameTest() throws Exception {
        Game game = gameService.createNewGame(false, 5, "FAKE_TOKEN", "device123");
        long gameId = game.getGame_ID();

        MvcResult result = mvc.perform(get("/api/game/join")
                        .param("game_ID", String.valueOf(gameId))
                        .param("name", "Kuba")
                        .param("deviceId", "dev-xyz"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        Game returned = objectMapper.readValue(json, Game.class);

        assertFalse(returned.getPlayers().isEmpty());
        assertTrue(returned.getPlayers().stream()
                .anyMatch(p -> p.getName().equals("Kuba")));

        Game updated = gameService.getGameById(gameId);
        assertEquals(1, updated.getPlayers().size());
    }

    @Test
    void removePlayerTest() throws Exception {
        Game game = gameService.createNewGame(false, 5, "FAKE_TOKEN", "device123");
        long gameId = game.getGame_ID();

        gameService.joinGame(gameId, "XYZ", "dev-xyz");
        assertEquals(1, game.getPlayers().size());

        mvc.perform(delete("/api/game/remove/player")
                .param("game_ID", String.valueOf(gameId))
                .param("name", "XYZ"))
            .andExpect(status().isOk());

        Game updated = gameService.getGameById(gameId);
        assertTrue(updated.getPlayers().isEmpty());
    }
}
