package de.hlvsapps.androidvideolib;

import java.util.List;

public class VideoSegment {
    private List<UriIdentifier> uriIdentifiers;
    public VideoSegment(List<UriIdentifier> parts){
        this.uriIdentifiers =parts;
    }

    public List<UriIdentifier> getUriIdentifiers() {
        return uriIdentifiers;
    }

    public void addUriIdentifier(UriIdentifier uriIdentifier){
        uriIdentifiers.add(uriIdentifier);
    }

    public void setUriIdentifiers(List<UriIdentifier> uriIdentifiers){
        this.uriIdentifiers = uriIdentifiers;
    }
}
