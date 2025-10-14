package com.niebo.quemus.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.services.GameService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/game")
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
    public Game joinGame(@RequestParam(name = "player_ID") long player_ID, @RequestParam(name = "game_ID") long game_ID,
    @RequestParam(name = "name") String name){
        return gameService.joinGame(player_ID, game_ID, name);
    }
}
