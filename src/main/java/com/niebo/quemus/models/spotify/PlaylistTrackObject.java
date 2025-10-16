package com.niebo.quemus.models.spotify;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Model which corresponds to the Spotify Web API")
public class PlaylistTrackObject {
    private String added_at;
    private PlaylistCreator added_by;
    private boolean is_local;
    private Song track;
    
}
