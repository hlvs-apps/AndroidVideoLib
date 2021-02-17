/*-----------------------------------------------------------------------------
 - Copyright hlvs-apps                                                        -
 - This is a part of AndroidVideoLib                                          -
 - Licensed under Apache 2.0                                                  -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

public class UriIdentifierPair {
    private final UriIdentifier uriIdentifier;
    private final Integer frameStartInProject;
    private int lengthInFrames;
    public UriIdentifierPair(UriIdentifier uriIdentifier,Integer frameStartInProject){
        this.uriIdentifier=uriIdentifier;
        this.frameStartInProject=frameStartInProject;
    }

    public int getLengthInFrames() {
        return lengthInFrames;
    }

    public UriIdentifierPair setLengthInFrames(int lengthInFrames) {
        this.lengthInFrames = lengthInFrames;
        return this;
    }

    public Integer getFrameStartInProject() {
        return frameStartInProject;
    }


    public UriIdentifier getUriIdentifier() {
        return uriIdentifier;
    }
}
