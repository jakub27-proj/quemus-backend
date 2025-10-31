package com.niebo.quemus.models.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.niebo.quemus.models.spotify.Playlist;
import com.niebo.quemus.models.spotify.PlaylistTrackObject;
import com.niebo.quemus.models.spotify.SimplifiedArtistObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Schema(description= "Model which represents game instance")
public class Game {
    private long game_ID;
    private List<Player> players = new ArrayList<>();
    private int turnsLeft;
    private Playlist playlist;
    private PlaylistTrackObject currentSong;
    private boolean hasStarted = false;
    private boolean isOnline;
    private boolean canJoin = true;
    @JsonIgnore
    private Random random = new Random();
    @JsonIgnore
    private int winCondition = 10;
    
    public Game(boolean isOnline, long game_ID, int turnsLeft){
        this.turnsLeft = turnsLeft;
        this.game_ID = game_ID;
        this.isOnline = isOnline;
    }

    public void addPlayer(long id, String name){
        this.players.add(new Player(id, name));
    }
    
    public void startGame(){
        this.hasStarted = true;
        this.canJoin = false;
    }

    public void setCurrentPlayersTurn(){
        if(hasStarted){
            for (int i = 0; i < players.size(); i++){
                if(players.get(i).isHasTurn()){
                    players.get(i).setHasTurn(false);
                    players.get((i + 1) % players.size()).setHasTurn(true);
                    break;
                }
            }
        }else{
            players.get(random.nextInt(players.size())).setHasTurn(true);
        }
    }

    public void setFirstSongForPlayers(){
        List<PlaylistTrackObject> listOfSongs = playlist.getTracks().getItems();
        this.turnsLeft--;
        for(Player player: players){
            player.getSongsList().add(listOfSongs.remove(random.nextInt(listOfSongs.size())));
        }
    }

    public PlaylistTrackObject newCurrentSong(){
         List<PlaylistTrackObject> listOfSongs = this.playlist.getTracks().getItems();
        if (listOfSongs.isEmpty()) return null;
        PlaylistTrackObject playlistTrackObject = listOfSongs.remove(random.nextInt(listOfSongs.size()));
        this.currentSong = playlistTrackObject;
        return playlistTrackObject;
    }

    public Player findPlayerByID(long player_ID){
        Player currentPlayer = null;
        for(Player player: this.players){
            if(player.getId() == player_ID){
                currentPlayer = player;
                break;
            }
        }
        return currentPlayer;
    }

    public Player findCurrentPlayersTurn() { 
         Player currentPlayer = null;
        for(Player player: this.players){
            if(player.isHasTurn()){
                currentPlayer = player;
                break;
            }
        }
        return currentPlayer;
    }

    public boolean checkDatesOfCreation(int index, Player player){
        List<PlaylistTrackObject> playlistTrackObjects = player.getSongsList();
        int currYear = getReleaseYear(currentSong.getTrack().getAlbum().release_date());
        if (index > 0){
            PlaylistTrackObject ptoLeft = playlistTrackObjects.get(index - 1);
            int leftYear = getReleaseYear(ptoLeft.getTrack().getAlbum().release_date());
            if (leftYear > currYear) return false;
        }
        if (index < playlistTrackObjects.size()){
            PlaylistTrackObject ptoRight = playlistTrackObjects.get(index);
            int rightYear = getReleaseYear(ptoRight.getTrack().getAlbum().release_date());
            if (currYear > rightYear) return false;
        }
        return true;
    }

    private int getReleaseYear(String date){
        log.debug(date);
        return Integer.parseInt(date.substring(0, 4));
    }

    public boolean checkArtistName(String guess, long player_ID){
        Player p = findPlayerByID(player_ID);
        guess = guess.toLowerCase();
        List<SimplifiedArtistObject> artists = currentSong.getTrack().getArtists();
        for (SimplifiedArtistObject aritist: artists){
            String name = aritist.getName().toLowerCase();
            if(guess.equals(name)){
                p.addSpecialToken();
                return true;
            } 
        }
        return false;
    }

    public boolean checkTitle(String guess, long player_ID){
        Player p = findPlayerByID(player_ID);
        guess = guess.toLowerCase();
        String title = currentSong.getTrack().getName().toLowerCase();
        if (guess.equals(title)){
            p.addSpecialToken();
            return true;
        } 
        return false;
    }

    public boolean checkWinCondition(Player player) {
        return player.getPoints() == winCondition;
    }

    @Override 
    public String toString(){
        return String.format("Game: ID: %d | Players: %d | HasStarted: %s | isOnline: %s ", 
        this.game_ID, this.players.size(), this.hasStarted, this.isOnline);
    }
}
