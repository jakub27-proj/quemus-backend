package com.niebo.quemus.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.services.GameService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/api/game")
@Slf4j
@Tag(name = "Game Controller", description = "Operations on not started instances of Game and basic operations")
public class GameController {
    @Autowired
    private GameService gameService;
    
    @GetMapping("") 
    public Game getGameByID(@RequestParam(name = "game_ID") long game_ID){
        return gameService.getGameById(game_ID);
    }

    @PostMapping("/create")
    public Game createGame(@RequestParam(name = "isOnline") boolean isOnline, 
    @RequestParam(name = "turns") int turns, 
    @RequestParam(name="deviceId") String device_ID,
    @CookieValue(value = "spotify_access_token", required = false) String accessToken){
        return gameService.createNewGame(isOnline, turns, accessToken, device_ID);
    }

    @GetMapping("/join")
    public Game joinGame(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "name") String name, @RequestParam(name="deviceId") String device_ID){
        return gameService.joinGame(game_ID, name, device_ID);
    }

    @DeleteMapping("/remove/player")
    public void removePlayer(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "name") String name) {
        gameService.removePlayer(game_ID, name);
    }
    
    @GetMapping("/start")
    public Game startGame(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "points") int points){
        return gameService.startGame(game_ID, points);
    }

    @PostMapping("/setplaylist")
    public Game setPlaylistGame(@RequestParam(name = "playlist_ID") String playlist_ID,
    @RequestParam(name = "game_ID") long game_ID, @CookieValue(name="spotify_access_token") String token){
        return gameService.setPlaylist(game_ID, playlist_ID, token);
    }

    @DeleteMapping("/resetplaylist")
    public Game resetPlaylist(@RequestParam long game_ID) {
        return this.gameService.resetPlaylist(game_ID);
    }

    @DeleteMapping("")
    public void deleteGame(@RequestParam(name = "game_ID") long game_ID){
        gameService.deleteGame(game_ID);
    }
    
    @PutMapping("/online")
    public void setOnline(@RequestBody long game_ID) {
        gameService.setOnline(game_ID);
    }
    
}
