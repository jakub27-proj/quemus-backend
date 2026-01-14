package com.niebo.quemus.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.reactive.function.client.WebClient;

import com.niebo.quemus.controllers.AuthController;
import com.niebo.quemus.controllers.NotificationController;
import com.niebo.quemus.models.game.Game;
import com.niebo.quemus.models.game.GameAction;
import com.niebo.quemus.models.game.Notification;
import com.niebo.quemus.models.game.NotificationType;
import com.niebo.quemus.models.game.Player;
import com.niebo.quemus.models.spotify.Playlist;
import com.niebo.quemus.models.spotify.PlaylistTrackObject;
import com.niebo.quemus.models.spotify.Tracks;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GameService {
    private final Map<Long, Game> activeGames = new ConcurrentHashMap<>();
    private static final AtomicLong gameID = new AtomicLong(0);
    private static final AtomicLong playerID = new AtomicLong(0);
    @Autowired
    private WebClient webClient;
    @Autowired
    private NotificationController notificationController;
    @Autowired
    private AuthController authController;

    public Game getGameById(long game_ID){
        return activeGames.get(game_ID);
    }

    public Game createNewGame(boolean isOnline, int turnsLeft, 
    @CookieValue(value = "spotify_access_token", required = false) String accessToken){
        if(!authController.getSpotifySession(accessToken).get("loggedIn")) return null;
        Game game = new Game(isOnline, gameID.get(), turnsLeft);
        log.info(game.toString());
        activeGames.put(gameID.getAndAdd(1), game);
        return game;
    }

    public void setOnline(long game_ID){
        Game game = activeGames.get(game_ID);
        game.setOnline(true);
        notificationController.sendMessage(new Notification(NotificationType.REFRESH, ""), game_ID);
    }

    public Game joinGame(long game_ID, String name){
        Game currentGame = activeGames.get(game_ID);
        if (!currentGame.isCanJoin()){
            return null;
        }
        log.info("Player: " + name);
        long id = playerID.getAndAdd(1);
        currentGame.addPlayer(id, name);
        if (currentGame.isOnline()){
            notificationController.sendMessage(new Notification(NotificationType.REFRESH, String.valueOf(id)),
             currentGame.getGame_ID());
        }
        return currentGame;
    }

    public void removePlayer(long game_ID, String name){
        Game currentGame = activeGames.get(game_ID);
        for (Player player: currentGame.getPlayers()){
            if(player.getName().equals(name)){
                currentGame.getPlayers().remove(player);
                break;
            } 
        }
        if (currentGame.getPlayers().isEmpty()) deleteGame(game_ID);
        else {
            notificationController.sendMessage(new Notification(NotificationType.REFRESH, ""),
            currentGame.getGame_ID());
        }
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
        if (currentGame.isOnline()){
            notificationController.sendMessage(new Notification(NotificationType.REFRESH, ""),
             currentGame.getGame_ID());
        }
        return currentGame;
    }
    
    public Game startGame(long game_ID, int points){
        Game currentGame = activeGames.get(game_ID);
        log.info("Game has started: " + game_ID);
        currentGame.setCurrentPlayersTurn();
        currentGame.setTurnsLeft(currentGame.getTurnsLeft() * currentGame.getPlayers().size());
        if(currentGame.getPlaylist().getTracks().getItems().size() < currentGame.getTurnsLeft()){
            currentGame.setTurnsLeft(currentGame.getPlaylist().getTracks().getItems().size());
        }
        currentGame.setWinCondition(points - 1);
        currentGame.startGame();
        currentGame.setFirstSongForPlayers();
        currentGame.newCurrentSong();
        if(currentGame.isOnline()) {
            this.notificationController.sendMessage(new Notification(NotificationType.START, String.valueOf(game_ID)), game_ID);
        }
        return currentGame;
    }

    public boolean checkIfSongGuessIsCorrect(Game currentGame, GameAction action){
        Player player = currentGame.findPlayerByID(action.getPlayer().getId());
        int index = action.getIndex();
        boolean ans = currentGame.checkDatesOfCreation(index, player);
        if(ans) {
            player.getSongsList().add(index, currentGame.getCurrentSong());
            player.addPoint();
            if(currentGame.checkWinCondition(player)){
                log.info("Game Finished");
                notificationController.sendMessage(new Notification(NotificationType.END, String.valueOf(currentGame.getGame_ID())),
                 currentGame.getGame_ID());
                 return true;
            }
        }
        return ans;
    }

    public boolean checkIfArtistNameGuessIsCorrect(long game_ID, String artistName, long player_ID){
        Game currentGame = activeGames.get(game_ID);
        boolean ans = currentGame.checkArtistName(artistName, player_ID);
        if (currentGame.isOnline()){
            notificationController.sendMessage(new Notification(NotificationType.REFRESH, ""),
            currentGame.getGame_ID());
        }
        return ans;
    }


    public boolean checkIfTitleGuessIsCorrect(long game_ID, String title, long player_ID){
        Game currentGame = activeGames.get(game_ID); 
        boolean ans = currentGame.checkTitle(title, player_ID);
        if (currentGame.isOnline()){
            notificationController.sendMessage(new Notification(NotificationType.REFRESH, ""),
            currentGame.getGame_ID());
        }
        return ans;
    }

    public boolean checkToken(Game currentGame, GameAction action){
        Player player = currentGame.findPlayerByID(action.getPlayer().getId());
        int index = action.getIndex();
        boolean ans = currentGame.checkDatesOfCreation(index, currentGame.findCurrentPlayersTurn());
        if (ans){
            player.addPoint();
            player.addSongProperly(currentGame.getCurrentSong());
            if(currentGame.checkWinCondition(player)){
                log.info("Game Finished");
                notificationController.sendMessage(
                    new Notification(NotificationType.END, String.valueOf(currentGame.getGame_ID())), currentGame.getGame_ID());
                return true;
            }
        }
        return ans;
    }

    public boolean useToken(long game_ID, long player_ID) {
        Game currentGame = activeGames.get(game_ID);
        Player player = currentGame.findPlayerByID(player_ID);
        return player.deleteSpecialToken();
    }

    public Game resetPlaylist(long game_ID){
        Game currentGame = activeGames.get(game_ID);
        currentGame.setPlaylist(null);
        if (currentGame.isOnline()){
            notificationController.sendMessage(new Notification(NotificationType.REFRESH, ""),
            currentGame.getGame_ID());
        }
        return currentGame;
    }

    public List<GameAction> addAction(long game_ID, GameAction action){
        Game currentGame = activeGames.get(game_ID);
        currentGame.getActions().add(action);
        if (currentGame.isOnline()){
            notificationController.sendMessage(new Notification(NotificationType.REFRESH_ACTIONS, ""),
            currentGame.getGame_ID());
        }
        return currentGame.getActions();
    }

    public Game executeActions(long game_ID) {
        Game currentGame = activeGames.get(game_ID);
        if(currentGame.getActions().isEmpty()) return currentGame;
        for(GameAction action: currentGame.getActions()){
            if(action.getPlayer().isHasTurn()){
                if(checkIfSongGuessIsCorrect(currentGame, action)){
                    break;
                }
            }else{
                if(checkToken(currentGame, action)){
                    break;
                }
            }
        }
        newTurn(currentGame);
        return currentGame;
    }

    public void newTurn(Game currentGame){
        currentGame.setCurrentPlayersTurn();
        currentGame.setActions(new ArrayList<>());
        currentGame.newCurrentSong();
        if (currentGame.isOnline()){
            notificationController.sendMessage(new Notification(NotificationType.LOCKED_BY_PLAYER,
            String.valueOf(currentGame.findCurrentPlayersTurn().getId())), currentGame.getGame_ID());
            notificationController.sendMessage(new Notification(NotificationType.REFRESH, ""),
            currentGame.getGame_ID());
        }
    }

    public void deleteGame(long game_ID){
        log.info("Deleting game: " + game_ID);
        this.activeGames.remove(game_ID);
    }
}
