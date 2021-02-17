/*-----------------------------------------------------------------------------
 - Copyright hlvs-apps                                                        -
 - This is a part of AndroidVideoLib                                          -
 - Licensed under Apache 2.0                                                  -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import java.util.Comparator;

public class UriIdentifierPairComparator implements Comparator<UriIdentifierPair> {
    @Override
    public int compare(UriIdentifierPair o1, UriIdentifierPair o2) {
        return o1.getFrameStartInProject()-o2.getFrameStartInProject();
    }
}

