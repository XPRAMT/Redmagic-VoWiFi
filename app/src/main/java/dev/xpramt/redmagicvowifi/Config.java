package dev.xpramt.redmagicvowifi;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

final class Config {
    static final String PACKAGE_NAME = "dev.xpramt.redmagicvowifi";
    static final String PREFS_NAME = "module";
    static final String HOOK_CONFIG_PATH = "/data/adb/redmagic-vowifi/config.properties";

    static final String KEY_ENABLE_WFC_SETTINGS = "enable_wfc_settings";
    static final String KEY_ENABLE_STATUS_ICON = "enable_status_icon";
    static final String KEY_ICON_STYLE = "icon_style";
    static final String KEY_OPERATION_MODE = "operation_mode";

    static final String STYLE_DEFAULT = "default";
    static final String STYLE_GEN_BD = "gen_bd";
    static final String STYLE_ARRAY_HOOK = "array_hook";

    static final String MODE_LSPOSED = "lsposed";
    static final String MODE_ROOT_GLOBAL = "root_global";

    private Config() {
    }

    static SharedPreferences appPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    static Snapshot loadForHook() {
        Properties prefs = new Properties();
        File file = new File(HOOK_CONFIG_PATH);
        try (FileInputStream input = new FileInputStream(file)) {
            prefs.load(input);
        } catch (IOException ignored) {
        }
        return new Snapshot(
                prefs.getProperty(KEY_OPERATION_MODE, MODE_LSPOSED),
                Boolean.parseBoolean(prefs.getProperty(KEY_ENABLE_WFC_SETTINGS, "false")),
                Boolean.parseBoolean(prefs.getProperty(KEY_ENABLE_STATUS_ICON, "false")),
                prefs.getProperty(KEY_ICON_STYLE, STYLE_DEFAULT)
        );
    }

    static final class Snapshot {
        final boolean enableWfcSettings;
        final boolean enableStatusIcon;
        final String iconStyle;
        final String operationMode;

        Snapshot(String operationMode, boolean enableWfcSettings, boolean enableStatusIcon, String iconStyle) {
            this.operationMode = operationMode == null ? MODE_LSPOSED : operationMode;
            this.enableWfcSettings = enableWfcSettings;
            this.enableStatusIcon = enableStatusIcon;
            this.iconStyle = iconStyle == null ? STYLE_DEFAULT : iconStyle;
        }
    }
}
