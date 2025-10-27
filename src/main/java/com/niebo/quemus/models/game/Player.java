package com.niebo.quemus.models.game;

import java.util.ArrayList;
import java.util.List;

import com.niebo.quemus.models.spotify.PlaylistTrackObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description= "Model which represents player of the game")
public class Player {
    private long id;
    private String name;
    private int points = 0;
    private List<PlaylistTrackObject> songsList = new ArrayList<>();
    private int specialTokens = 2;
    private boolean hasTurn = false;

    public Player(long id, String name){
        this.id = id;
        this.name = name;
    }
    public void addSpecialToken(){
        specialTokens++;
    }
    
    public boolean deleteSpecialToken(){
        if (specialTokens < 1){
            return false;
        }
        specialTokens--;
        return true;
    }

    public void addPoint(){
        this.points++;
    }
    public void addSongProperly(PlaylistTrackObject currentSong){
        int currYear = Integer.parseInt(currentSong.getTrack().getAlbum().release_date().substring(0,4));
        for(int i = 0; i < songsList.size(); i++){
            if(i == songsList.size() - 1) this.songsList.add(currentSong);
            int prev_year = Integer.parseInt(songsList.get(i).getTrack().getAlbum().release_date().substring(0,4));
            int next_year = Integer.parseInt(songsList.get(i + 1).getTrack().getAlbum().release_date().substring(0,4));
            if(prev_year <= currYear && currYear <= next_year){
                this.songsList.add(i, currentSong);
            }
        }
    }
}
