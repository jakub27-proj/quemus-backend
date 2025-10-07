package com.niebo.quemus.models.spotify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Model which corresponds to the Spotify Web API")
public class Playlist {
    private boolean collaborative;
    private String description;
    private String href;
    private String id;
    private List<ImageObject> images;
    private String name;
    private Owner owner;
    private Tracks tracks;
    private String type;
    private String uri;
}
