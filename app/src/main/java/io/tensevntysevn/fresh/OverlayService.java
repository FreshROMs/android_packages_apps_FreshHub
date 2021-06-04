package io.tensevntysevn.fresh;

import android.content.Context;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import de.dlyt.yanndroid.fresh.BuildConfig;

public class OverlayService {
    private static final String disableOverlay = "cmd overlay disable";
    private static final String enableOverlay = "cmd overlay enable";

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    public static void setOverlayState(Context context, @NonNull String packageName, Boolean enabled) throws Exception {
        StringBuilder enableCommand = new StringBuilder(enableOverlay + ' ' + packageName);
        StringBuilder disableCommand = new StringBuilder(disableOverlay + ' ' + packageName);

        Shell.getShell(shell -> new Thread(() -> {
            Shell.sh(enabled ? disableCommand.toString() : enableCommand.toString());
        }).start());
    }
}
