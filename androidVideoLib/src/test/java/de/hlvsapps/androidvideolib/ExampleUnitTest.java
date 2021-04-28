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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    boolean b=false;
    boolean b2=false;
    @Test
    public void runnableTest() throws InterruptedException {
        PreRenderer.ExecutorPool.Executor exe=new PreRenderer.ExecutorPool.Executor(() -> {
            System.out.println("Ende");
            System.out.println(b);
            Assert.assertTrue(b);
            b2=true;
        });
        exe.execute(() -> {
                System.out.println(b);
                int i;
                for (i=0;i<=99;i++){
                    System.out.println(i);
                }
                Assert.assertEquals(i,100);
                b=true;
                System.out.println(b);
        });
        Thread.sleep(5000);
        System.out.println("The End");
        assertTrue(b2);
    }
}