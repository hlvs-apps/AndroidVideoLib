/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:  https://githubcom/hlvs-apps/AndroidVideoLib/blob/master/AUTHOR.md
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

/**
 * Part of {@link SendProgressAsBroadcast} <br>
 * Enum for possible Calling Methods.<br>
 * The following Table Shows which Member of this Enum is used for which Method:
 * <table border="1">
 * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#preRenderUpdateProgress}</td><td>for</td></td></td> {@link SendProgressAsBroadcast#updateProgress(int, int, boolean)}.</td></tr>
 * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#renderInstantiateProgressForRendering}</td><td>for</td></td><td> {@link SendProgressAsBroadcast#instantiateProgressesForRendering(int)}.</td></tr>
 * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#renderUpdateProgressOfX}</td><td>for</td></td></td> {@link SendProgressAsBroadcast#updateProgressOfX(int, int, int, boolean)}.</td></tr>
 * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#lastRenderUpdateProgressOfSavingVideo}</td><td>for</td></td></td> {@link SendProgressAsBroadcast#updateProgressOfSavingVideo(int, int, boolean)}.</td></tr>
 * </table>
 *
 * @see SendProgressAsBroadcast
 * @author hlvs-apps
 */
public enum INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED{
    preRenderUpdateProgress,renderInstantiateProgressForRendering,renderUpdateProgressOfX,lastRenderUpdateProgressOfSavingVideo
}