package me.sagan.drivereadonly;

import static de.robv.android.xposed.XposedHelpers.*;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;

import java.util.Arrays;
import java.util.Collections;

public class Hook implements IXposedHookLoadPackage {
  public static final String DRIVE = "https://www.googleapis.com/auth/drive";
  public static final String DRIVE_READONLY = "https://www.googleapis.com/auth/drive.readonly";

  public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
//  if (!lpparam.packageName.equals("com.doubleTwist.cloudPlayer"))
//    return;
//  XposedBridge.log("fuckyou Load package " + lpparam.packageName);

    XSharedPreferences pref = new XSharedPreferences(Hook.class.getPackage().getName(), "config");
    pref.reload();
    if( !pref.getBoolean("enabled", false) ) {
      return;
    }

    XC_MethodHook scopeHook = new ScopeHook();
    hookConstructor("com.google.android.gms.common.api.Scope", lpparam.classLoader, String.class, scopeHook);
    // GMS 里创建 Scope 方法太多. 而且很多App代码都混淆了, 所以这样简单粗暴地 hook - -
    findAndHookMethod(Collections.class, "singletonList", Object.class, scopeHook);
    findAndHookMethod(Collections.class, "singleton", Object.class, scopeHook);
    findAndHookMethod(android.os.Parcel.class, "writeString", String.class, scopeHook);
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, Account.class, String.class, Bundle.class, scopeHook);
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, Account.class, String.class, scopeHook);
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, String.class, String.class, Bundle.class, scopeHook);
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, String.class, String.class, scopeHook);
    //getTokenWithNotification 好像没有什么 app 用，就不写hook了

  }

  static void hookMethod(String c, ClassLoader classLoader, String s, Object... parameterTypesAndCallback) {
    try {
      findAndHookMethod(c, classLoader, s, parameterTypesAndCallback);
    } catch(Exception e) {}
  }

  static void hookConstructor(String c, ClassLoader classLoader,  Object... parameterTypesAndCallback) {
    try {
      findAndHookConstructor(c, classLoader, parameterTypesAndCallback);
    } catch(Exception e) {}
  }

  class ScopeHook extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
      for(int i = 0; i < param.args.length; i++) {
        if( param.args[i] instanceof String ) {
          String scope = (String) param.args[i];
          if( scope.length() > 400 ) {
            continue;
          } else if( scope.equals(DRIVE) ) {
            param.args[i] = DRIVE_READONLY;
            break;
          } else if( scope.toLowerCase().startsWith("oauth2:") ){
//            XposedBridge.log("fuckyou hook oauth2 " + scope);
            if( scope.matches( ".*(\\s|:)https://www\\.googleapis\\.com/auth/drive(\\s|$).*") ) {
              param.args[i] = scope.replace(DRIVE, DRIVE_READONLY);
            }
            break;
          }
        }
      }
    }
  }

}