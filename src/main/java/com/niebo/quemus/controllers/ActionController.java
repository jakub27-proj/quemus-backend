package com.niebo.quemus.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.niebo.quemus.services.GameService;

public class ActionController {
    @Autowired
    private GameService gameService;
    @GetMapping("/action/start")
    public ResponseEntity<Boolean> startGame(@RequestParam(name = "playlist_ID") String playlist_ID,
     @RequestParam(name = "game_ID")long game_ID){
       return ResponseEntity.ok(gameService.startGame(game_ID, playlist_ID));
    }
}
