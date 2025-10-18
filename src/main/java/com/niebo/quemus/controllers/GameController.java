package com.niebo.quemus.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    @RequestParam(name = "turns") int turns){
        return gameService.createNewGame(isOnline, turns);
    }

    @GetMapping("/join")
    public Game joinGame(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "name") String name){
        return gameService.joinGame(game_ID, name);
    }

    @GetMapping("/start")
    public Game startGame(@RequestParam(name = "game_ID") long game_ID){
        return gameService.startGame(game_ID);
    }

    @PostMapping("/setplaylist")
    public Game setPlaylistGame(@RequestParam(name = "playlist_ID") String playlist_ID,
    @RequestParam(name = "game_ID") long game_ID, @CookieValue(name="spotify_access_token") String token){
        return gameService.setPlaylist(game_ID, playlist_ID, token);
    }

    @GetMapping("/resetplaylist")
    public Game resetPlaylist(@RequestParam long game_ID) {
        return this.gameService.resetPlaylist(game_ID);
    }
    
    
}
