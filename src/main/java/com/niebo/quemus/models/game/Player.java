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
    private String device_ID;
    private String name;
    private int points = 0;
    private List<PlaylistTrackObject> songsList = new ArrayList<>();
    private int specialTokens = 2;
    private boolean hasTurn = false;

    public Player(long id, String name, String deviceID){
        this.id = id;
        this.name = name;
        this.device_ID = deviceID;
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

    public void addSongProperly(PlaylistTrackObject currentSong) {
    int currYear = Integer.parseInt(currentSong.getTrack().getAlbum().release_date().substring(0, 4));
    if (songsList.isEmpty()) {
        songsList.add(currentSong);
        return;
    }
    for (int i = 0; i < songsList.size(); i++) {
        int year = Integer.parseInt(songsList.get(i).getTrack().getAlbum().release_date().substring(0, 4));
        if (currYear < year) {
            songsList.add(i, currentSong);
            return;
        }
    }
    songsList.add(currentSong);
}
}
