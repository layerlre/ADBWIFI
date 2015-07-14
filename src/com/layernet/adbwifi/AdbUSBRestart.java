package com.layernet.adbwifi;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.developerphil.adbidea.ui.NotificationHelper.error;
import static com.developerphil.adbidea.ui.NotificationHelper.info;

public class AdbUSBRestart {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AdbWifi-%d").build());

    public static void restart(final Project project){

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                String androidSdkPath;
                if (AndroidSdkUtils.getAndroidSdkPathsFromExistingPlatforms().size() > 0) {
                    androidSdkPath = Iterables.get(AndroidSdkUtils.getAndroidSdkPathsFromExistingPlatforms(), 0);
                    androidSdkPath = androidSdkPath + "/platform-tools/";
                } else {
                    error("Android SDK path not found");
                    return;
                }
                try {
                    WindowManager.getInstance().getStatusBar(project).setInfo("adb kill-server...");
                    Runtime.getRuntime().exec(androidSdkPath + "adb kill-server");
                    WindowManager.getInstance().getStatusBar(project).setInfo("adb start-server...");
                    Runtime.getRuntime().exec(androidSdkPath + "adb start-server");

                    info("restart successfully");

                } catch (IOException e) {
                    e.printStackTrace();
                    error(e.getMessage());
                }
            }
        });

    }
}
