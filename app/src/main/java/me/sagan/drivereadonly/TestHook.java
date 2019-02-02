package me.sagan.drivereadonly;

import static de.robv.android.xposed.XposedHelpers.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class TestHook implements IXposedHookLoadPackage {
  public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
    if (!lpparam.packageName.equals("me.sagan.drivereadonly"))
      return;
    findAndHookMethod("me.sagan.drivereadonly.MainActivity", lpparam.classLoader, "ok", new XC_MethodHook() {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        param.setResult(true);
      }
    });
  }
}