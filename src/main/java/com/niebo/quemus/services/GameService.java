package com.niebo.quemus.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.niebo.quemus.models.game.Game;

@Service
public class GameService {
    private Map<Long, Game> activeGames = new HashMap<>();

    public boolean startGame(long game_ID, String playlist_ID){
        Game currentGame = activeGames.get(game_ID);
        return true;
    }
}
