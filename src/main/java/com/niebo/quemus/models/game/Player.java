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
}
