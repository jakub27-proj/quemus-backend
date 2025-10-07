package com.niebo.quemus.models.spotify;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model which corresponds to the Spotify Web API")
public record External_ids(
    String isrc,
    String ean,
    String upc
) {
}
