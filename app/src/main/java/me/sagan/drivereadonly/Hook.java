package me.sagan.drivereadonly;

import static de.robv.android.xposed.XposedHelpers.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hook implements IXposedHookLoadPackage {
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
//        if (!lpparam.packageName.equals("com.android.systemui"))
//            return;
        XposedBridge.log("DriveReadonly Load package" + lpparam.packageName);

        findAndHookConstructor("com.google.android.gms.common.api.Scope", lpparam.classLoader, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String scope = (String) param.args[0];
                XposedBridge.log("DriveReadonly Find  scope of package " + lpparam.packageName + " init scope " + scope);
                if( scope.equals("https://www.googleapis.com/auth/drive") ) {
                    param.args[0] = "https://www.googleapis.com/auth/drive.readonly";
                }
            }
        });

        findAndHookConstructor("com.google.android.gms.common.api.Scope", lpparam.classLoader, int.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String scope = (String) param.args[1];
                XposedBridge.log("DriveReadonly Find  scope of package " + lpparam.packageName + " init scope(int,String) " + scope);
                if( scope.equals("https://www.googleapis.com/auth/drive") ) {
                    param.args[1] = "https://www.googleapis.com/auth/drive.readonly";
                }
            }
        });
    }
}