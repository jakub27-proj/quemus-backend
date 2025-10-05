package com.niebo.quemus.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.models.game.Player;

@Service
public class GameService {
    private Map<Long, Game> activeGames = new HashMap<>();
    private static long id = 0;

    public Game createNewGame(boolean isOnline, int turnsLeft){
        return activeGames.put(id, new Game(isOnline, id, turnsLeft));
    }

    public Game joinGame(Player player, long game_ID){
        Game currentGame = activeGames.get(game_ID);
        currentGame.addPlayer(player);
        return currentGame;
    }

    public Game startGame(long game_ID, String playlist_ID){
        Game currentGame = activeGames.get(game_ID);
        //Tu musi być setowanie playlisty jeszcze
        currentGame.setCurrentPlayersTurn();
        currentGame.startGame();
        currentGame.setFirstSongForPlayers();
        return currentGame;
    }


}
