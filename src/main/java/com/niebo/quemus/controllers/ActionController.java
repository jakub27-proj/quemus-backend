package com.niebo.quemus.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.niebo.quemus.services.GameService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.models.game.GameAction;



@RestController
@RequestMapping("/api/action")
@Tag(name= "Action Controller", description= "Operations on started Game instances")
public class ActionController {
    @Autowired
    private GameService gameService;

    @GetMapping("")
    public List<GameAction> getGameActions(@RequestParam(name = "game_ID") long game_ID) {
        return gameService.getGameById(game_ID).getActions();
    }
    

    @GetMapping("/check/title")
    public boolean checkTitleGuess(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "player_ID") long player_ID,
    @RequestParam(name = "title") String title){
        return gameService.checkIfTitleGuessIsCorrect(game_ID, title, player_ID);
    }

    @GetMapping("/check/artist")
    public boolean checkArtistGuess(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "player_ID") long player_ID,
    @RequestParam(name = "name") String name){
        return gameService.checkIfArtistNameGuessIsCorrect(game_ID, name, player_ID);
    }

    @PutMapping("/add/action")
    public List<GameAction> addAction(@RequestBody GameAction action, @RequestParam(name = "game_ID") long game_ID) {
        return gameService.addAction(game_ID, action);
    }

    @GetMapping("/execute/action")
    public Game executeAction(@RequestParam(name = "game_ID") long game_ID) {
        return gameService.executeActions(game_ID);
    }

    @GetMapping("/use/token")
    public boolean useToken(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "player_ID") long player_ID) {
        return gameService.useToken(game_ID, player_ID);
    }
}
