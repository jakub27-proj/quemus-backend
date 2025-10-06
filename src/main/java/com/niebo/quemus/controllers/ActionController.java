package com.niebo.quemus.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.models.spotify.Song;
import com.niebo.quemus.services.GameService;

@RestController
public class ActionController {
    @Autowired
    private GameService gameService;

    @GetMapping("/action/start")
    public ResponseEntity<Game> startGame(@RequestParam(name = "playlist_ID") String playlist_ID,
     @RequestParam(name = "game_ID") long game_ID){
       return ResponseEntity.ok(gameService.startGame(game_ID, playlist_ID));
    }

    @GetMapping("/action/setsong")
    public Song setSongToGuess(@RequestParam(name = "game_ID") long game_ID){
      return gameService.setNextSongToGuess(game_ID);
    }

    @GetMapping("/action/check/song")
    public boolean checkSongPlacement(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "player_ID") long player_ID,
    @RequestParam(name = "index") int index){
      return gameService.checkIfSongGuessIsCorrect(game_ID, player_ID, index);
    }

    @GetMapping("/action/check/title")
    public boolean checkTitleGuess(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "player_ID") long player_ID,
    @RequestParam(name = "title") String title){
      return gameService.checkIfTitleGuessIsCorrect(game_ID, title, player_ID);
    }

    @GetMapping("/action/check/artist")
    public boolean checkArtistGuess(@RequestParam(name = "game_ID") long game_ID, @RequestParam(name = "player_ID") long player_ID,
    @RequestParam(name = "name") String name){
      return gameService.checkIfArtistNameGuessIsCorrect(game_ID, name, player_ID);
    }
}
