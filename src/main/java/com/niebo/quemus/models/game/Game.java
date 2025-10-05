package com.niebo.quemus.models.game;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.niebo.quemus.models.spotify.Playlist;
import com.niebo.quemus.models.spotify.PlaylistTrackObject;
import com.niebo.quemus.models.spotify.Song;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    private long game_ID;
    private List<Player> players;
    private int turnsLeft;
    private Playlist playlist;
    private Song currentSong;
    private boolean hasStarted = false;
    private boolean isOnline;
    private boolean canJoin = true;
    @JsonIgnore
    private Random random = new Random();
    
    public Game(boolean isOnline, long game_ID, int turnsLeft){
        this.turnsLeft = turnsLeft;
        this.game_ID = game_ID;
        this.isOnline = isOnline;
    }

    public void addPlayer(Player player){
        this.players.add(player);
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
        for(Player player: players){
            player.getSongsList().add(listOfSongs.remove(random.nextInt(listOfSongs.size())).getTrackObject());
        }
    }

    public Song newCurrentSong(){
         List<PlaylistTrackObject> listOfSongs = this.playlist.getTracks().getItems();
        if (listOfSongs.isEmpty()) return null;
        return listOfSongs.remove(random.nextInt(listOfSongs.size())).getTrackObject();
    }

    public boolean checkGuess(int index, long player_ID){
        Player currentPlayer = null;
        for(Player player: this.players){
            if(player.getId() == player_ID){
                currentPlayer = player;
                break;
            }
        }
        return checkDatesOfCreation(currentPlayer);
    }

    private boolean checkDatesOfCreation(Player player){
        return true;
    }
}
