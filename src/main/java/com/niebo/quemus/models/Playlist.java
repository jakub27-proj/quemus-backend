package com.niebo.quemus.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
