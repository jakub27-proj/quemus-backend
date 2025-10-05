package com.niebo.quemus.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.models.game.Player;
import com.niebo.quemus.models.spotify.Song;

@Service
public class GameService {
    private final Map<Long, Game> activeGames = new HashMap<>();
    private static long id = 0;

    public Game createNewGame(boolean isOnline, int turnsLeft){
        return activeGames.put(id, new Game(isOnline, id++, turnsLeft));
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

    public Song setNextSongToGuess(long game_ID){
       return this.activeGames.get(game_ID).newCurrentSong();
    }

    public boolean checkIfGuessIsCorrect(long game_ID, long player_ID, int index){
        Game currentGame = activeGames.get(game_ID);
        return currentGame.checkGuess(index, player_ID);
    }

}
