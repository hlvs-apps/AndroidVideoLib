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

package de.hlvsapps.test;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.hlvsapps.androidvideolib.R;
import de.hlvsapps.androidvideolib.utils;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws InterruptedException {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testActivityRule.getActivity();
        testActivityRule.launchActivity(new Intent());
        Thread.sleep(1000L*1000);
        //assertEquals("de.hlvsapps.androidvideolib", appContext.getPackageName());

    }

    @Test
    public void test() throws Exception {
        startActivityRule.getActivity();
        startActivityRule.launchActivity(new Intent());
        Thread.sleep(1000*3);
        StartActivityForResult myActivity = startActivityRule.getActivity();
        Assert.assertFalse(myActivity.getActivityResultIsReturned());
        Assert.assertFalse(myActivity.getSuccess());

        Button startButton = (Button) myActivity.findViewById(R.id.start_button);

        // Simulate a button click that start ChildActivity for result:
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // click button and open next activity.
                startButton.performClick();
            }
        });

        // Wait for the ActivityMonitor to be hit, Instrumentation will then return the mock ActivityResult:
        long start = System.currentTimeMillis();
        while (!myActivity.getActivityResultIsReturned()) {
            long current=System.currentTimeMillis();
            utils.LogI("Start: "+start+" End: "+ (start+TIME_OUT) + " Current: "+current);
            if((start+TIME_OUT)<current){
                Assert.fail("To much time");
                return;
            }
            Thread.sleep(5000);
        }

        // How do I check that StartActivityForResult correctly handles the returned result?
        Assert.assertTrue(myActivity.getActivityResultIsReturned());
        Assert.assertTrue(myActivity.getSuccess());

    }

    private static final int TIME_OUT = 30 * 3600 * 1000; // 30 Minutes

    private final ActivityTestRule<video_test> testActivityRule = new ActivityTestRule<>(video_test.class, true, true);
    private final ActivityTestRule<StartActivityForResult> startActivityRule = new ActivityTestRule<>(StartActivityForResult.class, true, true);


}