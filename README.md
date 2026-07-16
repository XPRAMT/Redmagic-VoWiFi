# RedMagic VoWiFi

Target device: RedMagic / Nubia NX809J China ROM.

This app supports two operation modes:

- `ADB/root 全域模式`: do not hook apps. Write global properties with `resetprop`.
- `Root + LSPosed 模式`: hook only `com.android.settings` and `com.android.systemui`; avoid persistent global property changes.

Pixel IMS or equivalent carrier-config changes are still required for carrier WFC support.

## Build

```powershell
& 'C:\Users\XPRAMT\.gradle\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat' -p 'D:\Android\ZTE\VoWiFI\lsposed-redmagic-vowifi' assembleDebug
```

APK output:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Install

ADB install may fail on this ROM with `install session [-1]`.
If that happens, push the APK and open the system installer:

```powershell
& 'C:\APP\scrcpy-win64-v3.3.1\adb.exe' -s '192.168.1.9:5555' push 'D:\Android\ZTE\VoWiFI\lsposed-redmagic-vowifi\app\build\outputs\apk\debug\app-debug.apk' '/sdcard/Download/redmagic-vowifi-lsposed.apk'
& 'C:\APP\scrcpy-win64-v3.3.1\adb.exe' -s '192.168.1.9:5555' shell am start -a android.intent.action.VIEW -d 'file:///sdcard/Download/redmagic-vowifi-lsposed.apk' -t 'application/vnd.android.package-archive'
```

Then tap install on the phone.

## Mode 1: ADB/root Global Mode

Use this mode when you do not want LSPosed hooks.
The app's `套用全域參數（ADB/root 模式）` button runs `resetprop` through root.

Switch mapping:

- `開啟 VoWiFi 設定`
  - Writes: `ro.vendor.feature.zte_feature_need_wfc_for_domestic=true`
  - Effect: Settings domestic WFC gate passes.
- `開啟狀態列 VoWiFi 圖標`
  - Writes: `ro.vendor.mifavor.custom=abroad`
  - Writes: `ro.mifavor.custom=abroad`
  - Effect: SystemUI uses the abroad IMS icon branch.
- `VoWiFi 圖標樣式 = GEN_BD`
  - Writes: `persist.custom.variant.id=GEN_BD`
  - Effect: SystemUI chooses BD-style VoWiFi resources such as `bd_stat_vowifi` and `bd_vowifi_card1/2/12`.

Equivalent ADB commands:

```sh
su
/data/adb/ksu/bin/resetprop -n ro.vendor.feature.zte_feature_need_wfc_for_domestic true
/data/adb/ksu/bin/resetprop -n ro.vendor.mifavor.custom abroad
/data/adb/ksu/bin/resetprop -n ro.mifavor.custom abroad
/data/adb/ksu/bin/resetprop -n persist.custom.variant.id GEN_BD
am force-stop com.android.settings
kill -9 $(pidof com.android.systemui)
```

Clear global mode changes:

```sh
su
/data/adb/ksu/bin/resetprop -n ro.vendor.feature.zte_feature_need_wfc_for_domestic false
/data/adb/ksu/bin/resetprop -n ro.vendor.mifavor.custom home
/data/adb/ksu/bin/resetprop -n ro.mifavor.custom home
/data/adb/ksu/bin/resetprop -d persist.custom.variant.id
am force-stop com.android.settings
kill -9 $(pidof com.android.systemui)
```

Notes:

- This mode changes global process-visible properties until reset/reboot/persistence tooling changes them again.
- `ro.vendor.mifavor.custom=abroad` and `persist.custom.variant.id=GEN_BD` may affect more than VoWiFi icons.

## Mode 2: Root + LSPosed Mode

Use this mode when LSPosed is available and you want reduced global side effects.

LSPosed scope:

- `com.android.settings`
- `com.android.systemui`
- Android/System Framework only if LSPosed requires it for the module to load.

Switch mapping:

- `開啟 VoWiFi 設定`
  - Hook target: `com.android.settings`
  - Faked value: `ro.vendor.feature.zte_feature_need_wfc_for_domestic=true`
  - Hook class candidate: `com.zte.settings.utils.SystemPropertiesZTE`
- `開啟狀態列 VoWiFi 圖標`
  - Hook target: `com.android.systemui`
  - Faked values: `ro.vendor.mifavor.custom=abroad`, `ro.mifavor.custom=abroad`
  - Effect: SystemUI abroad branch maps WFC state to VoWiFi icon.
- `VoWiFi 圖標樣式 = GEN_BD`
  - Hook target: `com.android.systemui`
  - Faked value: `persist.custom.variant.id=GEN_BD`
  - Effect: BD-style VoWiFi icon resources.

After changing switches, press:

```text
重啟 Settings + SystemUI
```

The app also includes:

```text
修正模組設定讀取權限
```

This fixes the module preference XML permissions for `XSharedPreferences`.
If preferences cannot be read, hooks fail closed:

- WFC Settings hook: off
- SystemUI abroad hook: off
- icon style override: default/off

## Theme

The app uses Android's non-light Material theme so light/dark appearance follows the system setting.
