package com.niebo.quemus.services;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.reactive.function.client.WebClient;

import com.niebo.quemus.controllers.AuthController;
import com.niebo.quemus.controllers.NotificationController;
import com.niebo.quemus.models.dataStructures.TrackingActivityMap;
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
    private final TrackingActivityMap<Long, Game> activeGames = new TrackingActivityMap<>();
    private static final AtomicLong playerID = new AtomicLong(0);
    @Autowired
    private IDService idService;
    @Autowired
    private WebClient webClient;
    @Autowired
    private NotificationController notificationController;
    @Autowired
    private AuthController authController;
    @Value("${timeout.cleanup.time}")
    private long timeout_ms;


    public Game getGameById(long game_ID) {
        Game game = activeGames.get(game_ID);
        if (game == null) {
        throw new NoSuchElementException();
        }
        return game;
    }
    public Game createNewGame(boolean isOnline, int turnsLeft, 
    @CookieValue(value = "spotify_access_token", required = false) String accessToken, String device_ID){
        if(!authController.getSpotifySession(accessToken).get("loggedIn")) return null;
        long id = 0;
        do { 
            id = idService.generateGameID();
        } while (activeGames.hasKey(id));
        Game game = new Game(isOnline, id, turnsLeft, device_ID);
        log.info(game.toString());
        activeGames.put(id, game);
        return game;
    }

    public void setOnline(long game_ID){
        Game game = activeGames.get(game_ID);
        game.setOnline(!game.isOnline());
        if(!game.isOnline()){
            this.notificationController.sendMessage(new Notification(NotificationType.KICK, game.getHost_device_ID()), game_ID);
            game.removeAllForeignPlayers();
        }
        this.notificationController.sendMessage(new Notification(NotificationType.REFRESH, null), game_ID);
    }

    public Game joinGame(long game_ID, String name, String device_ID){
        Game currentGame = activeGames.get(game_ID);
        if (!currentGame.isCanJoin()){
            return null;
        }
        long id = playerID.getAndAdd(1);
        currentGame.addPlayer(id, name, device_ID);
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
        if (currentGame.getPlayers().isEmpty() && currentGame.isHasStarted()) deleteGame(game_ID);
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
        Game game = this.activeGames.get(game_ID);
        if(!game.isHasStarted()) this.notificationController.sendMessage(new Notification(NotificationType.KICK, game.getHost_device_ID()), game_ID);
        this.activeGames.remove(game_ID);
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanupInactiveSessions() {
        long cutoff = System.currentTimeMillis() - timeout_ms;

        for (Long key : activeGames.inactiveSince(cutoff)) {
            log.info("Scheduler removed game with ID {}", key);
            activeGames.remove(key);
        }
    }
}
