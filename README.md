# drivereadonly

* This xposed module make Android apps that can access Google Drive request API token in read-only mode.
* Usage: tick the "Enable" button. (You need to restart the target app if already running).
* This module decrease your device's performance seriously when working (loaded and enabled).
You should untick the "Enable" checkbox once you have done authorizing Google Drive in your target app.
* Tested in Android 9.0 with [ElderDrivers/EdXposed](https://github.com/ElderDrivers/EdXposed).

这个 Xposed 模块强制那些需要访问 Google Drive 的 App 以只读模式申请 Google Drive 的 Oauth2 token.

其使用的方法十分粗暴，直接 hook 几个底层接口以修改 App 使用的 Google API scope 字符串
将 "https://www.googleapis.com/auth/drive" 统统改为 "https://www.googleapis.com/auth/drive.readonly"。
