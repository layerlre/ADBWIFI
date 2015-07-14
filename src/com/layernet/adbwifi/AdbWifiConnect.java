package com.layernet.adbwifi;

import com.developerphil.adbidea.adb.AdbUtil;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.developerphil.adbidea.adb.command.*;
import com.developerphil.adbidea.ui.DeviceChooserDialog;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidUtils;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.developerphil.adbidea.ui.NotificationHelper.error;

public class AdbWifiConnect {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AdbWifi-%d").build());

    public static void adbWifi(Project project) {
        executeOnDevice(project, new WifiCommand());
    }

    private static void executeOnDevice(final Project project, final Command runnable) {
        final DeviceResult result = getDevice(project);
        if (result != null) {
            for (final IDevice device : result.devices) {
                EXECUTOR.submit(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run(project, device, result.facet, result.packageName);
                    }
                });
            }
        }
    }

    private static DeviceResult getDevice(Project project) {
        List<AndroidFacet> facets = getApplicationFacets(project);
        if (!facets.isEmpty()) {
            AndroidFacet facet = facets.get(0);
            String packageName = AdbUtil.computePackageName(facet);
            AndroidDebugBridge bridge = AndroidSdkUtils.getDebugBridge(project);
            if (bridge == null) {
                error("No platform configured");
                return null;
            }
            int count = 0;
            while (!bridge.isConnected() || !bridge.hasInitialDeviceList()) {
                try {
                    Thread.sleep(100);
                    count++;
                } catch (InterruptedException e) {
                    // pass
                }

                // let's not wait > 10 sec.
                if (count > 100) {
                    error("Timeout getting device list!");
                    return null;
                }
            }

            IDevice[] devices = bridge.getDevices();
            if (devices.length == 1) {
                return new DeviceResult(devices, facet, packageName);
            } else if (devices.length > 1) {
                return askUserForDevice(facet, packageName);
            } else {
                return null;
            }

        }
        error("No devices found");
        return null;
    }

    private static List<AndroidFacet> getApplicationFacets(Project project) {

        List<AndroidFacet> facetList = AndroidUtils.getApplicationFacets(project);
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            AndroidFacet androidFacet = AndroidFacet.getInstance(module);
            if (androidFacet != null) {
                facetList.add(androidFacet);
            }
        }

        return facetList;
    }

    private static DeviceResult askUserForDevice(AndroidFacet facet, String packageName) {
        final DeviceChooserDialog chooser = new DeviceChooserDialog(facet);
        chooser.show();

        if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
            return null;
        }

        IDevice[] selectedDevices = chooser.getSelectedDevices();
        if (selectedDevices.length == 0) {
            return null;
        }

        return new DeviceResult(selectedDevices, facet, packageName);
    }

    private static final class DeviceResult {
        private final IDevice[] devices;
        private final AndroidFacet facet;
        private final String packageName;

        private DeviceResult(IDevice[] devices, AndroidFacet facet, String packageName) {
            this.devices = devices;
            this.facet = facet;
            this.packageName = packageName;
        }
    }
}
