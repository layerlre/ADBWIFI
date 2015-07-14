package com.layernet.adbwifi;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class AdbWifiAction extends AdbAction {
    @Override
    public void actionPerformed(AnActionEvent e, Project project) {
        AdbWifiConnect.adbWifi(project);
    }
}
