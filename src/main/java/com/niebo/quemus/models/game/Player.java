package com.niebo.quemus.models.game;

import java.util.List;

import com.niebo.quemus.models.spotify.Song;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Player {
    private long id;
    private int points;
    private List<Song> songsList;
    private int specialTokens = 2;
    private boolean hasTurn = false;

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
}
