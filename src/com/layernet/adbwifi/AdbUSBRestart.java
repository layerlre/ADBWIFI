package com.layernet.adbwifi;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.android.sdk.AndroidSdkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.developerphil.adbidea.ui.NotificationHelper.error;
import static com.developerphil.adbidea.ui.NotificationHelper.info;

/**
 * Created by layer on 9/7/2558.
 */
public class AdbUSBRestart {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AdbWifi-%d").build());

    public static void restart(){
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                String androidSdkPath;
                if (AndroidSdkUtils.getAndroidSdkPathsFromExistingPlatforms().size() > 0) {
                    androidSdkPath = Iterables.get(AndroidSdkUtils.getAndroidSdkPathsFromExistingPlatforms(), 0);
                    androidSdkPath = androidSdkPath + "/platform-tools/";
                    File file = new File(androidSdkPath + "adb.exe");
                    System.out.println("file : "+ file.getAbsolutePath());
                    if (file.exists()){
                        androidSdkPath = androidSdkPath.replace("/", "\\");
                    }
                } else {
                    error("Android SDK path not found");
                    return;
                }
                try {
                    Runtime.getRuntime().exec(androidSdkPath + "adb kill-server");
                    Process process = Runtime.getRuntime().exec(androidSdkPath + "adb start-server");
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;

                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }

                    info("restart successfully");

                } catch (IOException e) {
                    e.printStackTrace();
                    error(e.getMessage());
                }
            }
        });

    }
}
