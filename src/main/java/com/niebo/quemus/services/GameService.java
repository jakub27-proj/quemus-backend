package com.niebo.quemus.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.models.spotify.Playlist;
import com.niebo.quemus.models.spotify.Song;

@Service
public class GameService {
    private final Map<Long, Game> activeGames = new HashMap<>();
    private static long id = 0;
    @Autowired
    private WebClient webClient;

    public Game getGameById(long game_ID){
        return activeGames.get(game_ID);
    }

    public Game createNewGame(boolean isOnline, int turnsLeft){
        return activeGames.put(id, new Game(isOnline, id++, turnsLeft));
    }

    public Game joinGame(long player_ID, long game_ID,String name){
        Game currentGame = activeGames.get(game_ID);
        currentGame.addPlayer(player_ID, name);
        return currentGame;
    }

    public Game startGame(long game_ID, String playlist_ID, String accessToken){
        Game currentGame = activeGames.get(game_ID);
        currentGame.setPlaylist(webClient.get().uri("/playlists/" + playlist_ID).header("Authorization", "Bearer " + accessToken)
        .retrieve().bodyToMono(Playlist.class).block());
        currentGame.setCurrentPlayersTurn();
        currentGame.setTurnsLeft(currentGame.getTurnsLeft() * currentGame.getPlayers().size());
        if(currentGame.getPlaylist().getTracks().getItems().size() < currentGame.getTurnsLeft()){
            currentGame.setTurnsLeft((int) (currentGame.getPlaylist().getTracks().getItems().size() / 4));
        }
        currentGame.startGame();
        currentGame.setFirstSongForPlayers();
        return currentGame;
    }

    public Song setNextSongToGuess(long game_ID){
       return this.activeGames.get(game_ID).newCurrentSong();
    }

    public boolean checkIfSongGuessIsCorrect(long game_ID, long player_ID, int index){
        Game currentGame = activeGames.get(game_ID);
        return currentGame.checkDatesOfCreation(index, currentGame.findPlayerByID(player_ID));
    }

    public boolean checkIfArtistNameGuessIsCorrect(long game_ID, String artistName, long player_ID){
        Game currentGame = activeGames.get(game_ID);
        return currentGame.checkArtistName(artistName, player_ID);
    }

    public boolean checkIfTitleGuessIsCorrect(long game_ID, String title, long player_ID){
        Game currentGame = activeGames.get(game_ID); 
        return currentGame.checkTitle(title, player_ID);
    }
}
