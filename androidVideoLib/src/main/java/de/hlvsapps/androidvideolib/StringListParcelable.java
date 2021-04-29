/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:                               -
 -       https://github.com/hlvs-apps/AndroidVideoLib/blob/master/AUTHORS.md  -
 - Unless otherwise noted, this is                                            -
 - Licensed under the Apache License, Version 2.0 (the "License");            -
 - you may not use this file except in compliance with the License.           -
 - You may obtain a copy of the License at                                    -
 -                                                                            -
 -     http://www.apache.org/licenses/LICENSE-2.0                             -
 -                                                                            -
 - Unless required by applicable law or agreed to in writing, software        -
 - distributed under the License is distributed on an "AS IS" BASIS,          -
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   -
 - See the License for the specific language governing permissions and        -
 - limitations under the License.                                             -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A class to easily create Parcels from StringLists
 * @author hlvs-apps
 */
public class StringListParcelable implements Parcelable {
    private final List<String> stringList;

    private StringListParcelable(List<String> stringList) {
        this.stringList = stringList;
    }

    /**
     * Gets an Instance with the stringList
     * @param stringList The StringList
     * @return the Instance
     */
    public static StringListParcelable from(List<String> stringList){
        return new StringListParcelable(stringList);
    }

    /**
     * Gets an Instance from a File, saved by {@link StringListParcelable#saveToFile(Context)}
     * @param byteArrayFile The File with the Saved byte Array
     * @return The Instance
     * @see StringListParcelable#saveToFile(Context)
     * @see utils#getParcelableFromTempFile(File, Creator)
     */
    public static StringListParcelable from(File byteArrayFile) throws IOException {
        return utils.getParcelableFromTempFile(byteArrayFile,CREATOR);
    }

    /**
     * Saves this Instance to a temp File
     * @param context Your Context to get the temp file
     * @return The File this Instance was saved in. To restore the Instance, call {@link StringListParcelable#from(File)}
     * @see StringListParcelable#from(File)
     * @see utils#writeByteArrayToTempFile(Context, byte[])
     * @see utils#marshall(Parcelable)
     */
    public File saveToFile(Context context) throws IOException {
        return utils.writeByteArrayToTempFile(context,utils.marshall(this));
    }

    /**
     * Gets the StringList
     * @return the String List
     */
    public List<String> getStringList() {
        return stringList;
    }


    protected StringListParcelable(Parcel in) {
        stringList = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(stringList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StringListParcelable> CREATOR = new Creator<StringListParcelable>() {
        @Override
        public StringListParcelable createFromParcel(Parcel in) {
            return new StringListParcelable(in);
        }

        @Override
        public StringListParcelable[] newArray(int size) {
            return new StringListParcelable[size];
        }
    };
}
