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
//  if (!lpparam.packageName.equals("com.android.systemui"))
//     return;
    XposedBridge.log("fuckyou Load package " + lpparam.packageName);

    XSharedPreferences pref = new XSharedPreferences(Hook.class.getPackage().getName(), "config");
    pref.reload();

    if( !pref.getBoolean("enabled", false) ) {
      return;
    }

    try {
      findAndHookConstructor("com.google.android.gms.common.api.Scope", lpparam.classLoader, String.class, new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
          String scope = (String) param.args[0];
          XposedBridge.log("fuckyou hook scope " + scope);
          if (scope.equals(DRIVE)) {
            param.args[0] = DRIVE_READONLY;
          }
        }
      });
    } catch(Exception e) {

    }

    XC_MethodHook scopeHook = new ScopeHook();
    // 很多 app 使用 Collections.singletonList(DriveScopes.DRIVE) 这种方式创建 Scope. 所以这样简单粗暴地 hook - -
    findAndHookMethod(Collections.class, "singletonList", Object.class, scopeHook);
    findAndHookMethod(Collections.class, "singleton", Object.class, scopeHook);
    // findAndHookMethod(Arrays.class, "asList", Object[].class, scopeHook);



    XC_MethodHook authoUtilHook = new GoogleAuthUtilGetTokenHook();
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, Account.class, String.class, Bundle.class, authoUtilHook);
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, Account.class, String.class, authoUtilHook);
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, String.class, String.class, Bundle.class, authoUtilHook);
    hookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, String.class, String.class, authoUtilHook);
    //getTokenWithNotification 好像没有什么 app 用，就不写hook了

  }

  static void hookMethod(String c, ClassLoader classLoader, String s, Object... parameterTypesAndCallback) {
    try {
      findAndHookMethod(c, classLoader, s, parameterTypesAndCallback);
    } catch(Exception e) {}
  }

  class GoogleAuthUtilGetTokenHook extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
      for(int i = 0; i < param.args.length; i++) {
        if( param.args[i] instanceof String ) {
          String scope = (String) param.args[i];
          if( scope.matches("(?i)^\\s*oauth2:.*") ){
            XposedBridge.log("fuckyou hook AuthUtils " + scope);
            if( scope.matches( ".*(\\s|:)https://www\\.googleapis\\.com/auth/drive(\\s|$).*") ) {
              param.args[i] = scope.replace(DRIVE, DRIVE_READONLY);
            }
            break;
          }
        }
      }
    }
  }

  class ScopeHook extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
      for(int i = 0; i < param.args.length; i++) {
        if( param.args[i] instanceof String ) {
          String scope = (String) param.args[i];
          if( scope.equals(DRIVE) ) {
            param.args[i] = DRIVE_READONLY;
            break;
          }
        }
      }
    }
  }

}