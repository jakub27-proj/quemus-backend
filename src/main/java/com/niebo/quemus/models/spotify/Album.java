package com.niebo.quemus.models.spotify;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model which corresponds to the Spotify Web API")
public record Album(
        String album_type,
        int total_tracks,
        List<String> avaliable_markets,
        External_urls external_urls,
        String href,
        String id,
        List<ImageObject> images,
        String name,
        String release_date,
        String release_date_precision,
        Restrictions restrictions,
        String type,
        List<SimplifiedArtistObject> artists
) {
}
