package com.layernet.adbwifi;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.developerphil.adbidea.adb.command.Command;
import com.developerphil.adbidea.adb.command.receiver.GenericReceiver;
import com.google.common.collect.Iterables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.apache.http.util.TextUtils;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.developerphil.adbidea.ui.NotificationHelper.error;
import static com.developerphil.adbidea.ui.NotificationHelper.info;

public class WifiCommand implements Command {

    private String androidSdkPath;
    private boolean success;
    private Project project;

    @Override
    public boolean run(Project project, IDevice device, AndroidFacet facet, String packageName) {
        this.project = project;
        GenericReceiver receiver = new GenericReceiver();
        try {
            WindowManager.getInstance().getStatusBar(project).setInfo("ADB WIFI : scan IP address ...");
            device.executeShellCommand("netcfg | grep UP | grep wlan", receiver, 1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
            error(e.getMessage());
            return false;
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
            error(e.getMessage());
            return false;
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
            error(TextUtils.isEmpty(e.getMessage()) ? "Killing process after timeout" : e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            error(e.getMessage());
            return false;
        }

        String ipAddress = getIpAddress(receiver);
        if (ipAddress == null) {
            error("Can't connect to wireless or get a valid IP address.");
        } else {

            if (AndroidSdkUtils.getAndroidSdkPathsFromExistingPlatforms().size() > 0) {
                androidSdkPath = Iterables.get(AndroidSdkUtils.getAndroidSdkPathsFromExistingPlatforms(), 0);
                androidSdkPath = androidSdkPath + "/platform-tools/";
            } else {
                error("Android SDK path not found");
                return false;
            }

            if (adbTcpip()) {
                try {
                    Thread.sleep(500);
                    success = adbWificonnect(ipAddress);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

        return success;
    }

    private String getIpAddress(GenericReceiver receiver) {
        for (String line : receiver.getAdbOutputLines()) {
            WindowManager.getInstance().getStatusBar(project).setInfo("ADB WIFI : " + line);
            if (!line.contains("127.0.0.1") && !line.contains("0.0.0.0")) {
                return line.substring(line.indexOf("UP") + 2, line.indexOf("/")).trim();
            }
        }
        return null;
    }

    private boolean adbTcpip() {
        try {
            WindowManager.getInstance().getStatusBar(project).setInfo("ADB WIFI : restarting in TCP mode port: 5555 ...");
            Process process = Runtime.getRuntime().exec(androidSdkPath + "adb tcpip 5555");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = null;
            while ((line = in.readLine()) != null) {
                if (line.contains("error")) {
                    error(line);
                    return false;
                }
                return true;
            }


        } catch (IOException e) {
            e.printStackTrace();
            error(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean adbWificonnect(String ipAddress) {
        boolean connected = false;
        try {
            WindowManager.getInstance().getStatusBar(project).setInfo("ADB WIFI : connect to " + ipAddress + "...");
            Process process = Runtime.getRuntime().exec(androidSdkPath + "adb connect " + ipAddress);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            String message = null;
            while ((line = in.readLine()) != null) {
                if (line.contains("connected")) {
                    connected = true;
                }
                message = line;
            }
            if (connected) {
                info(message);
                return true;
            } else {
                error(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
            error(e.getMessage());
        }
        return false;
    }
}
