package com.niebo.quemus.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimplifiedArtistObject {
    private External_urls externalUrls;
    private String href;
    private String id;
    private String name;
    private String type;
    private String uri;
}
