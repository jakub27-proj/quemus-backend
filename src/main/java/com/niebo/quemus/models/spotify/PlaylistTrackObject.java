package com.niebo.quemus.models.spotify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistTrackObject {
    private String added_at;
    private String added_by;
    private boolean is_local;
    private Song trackObject;
    
}
