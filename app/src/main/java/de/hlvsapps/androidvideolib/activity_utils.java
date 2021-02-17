/*-----------------------------------------------------------------------------
 - Copyright hlvs-apps                                                        -
 - This is a part of AndroidVideoLib                                          -
 - Licensed under Apache 2.0                                                  -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class activity_utils {
    //Konstanten von https://stackoverflow.com/questions/48166206/how-to-start-power-manager-of-all-android-manufactures-to-enable-background-and
    //Code teils von https://stackoverflow.com/questions/48349889/huawei-kill-a-background-app-when-the-phone-is-locked
    public static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")),
            new Intent().setComponent(new ComponentName("com.transsion.phonemanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity"))
    };

    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo>list=context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void startPowerSaverIntent(Context context) {
        for (Intent intent : POWERMANAGER_INTENTS) {
            if (isCallable(context, intent)) {
                new AlertDialog.Builder(context)
                        .setTitle(Build.MANUFACTURER + " Protected Apps")
                        .setMessage(String.format("%s requires to be 'White list' to function properly.\nDisable %s from list, otherwise the App will not be able to render when the display is off.\nIf you have to check options, make sure to check all.%n", context.getString(R.string.app_name), context.getString(R.string.app_name)))
                        .setPositiveButton("Go to settings", (dialog, which) -> {
                            context.startActivity(intent);
                            dialog.dismiss();
                        })
                        .show();
                break;
            }
        }
    }
}
