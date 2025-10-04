package com.niebo.quemus.models.spotify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Song {
    private Album album;
    private List<SimplifiedArtistObject> artists;
    private List<String> avaliable_markets;
    private int disc_number;
    private int duration_ms;
    private boolean explicit;
    private External_ids external_ids;
    private External_urls external_urls;
    private String href;
    private String id;
    private boolean is_playable;
    private Restrictions restrictions;
    private String name;
    private int popularity;
    private String preview_url;
    private int track_number;
    private String type;
    private String uri;
    private boolean is_local;
}
