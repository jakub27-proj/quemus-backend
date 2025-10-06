package com.niebo.quemus.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.services.GameService;

@RestController
public class GameController {
    @Autowired
    private GameService gameService;
    
    @GetMapping("/game") 
    public Game getGameByID(@RequestParam(name = "game_ID") long game_ID){
        return gameService.getGameById(game_ID);
    }

    @PostMapping("/game/create")
    public Game createGame(@RequestParam(name = "isOnline") boolean isOnline, 
    @RequestParam(name = "turns") int turns){
        return gameService.createNewGame(isOnline, turns);
    }

    @GetMapping("/game/join")
    public Game joinGame(@RequestParam(name = "player_ID") long player_ID, @RequestParam(name = "game_ID") long game_ID){
        return gameService.joinGame(player_ID, game_ID);
    }
}
