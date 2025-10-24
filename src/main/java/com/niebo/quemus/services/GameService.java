package com.niebo.quemus.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.models.spotify.Playlist;
import com.niebo.quemus.models.spotify.PlaylistTrackObject;
import com.niebo.quemus.models.spotify.Tracks;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GameService {
    private final Map<Long, Game> activeGames = new HashMap<>();
    private static final AtomicLong gameID = new AtomicLong(0);
    private static final AtomicLong playerID = new AtomicLong(0);
    @Autowired
    private WebClient webClient;

    public Game getGameById(long game_ID){
        return activeGames.get(game_ID);
    }

    public Game createNewGame(boolean isOnline, int turnsLeft){
        Game game = new Game(isOnline, gameID.get(), turnsLeft);
        log.info(game.toString());
        activeGames.put(gameID.getAndAdd(1), game);
        return game;
    }

    public Game joinGame(long game_ID, String name){
        Game currentGame = activeGames.get(game_ID);
        log.info("Player:" + name);
        currentGame.addPlayer(playerID.getAndAdd(1), name);
        return currentGame;
    }

    public Game setPlaylist(long game_ID, String playlist_ID, String accessToken){
        Game currentGame = activeGames.get(game_ID);
        currentGame.setPlaylist(webClient.get().uri("/playlists/" + playlist_ID).header("Authorization", "Bearer " + accessToken)
        .retrieve().bodyToMono(Playlist.class).block());
        String next = currentGame.getPlaylist().getTracks().getNext();
        while(next != null) {
            next = next.substring("https://api.spotify.com/v1".length());
            log.info("Next: " + next);
            List<PlaylistTrackObject> items = currentGame.getPlaylist().getTracks().getItems();
            currentGame.getPlaylist().setTracks((webClient.get().uri(next).header("Authorization", "Bearer " + accessToken)
        .retrieve().bodyToMono(Tracks.class).block()));
            currentGame.getPlaylist().getTracks().getItems().addAll(items);
            next = currentGame.getPlaylist().getTracks().getNext();
        }
        log.info("Playlist: " + currentGame.getPlaylist().getName());
        return currentGame;
    }
    
    public Game startGame(long game_ID){
        Game currentGame = activeGames.get(game_ID);
        log.info("Game has started: " + game_ID);
        currentGame.setCurrentPlayersTurn();
        currentGame.setTurnsLeft(currentGame.getTurnsLeft() * currentGame.getPlayers().size());
        if(currentGame.getPlaylist().getTracks().getItems().size() < currentGame.getTurnsLeft()){
            currentGame.setTurnsLeft((int) (currentGame.getPlaylist().getTracks().getItems().size() / 4));
        }
        currentGame.startGame();
        currentGame.setFirstSongForPlayers();
        return currentGame;
    }

    public PlaylistTrackObject setNextSongToGuess(long game_ID){
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

    public Game resetPlaylist(long game_ID){
         Game currentGame = activeGames.get(game_ID);
         currentGame.setPlaylist(null);
         return currentGame;
    }
}
