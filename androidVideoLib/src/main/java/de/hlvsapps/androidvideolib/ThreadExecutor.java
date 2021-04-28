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


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * A Executor to Execute with the given {@link LooperType}
 * @author hlvs-apps
 */
public class ThreadExecutor implements Executor {
    private final Handler handler;

    private ThreadExecutor(LooperType type) {
        handler = new Handler(type.getLooperWithType());
    }
    public static ThreadExecutor getInstance(){
        return new ThreadExecutor(LooperType.CurrentLooper);
    }
    public static ThreadExecutor getInstance(LooperType looperType){
        return new ThreadExecutor(looperType);
    }

    @Override
    public void execute(Runnable r) {
        handler.post(r);
    }

    public enum LooperType{
        CurrentLooper, MainLooper;
        public Looper getLooperWithType(){
            switch (this){
                case MainLooper:
                    return Looper.getMainLooper();
                case CurrentLooper:
                    default:
                        return Looper.myLooper();
            }
        }
    }
}
