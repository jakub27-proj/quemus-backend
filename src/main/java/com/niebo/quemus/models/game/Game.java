package com.niebo.quemus.models.game;

import java.util.List;

import com.niebo.quemus.models.spotify.Playlist;
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
    private boolean hasStarted;

}
