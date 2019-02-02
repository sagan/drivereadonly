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

public class Hook implements IXposedHookLoadPackage {
  public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
//  if (!lpparam.packageName.equals("com.android.systemui"))
//     return;
    XposedBridge.log("DriveReadonly Load package" + lpparam.packageName);

    XSharedPreferences pref = new XSharedPreferences(Hook.class.getPackage().getName(), "config");
    pref.reload();

    if( !pref.getBoolean("enabled", false) ) {
      return;
    }

    findAndHookConstructor("com.google.android.gms.common.api.Scope", lpparam.classLoader, String.class, new XC_MethodHook() {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        String scope = (String) param.args[0];
        if (scope.equals("https://www.googleapis.com/auth/drive")) {
          param.args[0] = "https://www.googleapis.com/auth/drive.readonly";
        }
      }
    });

    findAndHookConstructor("com.google.android.gms.common.api.Scope", lpparam.classLoader, int.class, String.class, new XC_MethodHook() {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        String scope = (String) param.args[1];
        if (scope.equals("https://www.googleapis.com/auth/drive")) {
          param.args[1] = "https://www.googleapis.com/auth/drive.readonly";
        }
      }
    });

    XC_MethodHook hook = new GoogleAuthUtilGetTokenHook();

    findAndHookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, Account.class, String.class, Bundle.class, hook);
    findAndHookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, Account.class, String.class, hook);
    findAndHookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, String.class, String.class, Bundle.class, hook);
    findAndHookMethod("com.google.android.gms.auth.GoogleAuthUtil", lpparam.classLoader, "getToken", Context.class, String.class, String.class, hook);
    //getTokenWithNotification 好像没有什么 app 用，就不写hook了

  }

  class GoogleAuthUtilGetTokenHook extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
      for(int i = 0; i < param.args.length; i++) {
        if( param.args[i] instanceof String ) {
          String scope = (String) param.args[i];
          if( scope.matches("(?i)^\\s*oauth2:.*") ){
            if( scope.matches( ".*(\\s|^)https://www\\.googleapis\\.com/auth/drive(\\s|$).*") ) {
              param.args[i] = scope.replace("https://www.googleapis.com/auth/drive", "https://www.googleapis.com/auth/drive.readonly");
            }
            break;
          }
        }
      }
    }
  }
}